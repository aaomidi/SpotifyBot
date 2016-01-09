package com.aaomidi.handlers;

import com.aaomidi.SpotifyBot;
import com.aaomidi.commands.SearchSongCommand;
import com.aaomidi.engine.TelegramCommand;
import pro.zackpollard.telegrambot.api.event.chat.message.CommandMessageReceivedEvent;
import pro.zackpollard.telegrambot.api.event.chat.message.TextMessageReceivedEvent;

import java.util.HashMap;

/**
 * Created by amir on 2015-11-26.
 */
public class CommandHandler {
    private final SpotifyBot instance;
    private HashMap<String, TelegramCommand> commands;

    public CommandHandler(SpotifyBot instance) {
        this.instance = instance;
        commands = new HashMap<>();
    }

    public void registerCommands() {
        new SearchSongCommand(instance, "Search", "Searches for a song on spotify.", "searchsong", "searchs", "ssong");
    }

    public void registerCommand(TelegramCommand telegramCommand) {
        commands.put(telegramCommand.getName().toLowerCase(), telegramCommand);

        for (String alias : telegramCommand.getAliases()) {
            commands.put(alias.toLowerCase(), telegramCommand);
        }
    }

    public void handleCommand(CommandMessageReceivedEvent event) {
        String cmdString = event.getCommand();
        cmdString = cmdString.toLowerCase();

        TelegramCommand command = commands.get(cmdString);

        if (command == null) return;

        command.execute(event);
    }

    public void handleText(final TextMessageReceivedEvent event) {
        commands.values().stream().forEach(c -> c.listenToReply(event));
    }

}
