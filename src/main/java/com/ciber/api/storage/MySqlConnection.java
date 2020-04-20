package com.ciber.api.storage;

import com.ciber.api.CiberPlugin;
import com.ciber.api.object.CiberConfig;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MySqlConnection {

    private Boolean enable;
    private final Plugin plugin;
    private CiberConfig config;
    private String host;
    private int port;
    private String user;
    private String password;
    private String database;
    private String type;
    private boolean ssl;
    private Connection connection;

    public MySqlConnection(CiberPlugin plugin, CiberConfig config) {

        if (!config.contains("mysql")) {
            config.setBoolean("mysql", false);
            config.setString("mysql.host", "localhost");
            config.setInt("mysql.port", 3306);
            config.setString("mysql.user", "Ciber");
            config.setString("mysql.password", "");
            config.setString("mysql.database", "ciber-db");
            config.setString("mysql.url-type", "jdbc:mysql://");
            config.setBoolean("mysql.ssl", false);
            config.save();
        }

        this.config = config;
        this.plugin = plugin;
        this.enable = config.getBoolean("mysql");
        this.host = config.getString("mysql.host");
        this.port = config.getInt("mysql.port");
        this.user = config.getString("mysql.user");
        this.password = config.getString("mysql.password");
        this.database = config.getString("mysql.database");
        this.type = config.getString("mysql.url-type");
        this.ssl = config.getBoolean("mysql.ssl");

        String url = type + host + ":" + port + "/" + database + "createDatabaseIfNotExist=true&useSSL=" + ssl;

        try {
            connection = DriverManager.getConnection(user, password, url);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public Connection connect() {
        try {
            String url = type + host + ":" + port + "/" + database + "createDatabaseIfNotExist=true&useSSL=" + ssl;

            assert user != null;

            msg("§3[" + plugin.getName() + "] §6MongoDb: §aconexao bem sucedida usando database: §6" + database);

            return DriverManager.getConnection(user, password, url);
        } catch (Exception e) {
            e.printStackTrace();
            msg("§3[" + plugin.getName() + "] §6MySQL: §4Nao foi possivel se " +
                    "§4conectar verifique os dados em: §6[" + config.getName() + ".yml]");
        }
        return null;
    }

    public void close() throws SQLException {
        connection.close();
    }

    public void createTable(String table, String columns) {
        try {
            PreparedStatement db = connection.prepareStatement("use `" + database + "`;");
            db.executeUpdate();
            PreparedStatement statement = connection.prepareStatement(
                    "create table if not exists `" + table + "`(" + columns + ");"
            );
            statement.executeUpdate();
            msg("§3[" + plugin.getName() + "] §atabela: §6" + table + " §acriada na database: §6" + database);
        } catch (Exception e) {
        }
    }

    public void insert(String table, Map<String, Object> values) {
        try {
            PreparedStatement db = connection.prepareStatement("use `" + database + "`;");
            db.executeUpdate();

            String stringValues = values
                    .entrySet()
                    .stream()
                    .map(Map.Entry::getKey)
                    .collect(Collectors.joining(","));

            String objectValues = values
                    .entrySet()
                    .stream()
                    .map(it -> "?")
                    .collect(Collectors.joining(","));

            PreparedStatement statement = connection
                    .prepareStatement(
                            "insert into `"
                                    + table
                                    + "`("
                                    + stringValues
                                    + ") values ("
                                    + objectValues
                                    + ");"
                    );

            AtomicInteger statementNumber = new AtomicInteger(1);

            values.forEach((identifier, value) -> {
                try {
                    statement.setObject(statementNumber.get(), value);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
                statementNumber.getAndIncrement();
            });

            statement.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            msg("§3[" + plugin.getName() + "] §6MySQL: §4Nao foi possivel inserir dados na tabela: §6" + table);
        }
    }

    public void set(String table, String setColumn, Object value, Object identifier, String identifierValue) {
        try {

            PreparedStatement db = connection.prepareStatement("use `" + database + "`;");
            db.executeUpdate();

            PreparedStatement statement = connection.prepareStatement(
                    "update " + table + " set " + setColumn + " = ? where " + identifier + " = ?;"
            );
            statement.setObject(1, value);
            statement.setObject(2, identifierValue);
            statement.executeUpdate();
        } catch (Exception e){
            e.printStackTrace();
            msg("§3[" + plugin.getName() + "] §6MySQL: §4Nao foi possivel setar dados na tabela: §6" + table);
        }
    }

    public ResultSet get(String table, String valueColumn, String identifierColumn, Object identifierValue) {
        try {

            PreparedStatement db = connection.prepareStatement("use `" + database + "`;");
            db.executeUpdate();

            PreparedStatement statement = connect().prepareStatement(
                    "select " + valueColumn + " from " + table + " where " + identifierColumn + " = ?;"
            );
            statement.setObject(1, identifierValue);

            return statement.executeQuery();
        }catch (Exception e) {
            e.printStackTrace();
            msg("§3[" + plugin.getName() + "] §6MySQL: §cNao foi possivel encontrar dados na tabela: §6" + table);
        }
        return null;
    }

    public Boolean contains(String table, String identifierColumn, String valueColumn, Object identifierValue) {
        try {
            PreparedStatement db = connection.prepareStatement("use `" + database + "`;");
            db.executeUpdate();

            PreparedStatement statement = connect().prepareStatement(
                    "select " + valueColumn + " from " + table + " where " + identifierColumn  + " = ?;"
            );

            statement.setObject(1, identifierValue);
            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();

        }catch (Exception e) {
            e.printStackTrace();
            msg("§3[" + plugin.getName() + "] §6MySQL: §cNao foi possivel selecionar dados na tabela: §6" + table);
        }
        return false;
    }

    private void msg(String message) {
        Bukkit.getConsoleSender().sendMessage(message);
    }

    public boolean isEnable() {
        return config.getBoolean("mysql");
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
