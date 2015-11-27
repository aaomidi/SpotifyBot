package com.aaomidi.commands;

import com.aaomidi.SpotifyBot;
import com.aaomidi.engine.TelegramCommand;
import com.aaomidi.engine.TrackInformation;
import com.aaomidi.util.LogHandler;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.wrapper.spotify.models.Track;
import pro.zackpollard.telegrambot.api.chat.Chat;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import pro.zackpollard.telegrambot.api.event.chat.message.CommandMessageReceivedEvent;
import pro.zackpollard.telegrambot.api.event.chat.message.TextMessageReceivedEvent;
import pro.zackpollard.telegrambot.api.keyboards.ReplyKeyboardHide;
import pro.zackpollard.telegrambot.api.keyboards.ReplyKeyboardMarkup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by amir on 2015-11-26.
 */
public class SearchSongCommand extends TelegramCommand {
    RemovalListener<Integer, TrackInformation> removalListener = removalNotification -> {
        TrackInformation trackInformation = removalNotification.getValue();

        if (trackInformation.isUsed()) return;

        SendableTextMessage textMessage = SendableTextMessage.builder()
                .message("Slow reaction time!")
                .replyMarkup(ReplyKeyboardHide.builder().selective(true).build())
                .replyTo(trackInformation.getOriginalMessage())
                .build();

        trackInformation.getChat().sendMessage(textMessage, getTelegramBot());
    };

    Cache<Integer, TrackInformation> cache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .removalListener(removalListener)
            .build();

    public SearchSongCommand(SpotifyBot instance, String name, String description, String... aliases) {
        super(instance, name, description, aliases);
    }

    @Override
    public void execute(final CommandMessageReceivedEvent event) {
        LogHandler.logn("Command executed");

        String songName = event.getArgsString();
        final Consumer<List<Track>> consumer = new Consumer<List<Track>>() {
            Chat chat = event.getChat();

            @Override
            public void accept(List<Track> tracks) {
                if (tracks.size() == 1) {
                    Consumer<Track> con = getInstance().getSpotifyHandler().constructTrackConsumer(getTelegramBot(), event.getChat(), event.getMessage());
                    con.accept(tracks.get(0));
                    return;
                }

                ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder replyKeyboard = ReplyKeyboardMarkup.builder();
                HashMap<String, Track> trackHashMap = new HashMap<>();
                LogHandler.logn("Size: %d", tracks.size());
                List<String> row = new ArrayList<>();
                for (int i = 0; i < tracks.size(); i ++) {
                    if (i / 3 >= 1 && i % 3 == 0) {
                        replyKeyboard.addRow(row);
                        row = new ArrayList<>();
                    }
                    Track t = tracks.get(i);
                    String song = String.format("%s - %s", t.getName(), t.getArtists().get(0).getName());
                    row.add(song);
                    trackHashMap.put(song, t);
                }
                replyKeyboard.addRow(row);

                TrackInformation trackInformation = new TrackInformation(chat, event.getMessage(), trackHashMap);
                SendableTextMessage textMessage = SendableTextMessage.builder()
                        .message("Select your song: ")
                        .replyMarkup(replyKeyboard.oneTime(true).selective(true).build())
                        .replyTo(event.getMessage())
                        .build();

                Message sentMessage = chat.sendMessage(textMessage, getTelegramBot());

                cache.put(sentMessage.getMessageId(), trackInformation);
            }
        };

        getInstance().getSpotifyHandler().searchSongAsync(songName, consumer);
    }

    @Override
    public void listenToReply(TextMessageReceivedEvent event) {
        Message repliedTo = event.getMessage().getRepliedTo();
        if (repliedTo == null) return;

        TrackInformation trackInformation = cache.getIfPresent(repliedTo.getMessageId());
        if (trackInformation == null) return;

        Track track = trackInformation.getTracks().get(event.getContent().getContent());
        if (track == null) return;

        trackInformation.setUsed(true);

        cache.invalidate(repliedTo.getMessageId());

        Consumer<Track> con = getInstance().getSpotifyHandler().constructTrackConsumer(getTelegramBot(), event.getChat(), event.getMessage());
        con.accept(track);
    }
}
