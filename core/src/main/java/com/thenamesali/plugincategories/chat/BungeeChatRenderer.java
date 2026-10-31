package com.thenamesali.plugincategories.chat;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class BungeeChatRenderer implements ChatRenderer {

    private static final Method OF_METHOD;

    static {
        Method method = null;
        try {
            method = ChatColor.class.getMethod("of", String.class);
        } catch (Throwable throwable) {
            method = null;
        }
        OF_METHOD = method;
    }

    @Override
    public void send(CommandSender viewer, List<List<ChatPart>> lines) {
        if (!(viewer instanceof Player)) {
            LegacyText.sendPlain(viewer, lines);
            return;
        }
        Player player = (Player) viewer;
        try {
            for (List<ChatPart> line : lines) {
                player.spigot().sendMessage(toComponents(line));
            }
        } catch (Throwable throwable) {
            LegacyText.sendPlain(viewer, lines);
        }
    }

    private BaseComponent[] toComponents(List<ChatPart> parts) {
        List<BaseComponent> components = new ArrayList<BaseComponent>();
        for (ChatPart part : parts) {
            TextComponent component = new TextComponent(part.getText());
            ChatColor color = bungeeColor(part.getColor());
            if (color != null) {
                component.setColor(color);
            }
            component.setBold(Boolean.valueOf(part.isBold()));
            component.setItalic(Boolean.valueOf(part.isItalic()));
            component.setUnderlined(Boolean.valueOf(part.isUnderlined()));
            component.setStrikethrough(Boolean.valueOf(part.isStrikethrough()));
            component.setObfuscated(Boolean.valueOf(part.isObfuscated()));
            if (part.getClickCommand() != null) {
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, part.getClickCommand()));
            }
            if (part.getHoverText() != null) {
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(part.getHoverText())));
            }
            components.add(component);
        }
        return components.toArray(new BaseComponent[0]);
    }

    private ChatColor bungeeColor(ColorSpec spec) {
        if (spec == null) {
            return null;
        }
        Integer hex = spec.getHex();
        if (hex != null && OF_METHOD != null) {
            try {
                return (ChatColor) OF_METHOD.invoke(null, String.format("#%06X", hex.intValue()));
            } catch (Throwable ignored) {
            }
        }
        return ChatColor.getByChar(spec.getLegacy());
    }
}
