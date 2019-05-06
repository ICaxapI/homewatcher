package ru.exsoft.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private Config config = new Config();
    private ObjectMapper mapper = new ObjectMapper();

    public Config save(File file) throws IOException {
        mapper.writeValue(file, config);
        return this.config;
    }

    public Config load(File file) throws IOException {
        if (!file.exists()) save(file);
        else {
            config = mapper.readValue(file, Config.class);
            save(file);
        }
        return this.config;
    }

    public Config getConfig() {
        return config;
    }
}
