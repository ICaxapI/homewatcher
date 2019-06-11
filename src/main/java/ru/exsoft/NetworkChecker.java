package ru.exsoft;

import ru.exsoft.config.Config;
import ru.exsoft.utils.GeneralUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkChecker extends Thread {
    private static NetworkChecker instance;
    private Config config;
    private static ArrayList<String> inHome;
    private static HashMap<String, Integer> toDeleteBuffer = new HashMap<>();
    private static ArrayList<String> inHomeCurrent;

    private NetworkChecker(Config config){
        this.config = config;
        inHome = new ArrayList<>();
        OpenCV.Watcher.setOwnerInHome(false);
        start();
    }

    public void run() {
        while (!this.isInterrupted()) {
            try {
                inHomeCurrent = getInHomeList();
                ArrayList<String> toDeleteGeneral = new ArrayList<>();
                ArrayList<String> toDeleteFromTempRemove = new ArrayList<>();
                ArrayList<String> toIncrement = new ArrayList<>();
                Iterator<Map.Entry<String, Integer>> iterator = toDeleteBuffer.entrySet().iterator();
                while (iterator.hasNext()){
                    Map.Entry<String, Integer> entry = iterator.next();
                    int count = entry.getValue() + 1;
                    String str = entry.getKey();
                    if (count > config.filterPeriod) {
                        toDeleteGeneral.add(str);
                        Telegram.sendMsg(String.format("%s ушёл (%s)",
                                config.knownHosts.getOrDefault(str, MacVendor.get(str)), str), Telegram.MYCHAT);
                        toDeleteFromTempRemove.add(str);
                    } else {
                        if (inHomeCurrent.contains(str)) {
                            toDeleteFromTempRemove.add(str);
                        }
                    }
                    entry.setValue(count);
                }
                toIncrement.forEach((str -> toDeleteBuffer.put(str, toDeleteBuffer.get(str) + 1)));
                toDeleteGeneral.forEach(str -> inHome.remove(str));
                toDeleteFromTempRemove.forEach(str -> toDeleteBuffer.remove(str));
                inHomeCurrent.forEach(mac -> {
                    if (!inHome.contains(mac)) {
                        Telegram.sendMsg(String.format("%s пришёл (%s)",
                                config.knownHosts.getOrDefault(mac, MacVendor.get(mac)), mac), Telegram.MYCHAT);
                        inHome.add(mac);
                    }
                });
                inHome.forEach(mac -> {
                    if (!inHomeCurrent.contains(mac) && !toDeleteBuffer.containsKey(mac)){
                        toDeleteBuffer.put(mac, 0);
                    }
                });
                OpenCV.Watcher.setOwnerInHome(checkEnabled(inHome));
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    public boolean checkEnabled(ArrayList<String> check){
        for (String mac : config.greenHosts) {
            if (check.contains(mac)) return true;
        }
        return false;
    }

    public ArrayList<String>  getInHomeList(){
        ArrayList<String> inHome = new ArrayList();
        try {
            String out = GeneralUtils.getOutput("arp-scan --interface=eth0 --localnet");
            Pattern pattern = Pattern.compile("([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}");
            Matcher matcher = pattern.matcher(out);
            while (matcher.find()) {
                inHome.add(matcher.group().toUpperCase());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return inHome;
    }

    public static NetworkChecker getInstance(Config config){
        if (instance == null) instance = new NetworkChecker(config);
        return instance;
    }

    public static ArrayList<String> getInHome() {
        return inHome;
    }

    public static ArrayList<String> getInHomeCurrent() {
        return inHomeCurrent;
    }

}
