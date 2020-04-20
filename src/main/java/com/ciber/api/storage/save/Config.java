package com.ciber.api.storage.save;

import com.google.common.collect.Maps;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class Config extends ConfigSection {

    private Yaml yaml;
    private DumperOptions options;
    private YamlRepresenter representer;
    private File file;

    Config(String path) {
        super();
        this.name = "";
        options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        representer = new YamlRepresenter();
        representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yaml = new Yaml(new Constructor(), representer, options);
        file = new File(path);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            List<String> list = Files.readAllLines(file.toPath());
            StringBuilder sb = new StringBuilder();
            for (String str : list) {
                sb.append(str).append('\n');
            }
            values = (Map<String, Object>) yaml.load(sb.toString());
            if (values == null) values = Maps.newLinkedHashMap();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.root = null;
        try {
            this.getSection("", this, values);
        } catch (Exception ex) {
            System.out.println("Failed to get config sections");
        }
    }

    @Override
    public void save() {
        try {
            Files.write(file.toPath(), saveToString().getBytes(Charset.defaultCharset()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

    public String saveToString() {
        if (values == null) {
            return "{}\n";
        } else return yaml.dump(values);
    }

    public YamlRepresenter getRepresenter() {
        return representer;
    }

    public DumperOptions getOptions() {
        return options;
    }

    public Yaml getYaml() {
        return yaml;
    }

    public SavableAPI.ISavableAPI getApi() {
        return api;
    }

    public void setApi(SavableAPI.ISavableAPI api) {
        this.api = api;
    }
}
