package com.aaomidi.engine;

import com.wrapper.spotify.models.Track;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import pro.zackpollard.telegrambot.api.chat.Chat;
import pro.zackpollard.telegrambot.api.chat.message.Message;

import java.util.HashMap;

/**
 * Created by amir on 2015-11-27.
 */
@RequiredArgsConstructor
public class TrackInformation {
    @Getter
    private final Chat chat;

    @Getter
    private final Message originalMessage;

    @Getter
    private final HashMap<String, Track> tracks;

    @Getter
    @Setter
    private boolean used = false;
}
