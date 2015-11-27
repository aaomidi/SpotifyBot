package com.aaomidi.handlers;

import com.aaomidi.SpotifyBot;
import com.aaomidi.util.LogHandler;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.methods.*;
import com.wrapper.spotify.models.*;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.Chat;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import pro.zackpollard.telegrambot.api.chat.message.send.*;
import pro.zackpollard.telegrambot.api.keyboards.ReplyKeyboardHide;

import java.net.URL;
import java.util.List;
import java.util.function.Consumer;


/**
 * Created by amir on 2015-11-26.
 */
public class SpotifyHandler {
    private final SpotifyBot instance;
    private Api api;

    public SpotifyHandler(String[] args, SpotifyBot instance) {
        this.instance = instance;
        api = Api.builder()
                .clientId(args[0])
                .clientSecret(args[1])
                .redirectURI(args[2])
                .build();

    }

    private void handleMessage(String s) {

    }

    public Type getType(String uri) {

        String[] split = uri.split(":");

        if (split.length <= 2) return Type.NONE;
        if (!split[0].equalsIgnoreCase("spotify")) return Type.NONE;

        switch (split[1].toLowerCase()) {

            case "track":
                return Type.TRACK;

            case "artist":
                return Type.ARTIST;

            case "album":
                return Type.ALBUM;

            case "user":
                if (split.length <= 4) return Type.NONE;

                if (split[3].equalsIgnoreCase("playlist")) return Type.PLAYLIST;

            default:
                return Type.NONE;
        }
    }

    public String getParsedURI(String uri, Type type) {
        // Efficient
        if (type == Type.NONE) return null;

        String[] split = uri.split(":");
        switch (type) {

            case ARTIST:
            case ALBUM:
            case TRACK:
                return split[2];

            case PLAYLIST:
                return split[4];

            default:
                return null;
        }
    }

    public String getUserID(String uri, Type type) {
        assert type == Type.PLAYLIST;

        String[] split = uri.split(":");

        if (split.length != 5) return null;

        return split[2];
    }

    public Consumer<Track> constructTrackConsumer(final TelegramBot bot, final Chat chat, final Message m) {
        return track -> {

            StringBuilder message = new StringBuilder(
                    String.format("Name: [%s](%s)\n", track.getName(), track.getExternalUrls().get("spotify")));

            if (track.getArtists().size() == 1) {
                SimpleArtist artist = track.getArtists().get(0);
                message.append(String.format("Artist: [%s](%s)\n", artist.getName(), artist.getExternalUrls().get("spotify")));
            } else {
                message.append("Artists: \n");
                for (SimpleArtist artist : track.getArtists()) {
                    message.append(String.format("\t - [%s](%s)\n", artist.getName(), artist.getExternalUrls().get("spotify")));
                }
            }


            double popularity = track.getPopularity() / 10.0;

            message
                    .append(String.format("Album: [%s](%s)\n", track.getAlbum().getName(), track.getAlbum().getExternalUrls().get("spotify")))
                    .append(String.format("Popularity: %.2f %s", popularity, popularity > 5 ? (popularity > 9 ? "\uD83D\uDC4C" : "\uD83D\uDC4D") : "\uD83D\uDC4E"));

            final SendableMessage sendableTextMessage = SendableTextMessage.builder()
                    .message(message.toString())
                    .parseMode(ParseMode.MARKDOWN)
                    .disableWebPagePreview(true)
                    .replyMarkup(ReplyKeyboardHide.builder().selective(true).build())
                    .replyTo(m)
                    .build();

            chat.sendMessage(sendableTextMessage, bot);

            if (track.getPreviewUrl() == null) return;

            /** Let people know something is uploading **/
            final SendableChatAction sendableChatAction = SendableChatAction.builder()
                    .chatAction(ChatAction.UPLOAD_AUDIO)
                    .build();

            chat.sendMessage(sendableChatAction, bot);
            /** End letting people know **/

            LogHandler.logn("\t\t%s", track.getPreviewUrl());
            SendableAudioMessage sendableAudioMessage = null;
            try {
                sendableAudioMessage = SendableAudioMessage.builder()
                        .duration(30)
                        .title(track.getName())
                        .performer(track.getArtists().get(0).getName())
                        .audio(new InputFile(new URL(track.getPreviewUrl() + ".mp3")))
                        .replyTo(m)
                        .build();

                chat.sendMessage(sendableAudioMessage, bot);

            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    public Consumer<Artist> constructArtistConsumer(final TelegramBot bot, final Chat chat) {
        return artist -> {

            StringBuilder message = new StringBuilder(
                    String.format("Name: [%s](%s)\n", artist.getName(), artist.getExternalUrls().get("spotify")));


            double popularity = artist.getPopularity() / 10.0;
            message
                    .append(String.format("Popularity: %.2f %s", popularity, popularity > 5 ? (popularity > 9 ? "\uD83D\uDC4C" : "\uD83D\uDC4D") : "\uD83D\uDC4E"));


            final SendableMessage sendableTextMessage = SendableTextMessage.builder()
                    .message(message.toString())
                    .parseMode(ParseMode.MARKDOWN)
                    .disableWebPagePreview(true)
                    .build();

            chat.sendMessage(sendableTextMessage, bot);

        };
    }

    public Consumer<Album> constructAlbumConsumer(final TelegramBot bot, final Chat chat) {
        return album -> {
            StringBuilder message = new StringBuilder(
                    String.format("Name: [%s](%s)\n", album.getName(), album.getExternalUrls().get("spotify")));

            if (album.getArtists().size() == 1) {
                SimpleArtist artist = album.getArtists().get(0);
                message.append(String.format("Artist: [%s](%s)\n", artist.getName(), artist.getExternalUrls().get("spotify")));
            } else {
                message.append("Artists: \n");

                for (SimpleArtist artist : album.getArtists()) {
                    message.append(String.format("\t - [%s](%s)\n", artist.getName(), artist.getExternalUrls().get("spotify")));
                }
            }
            message
                    .append(String.format("Released: %s\n", album.getReleaseDate()));

            double popularity = album.getPopularity() / 10.0;
            message
                    .append(String.format("Popularity: %.2f %s", popularity, popularity > 5 ? (popularity > 9 ? "\uD83D\uDC4C" : "\uD83D\uDC4D") : "\uD83D\uDC4E"));

            SendableMessage sendableTextMessage = SendableTextMessage.builder()
                    .message(message.toString())
                    .parseMode(ParseMode.MARKDOWN)
                    .disableWebPagePreview(true)
                    .build();

            chat.sendMessage(sendableTextMessage, bot);
        };
    }

    public Consumer<Playlist> constructPlaylistConsumer(final TelegramBot bot, final Chat chat) {
        return playlist -> {
            StringBuilder message = new StringBuilder(
                    String.format("Name: [%s](%s)\n", playlist.getName(), playlist.getExternalUrls().get("spotify")));


            message
                    .append(String.format("Owner: [%s](%s)\n", playlist.getOwner().getDisplayName(), playlist.getExternalUrls().get("spotify")));

            SendableMessage sendableTextMessage = SendableTextMessage.builder()
                    .message(message.toString())
                    .parseMode(ParseMode.MARKDOWN)
                    .disableWebPagePreview(true)
                    .build();

            chat.sendMessage(sendableTextMessage, bot);
        };
    }

    public Track handleTrack(String uri) {
        TrackRequest request = api.getTrack(uri).build();

        try {
            return request.get();
        } catch (Exception e) {
            return null;
        }
    }

    public void handleTrackAsync(final String uri, final Consumer<Track> consumer) {
        new Thread(() -> {
            Track track = handleTrack(uri);
            if (track == null) return;

            consumer.accept(track);
        }).start();
    }

    public Artist handleArtist(String uri) {
        ArtistRequest request = api.getArtist(uri).build();

        try {
            return request.get();
        } catch (Exception e) {
            return null;
        }
    }

    public void handleArtistAsync(final String uri, final Consumer<Artist> consumer) {
        new Thread(() -> {
            Artist artist = handleArtist(uri);
            if (artist == null) return;

            consumer.accept(artist);
        }).start();
    }

    public Album handleAlbum(String uri) {
        AlbumRequest request = api.getAlbum(uri).build();

        try {
            return request.get();
        } catch (Exception e) {
            return null;
        }
    }

    public void handleAlbumAsync(final String uri, final Consumer<Album> consumer) {
        new Thread(() -> {
            Album album = handleAlbum(uri);
            if (album == null) return;

            consumer.accept(album);
        }).start();
    }

    public Playlist handlePlaylist(final String userID, final String uri) {
        PlaylistRequest request = api.getPlaylist(userID, uri).build();

        try {
            return request.get();
        } catch (Exception e) {
            return null;
        }
    }

    public void handlePlaylistAsync(final String userID, final String uri, final Consumer<Playlist> consumer) {
        new Thread(() -> {
            Playlist playlist = handlePlaylist(userID, uri);
            LogHandler.logn("Info: %s\t\t%s", userID, uri);
            if (playlist == null) {
                LogHandler.logn("DEATH");
                return;
            }

            consumer.accept(playlist);
        }).start();
    }

    private List<Track> searchSong(String name) {
        TrackSearchRequest request = api.searchTracks(name).build();
        try {
            Page<Track> tracks = request.get();
            return tracks.getItems();
        } catch (Exception e) {
            return null;
        }
    }

    public void searchSongAsync(final String name, final Consumer<List<Track>> consumer) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Track> tracks = searchSong(name);

                if (tracks == null || tracks.size() == 0) return;

                consumer.accept(tracks);
            }
        }).start();
    }

    public enum Type {
        ARTIST,
        ALBUM,
        PLAYLIST,
        TRACK,
        NONE;
    }
}
