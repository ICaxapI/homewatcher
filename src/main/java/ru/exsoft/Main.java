package ru.exsoft;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;

public class Main {
    private static boolean isGui;
    private static int generalPid;

    public static void main(String[] args) {
        for (String string : args) {
            if (string.toLowerCase().equals("gui")) {
                isGui = true;
            }
        }
        createPid();
        HomeWatcher homeWatcher = HomeWatcher.getInstance(isGui);
    }

    private static void createPid() {
        generalPid = Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]); //получаем pid процесса
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            System.out.println("App run on windows");
        } else {
            System.out.println("App run on unix, changing pid file to ./pid.pid");
            File pidFile = new File("./HomeWatcher.pid"); //ОС Linux требует генерации pid файла в папке с программой
            if (!pidFile.exists()) {
                try {
                    pidFile.createNewFile();
                    System.out.println("Successful create file " + pidFile.getAbsolutePath());
                } catch (IOException e) {
                    System.out.println("No permission to create file :(");
                    e.printStackTrace();
                }
            }
            try {
                FileWriter fw = new FileWriter(pidFile, false);
                fw.write(String.valueOf(generalPid));
                System.out.println("Successful write pid");
                fw.flush();
            } catch (IOException e) {
                System.out.println("No permission to read file");
                e.printStackTrace();
            }
        }
    }

}
