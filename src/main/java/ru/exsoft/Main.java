package ru.exsoft;

import ru.exsoft.config.Config;
import ru.exsoft.config.ConfigManager;
import ru.exsoft.gui.Gui;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        for (String string : args) {
            if (string.toLowerCase().equals("gui")) {
                new Thread(() -> Gui.startGui(args)).start();
            }
        }
        HomeWatcher homeWatcher = HomeWatcher.getInstance();
    }
}
