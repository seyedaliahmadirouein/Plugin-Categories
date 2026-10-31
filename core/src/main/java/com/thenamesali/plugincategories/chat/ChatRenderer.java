package com.thenamesali.plugincategories.chat;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface ChatRenderer {

    void send(CommandSender viewer, List<List<ChatPart>> lines);
}
