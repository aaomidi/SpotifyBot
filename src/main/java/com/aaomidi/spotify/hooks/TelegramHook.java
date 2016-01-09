package com.aaomidi.spotify.hooks;

import com.aaomidi.spotify.SpotifyBot;
import com.aaomidi.spotify.handlers.SpotifyHandler;
import com.aaomidi.spotify.util.LogHandler;
import com.wrapper.spotify.models.Album;
import com.wrapper.spotify.models.Artist;
import com.wrapper.spotify.models.Track;
import lombok.Getter;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.Chat;
import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.message.CommandMessageReceivedEvent;
import pro.zackpollard.telegrambot.api.event.chat.message.TextMessageReceivedEvent;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by amir on 2015-11-26.
 */
public class TelegramHook implements Listener {
    private final SpotifyBot instance;
    private final Pattern pattern = Pattern.compile(".*((spotify:)[\\w:]+).*", Pattern.CASE_INSENSITIVE);
    @Getter
    private TelegramBot bot;

    public TelegramHook(SpotifyBot instance, String auth) {
        this.instance = instance;

        bot = TelegramBot.login(auth);

        bot.getEventsManager().register(this);

        bot.startUpdates(false);

        Chat mazenChat = TelegramBot.getChat(-17349250);

        //mazenChat.sendMessage("I LIKE TITS!!! @zackpollard", bot);
    }

    @Override
    public void onCommandMessageReceived(CommandMessageReceivedEvent event) {
        LogHandler.logn("Command received: %s", event.getContent().getContent());

        instance.getCommandHandler().handleCommand(event);
    }

    @Override
    public void onTextMessageReceived(TextMessageReceivedEvent event) {
        LogHandler.logn("Message received %s: %s", event.getChat().getId(), event.getContent().getContent());
        instance.getCommandHandler().handleText(event);

        String message = event.getContent().getContent();

        Chat chat = event.getChat();

        String uri = getSpotifyURI(message);

        if (uri == null) return;

        SpotifyHandler.Type type = instance.getSpotifyHandler().getType(uri);
        String parsedURI = instance.getSpotifyHandler().getParsedURI(uri, type);

        if (parsedURI == null) return;

        LogHandler.logn(type.toString());
        switch (type) {
            case ARTIST: {
                Consumer<Artist> consumer = instance.getSpotifyHandler().constructArtistConsumer(bot, chat);
                instance.getSpotifyHandler().handleArtistAsync(parsedURI, consumer);
                break;
            }
            case ALBUM: {
                Consumer<Album> consumer = instance.getSpotifyHandler().constructAlbumConsumer(bot, chat);
                instance.getSpotifyHandler().handleAlbumAsync(parsedURI, consumer);
                break;
            }
            case PLAYLIST: {
                //String userID = instance.getSpotifyHandler().getUserID(uri, type);
                //LogHandler.logn("\t %s\n\t %s", userID, parsedURI);
                //Consumer<Playlist> consumer = instance.getSpotifyHandler().constructPlaylistConsumer(bot, chat);
                //instance.getSpotifyHandler().handlePlaylistAsync(userID, parsedURI, consumer);
                chat.sendMessage("Due to some issues, playlists are currently broken. Sorry.", bot);
                break;
            }
            case TRACK: {
                Consumer<Track> consumer = instance.getSpotifyHandler().constructTrackConsumer(bot, chat, event.getMessage());
                instance.getSpotifyHandler().handleTrackAsync(parsedURI, consumer);
                break;
            }
            case NONE:
                break;
        }

    }


    private String getSpotifyURI(String message) {
        Matcher matcher = pattern.matcher(message);

        if (!matcher.matches()) return null;

        return matcher.group(1);
    }
}
