package ru.exsoft;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.exsoft.config.Config;
import ru.exsoft.utils.GeneralUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Telegram extends TelegramLongPollingBot {
    private static Telegram instance;
    private static Config config;
    public static final long MYCHAT = 324765066;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    public static void start(Config config) {
        Telegram.config = config;
        ApiContextInitializer.init(); // Инициализируем апи
        TelegramBotsApi botapi = new TelegramBotsApi();
        try {
            instance = new Telegram();
            botapi.registerBot(instance);
            System.out.println("Telegram api started");
            sendMsg("Program started", MYCHAT);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "ExSoftHomeWatcherBot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            int senderId = update.getMessage().getFrom().getId();
            System.out.println(String.format("Received message telegram. Chat \"%s\" User \"%s\" message \"%s\"", chatId, senderId, text));
            if (senderId == 324765066) {
                switch (text.toLowerCase()) {
                    case "/getphoto":
                    case "/getimage":
                        sendImage(OpenCV.Watcher.getCurrentImage(), "*" + sdf.format(new Date()) + "*", Telegram.MYCHAT);
                        break;
                    case "/getvideo":
                        OpenCV.Watcher.setNeedVideo(true);
                        break;
                    case "/getinhome": {
                        StringBuilder stringBuilder = new StringBuilder();
                        NetworkChecker.getInHome().forEach(str -> stringBuilder.append(String.format("%s : %s", str, config.knownHosts.containsKey(str) ? config.knownHosts.get(str) : MacVendor.get(str))).append("\n"));
                        sendMsg(stringBuilder.toString(), Telegram.MYCHAT);
                        break;
                    }
                    case "/getnf": {
                        StringBuilder stringBuilder = new StringBuilder();
                        NetworkChecker.getInHomeCurrent().forEach(str -> stringBuilder.append(String.format("%s : %s", str, config.knownHosts.containsKey(str) ? config.knownHosts.get(str) : MacVendor.get(str))).append("\n"));
                        sendMsg(stringBuilder.toString(), Telegram.MYCHAT);
                        break;
                    }
                    case "/gettemp":
                        try {
                            String out = GeneralUtils.getOutput("cat /sys/class/thermal/thermal_zone0/temp");
                            sendMsg(String.format("%s°C", out.replace("\n", "").trim()), Telegram.MYCHAT);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        break;
                    default:
                        sendMsg(String.format("Chat \"%s\" User \"%s\" message \"%s\"", chatId, senderId, text), chatId);
                        break;
                }
            }
        }
    }

    public static void sendVideo(File file, String text, long chatID){
        SendVideo video = new SendVideo();
        video.setVideo(file);
        video.setCaption(text);
        video.setParseMode("Markdown");
        video.setChatId(chatID);
        try {
            instance.execute(video);
            file.delete();
        } catch (TelegramApiException ex) {
            ex.printStackTrace();
        }
    }

    public static void sendImage(File file, String text, long chatID){
        SendPhoto photo = new SendPhoto();
        photo.setPhoto(file);
        photo.setCaption(text);
        photo.setParseMode("Markdown");
        photo.setChatId(chatID);
        try {
            instance.execute(photo);
            file.delete();
        } catch (TelegramApiException ex) {
            ex.printStackTrace();
        }
    }

    public static void sendMsg(String text, long chatID) { //-1001230733279 - tracker
        SendMessage s = new SendMessage();
        s.setText(text);
        s.setParseMode("Markdown");
        s.setChatId(chatID);
        try {
            instance.execute(s);
        } catch (TelegramApiException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getBotToken() {
        return "889354253:AAH-zuftzzHwjymLXfkdn7lXQzP0A7hk9qc";
    }

    public static Telegram getInstance() {
        return instance;
    }
}
