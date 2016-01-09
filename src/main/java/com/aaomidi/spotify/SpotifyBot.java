package com.aaomidi.spotify;

import com.aaomidi.spotify.handlers.CommandHandler;
import com.aaomidi.spotify.handlers.SpotifyHandler;
import com.aaomidi.spotify.hooks.TelegramHook;
import com.aaomidi.spotify.util.LogHandler;
import lombok.Getter;

/**
 * Created by amir on 2015-11-26.
 */
public class SpotifyBot {
    @Getter
    private TelegramHook telegramHook;
    @Getter
    private SpotifyHandler spotifyHandler;
    @Getter
    private CommandHandler commandHandler;

    public SpotifyBot(String... args) {
        this.setupTelegram(args[0]);
        this.setupSpotify(args[1], args[2], args[3]);
        this.setupCommands();

        this.keepAlive();
    }

    public static void main(String... args) {
        new SpotifyBot(args);
    }

    private void setupTelegram(String key) {
        LogHandler.logn("Connecting to telegram...");
        telegramHook = new TelegramHook(this, key);
        LogHandler.logn("\tConnected");
    }

    private void setupSpotify(String... args) {
        LogHandler.logn("Connecting to spotify...");
        spotifyHandler = new SpotifyHandler(args, this);
        LogHandler.logn("\tConnected");
    }

    private void setupCommands() {
        commandHandler = new CommandHandler(this);
        commandHandler.registerCommands();
    }

    private void keepAlive() {
        while (true) {
            //Chat chat = TelegramBot.getChat(-14978569);
            //chat.sendMessage("Hi!", telegramHook.getBot());
            //String in = System.console().readLine();
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
