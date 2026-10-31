package com.thenamesali.plugincategories.chat;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class AdventureChatRenderer implements ChatRenderer {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    @Override
    public void send(CommandSender viewer, List<List<ChatPart>> lines) {
        if (!(viewer instanceof Audience)) {
            LegacyText.sendPlain(viewer, lines);
            return;
        }
        Audience audience = (Audience) viewer;
        for (List<ChatPart> line : lines) {
            audience.sendMessage(buildLine(line));
        }
    }

    private Component buildLine(List<ChatPart> parts) {
        TextComponent.Builder builder = Component.text();
        for (ChatPart part : parts) {
            builder.append(buildPart(part));
        }
        return builder.build();
    }

    private Component buildPart(ChatPart part) {
        TextComponent.Builder builder = Component.text().content(part.getText());
        ColorSpec color = part.getColor();
        if (color != null) {
            Integer hex = color.getHex();
            builder.color(hex != null ? TextColor.color(hex.intValue()) : namedColor(color.getLegacy()));
        }
        if (part.isBold()) {
            builder.decorate(TextDecoration.BOLD);
        }
        if (part.isItalic()) {
            builder.decorate(TextDecoration.ITALIC);
        }
        if (part.isUnderlined()) {
            builder.decorate(TextDecoration.UNDERLINED);
        }
        if (part.isStrikethrough()) {
            builder.decorate(TextDecoration.STRIKETHROUGH);
        }
        if (part.isObfuscated()) {
            builder.decorate(TextDecoration.OBFUSCATED);
        }
        if (part.getClickCommand() != null) {
            builder.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, part.getClickCommand()));
        }
        if (part.getHoverText() != null) {
            builder.hoverEvent(HoverEvent.showText(LEGACY.deserialize(part.getHoverText())));
        }
        return builder.build();
    }

    private NamedTextColor namedColor(char code) {
        switch (code) {
            case '0':
                return NamedTextColor.BLACK;
            case '1':
                return NamedTextColor.DARK_BLUE;
            case '2':
                return NamedTextColor.DARK_GREEN;
            case '3':
                return NamedTextColor.DARK_AQUA;
            case '4':
                return NamedTextColor.DARK_RED;
            case '5':
                return NamedTextColor.DARK_PURPLE;
            case '6':
                return NamedTextColor.GOLD;
            case '7':
                return NamedTextColor.GRAY;
            case '8':
                return NamedTextColor.DARK_GRAY;
            case '9':
                return NamedTextColor.BLUE;
            case 'a':
                return NamedTextColor.GREEN;
            case 'b':
                return NamedTextColor.AQUA;
            case 'c':
                return NamedTextColor.RED;
            case 'd':
                return NamedTextColor.LIGHT_PURPLE;
            case 'e':
                return NamedTextColor.YELLOW;
            default:
                return NamedTextColor.WHITE;
        }
    }
}
