package ru.exsoft;

import ru.exsoft.config.Config;
import ru.exsoft.config.ConfigManager;

import java.io.File;
import java.io.IOException;

public class HomeWatcher {
    public final ConfigManager configManager = new ConfigManager();

    private static final File CONFIG_FILE = new File("./config.json");

    private static HomeWatcher instance;

    private HomeWatcher() {
        try {
            configManager.load(CONFIG_FILE);
            Config config = configManager.getConfig();
            Telegram.start(config.proxyHost, config.proxyPort, config.proxyUser, config.proxyPassword);
            OpenCV.init(config.width, config.height, config.sensitivity, config.maxarea);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HomeWatcher getInstance() {
        if (instance == null) {
            instance = new HomeWatcher();
        }
        return instance;
    }
}
