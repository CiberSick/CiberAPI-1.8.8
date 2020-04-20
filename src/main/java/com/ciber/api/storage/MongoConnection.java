package com.ciber.api.storage;

import com.ciber.api.object.CiberConfig;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoConnection {

    private Plugin plugin;
    private CiberConfig config;
    private String host;
    private String user;
    private String password;
    private String databaseName;
    private String type;
    private MongoClient client;
    private MongoDatabase database;

    public MongoConnection(Plugin plugin, CiberConfig config) {

        if (!config.contains("mongo")) {
            config.setBoolean("mongo", false);
            config.setString("mongo.host", "cibersick-d1zga.gcp.mongodb.net");
            config.setString("mongo.user", "CiberSick");
            config.setString("mongo.password", "32431169");
            config.setString("mongo.database", "ciber-db");
            config.setString("mongo.url-type", "mongodb+srv://");
            config.save();
        }

        this.host = config.getString("mongo.host");
        this.user = config.getString("mongo.user");
        this.password = config.getString("mongo.password");
        this.databaseName = config.getString("mongo.database");
        this.type = config.getString("mongo.url-type");

        this.plugin = plugin;
        this.config = config;
    }

    public void connect() {
        try {
            Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
            mongoLogger.setLevel(Level.SEVERE);
            client = MongoClients.create(type + user + ":" + password + "@" + host + "/test");
            database = client.getDatabase(databaseName);
            msg("§3[" + plugin.getName() + "] §6MongoDb: §aconexao bem sucedida usando database: §6" + database.getName());
        } catch (Exception e) {
            e.printStackTrace();
            msg("§3[" + plugin.getName() + "] §6MongoDb: §4Nao foi possivel se " +
                    "§4conectar verifique os dados em: §6[" + config.getName() + ".yml]");
        }
    }

    public void insert(String collectionName, Map<String, Object> values) {
        try {
            MongoCollection collection = database.getCollection(collectionName);

            values.forEach((column, value) -> {
                Document document = new Document(column, value);
                if (collection.find(document).first() == null) {
                    document.append(column, value);
                    collection.insertOne(document);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            msg("§3[" + plugin.getName() + "] §6MongoDb: §4Nao foi possivel inserir dados pois " +
                    "nao existe uma colecao com o nome §6" + collectionName);
        }
    }

    public void set(String collectionName, Object value, String identifier, String column, Object columnValue) {
        try {
            MongoCollection collection = database.getCollection(collectionName);
            Document document = new Document(column, columnValue);
            Document newValue = new Document(identifier, value);
            Document updateOperation = new Document("$set", newValue);
            collection.updateOne(document, updateOperation);
        } catch (Exception e) {
            e.printStackTrace();
            msg("§3[" + plugin.getName() + "] §6MongoDb: §4Nao foi possivel setar dados pois " +
                    "nao existe uma colecao com o nome §6" + collectionName);
        }
    }

    public Object get(String collectionName, String identifier, String column, Object columnValue) {
        try {
            MongoCollection collection = database.getCollection(collectionName);
            Document document = new Document(column, columnValue);
            if (collection.find(document).first() != null) {
                return collection.find().first();
            }
        } catch (Exception e) {
            e.printStackTrace();
            msg("§3[" + plugin.getName() + "] §6MongoDb: §4Nao foi possivel encontrar dados pois " +
                    "nao existe uma colecao com o nome §6" + collectionName);
        }
        return null;
    }

    public Boolean contains(String collectionName, String identifier, String column, Object columnValue){
        try {
            if (get(collectionName, identifier, column, columnValue) != null) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            msg("§3[" + plugin.getName() + "] §6MongoDb: §4Nao foi possivel encontrar dados pois " +
                    "nao existe uma colecao com o nome §6" + collectionName);
        }
        return false;
    }

    public void msg(String msg) {
        Bukkit.getConsoleSender().sendMessage(msg);
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public CiberConfig getConfig() {
        return config;
    }

    public void setConfig(CiberConfig config) {
        this.config = config;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public void setDatabase(MongoDatabase database) {
        this.database = database;
    }

    public Boolean isEnable() {
        return config.getBoolean("mysql");
    }

    public void setEnable(Boolean enable) {
        config.setBoolean("mysql", enable);
    }
}
