package ru.exsoft;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

public class Telegram extends AbilityBot {
    public static final long MYCHAT = -1001230733279L;
    private static final String TOKEN = "889354253:AAH-zuftzzHwjymLXfkdn7lXQzP0A7hk9qc";
    private static final String BOTNAME = "ExSoftHomeWatcherBot";
    public static Telegram instance;

    protected Telegram(String botToken, String botUsername, DefaultBotOptions botOptions) {
        super(botToken, botUsername, botOptions);
    }

    public int creatorId() {
        return 0;
    }

    public Ability pingPong() {
        return Ability
                .builder()
                .name("ping")
                .info("ping pong")
                .locality(ALL)
                .privacy(PUBLIC)
                .action(ctx -> silent.send("pong", ctx.chatId()))
                .build();
    }

    public static void start(String proxyHost, Integer proxyPort, String proxyUser, String proxyPassword) {
        try {
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
                }
            });
            ApiContextInitializer.init();
            TelegramBotsApi botApi = new TelegramBotsApi();
            DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);
            botOptions.setProxyHost(proxyHost);
            botOptions.setProxyPort(proxyPort);
            botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
            instance = new Telegram(TOKEN, BOTNAME, botOptions);
            botApi.registerBot(instance);
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            int senderId = update.getMessage().getFrom().getId();
            sendMsg(String.format("Chat \"%s\" User \"%s\" message \"%s\"", chatId, senderId, text), chatId);
            System.out.println(String.format("Received message telegram. Chat \"%s\" User \"%s\" message \"%s\"", chatId, senderId, text));
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

}
