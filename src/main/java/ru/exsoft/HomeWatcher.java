package ru.exsoft;

import ru.exsoft.config.Config;
import ru.exsoft.config.ConfigManager;

import java.io.File;
import java.io.IOException;

public class HomeWatcher {
    public final ConfigManager configManager = new ConfigManager();

    private static final File CONFIG_FILE = new File("./config.json");

    private static HomeWatcher instance;

    private HomeWatcher(boolean isGui) {
        try {
            configManager.load(CONFIG_FILE);
            Config config = configManager.getConfig();
            Telegram.start(config);
            NetworkChecker  networkChecker = NetworkChecker.getInstance(config);
            OpenCV.init(config, isGui);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HomeWatcher getInstance(boolean isGui) {
        if (instance == null) {
            instance = new HomeWatcher(isGui);
        }
        return instance;
    }
}
