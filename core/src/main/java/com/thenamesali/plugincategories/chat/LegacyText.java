package com.thenamesali.plugincategories.chat;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public final class LegacyText {

    private static final String CODES = "0123456789abcdefklmnor";

    private LegacyText() {
    }

    public static List<ChatPart> parse(String input, ColorSpec defaultColor) {
        List<ChatPart> parts = new ArrayList<ChatPart>();
        if (input == null || input.isEmpty()) {
            return parts;
        }
        ColorSpec color = defaultColor;
        boolean bold = false;
        boolean italic = false;
        boolean underlined = false;
        boolean strikethrough = false;
        boolean obfuscated = false;
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            char next = i + 1 < input.length() ? input.charAt(i + 1) : '\u0000';
            if (current == '&' && CODES.indexOf(Character.toLowerCase(next)) >= 0) {
                if (text.length() > 0) {
                    parts.add(new ChatPart(text.toString(), color, bold, italic, underlined, strikethrough, obfuscated));
                    text.setLength(0);
                }
                char code = Character.toLowerCase(next);
                if ((code >= '0' && code <= '9') || (code >= 'a' && code <= 'f')) {
                    color = ColorSpec.fromLegacy(code);
                    bold = false;
                    italic = false;
                    underlined = false;
                    strikethrough = false;
                    obfuscated = false;
                } else if (code == 'k') {
                    obfuscated = true;
                } else if (code == 'l') {
                    bold = true;
                } else if (code == 'm') {
                    strikethrough = true;
                } else if (code == 'n') {
                    underlined = true;
                } else if (code == 'o') {
                    italic = true;
                } else {
                    color = defaultColor;
                    bold = false;
                    italic = false;
                    underlined = false;
                    strikethrough = false;
                    obfuscated = false;
                }
                i++;
                continue;
            }
            text.append(current);
        }
        if (text.length() > 0) {
            parts.add(new ChatPart(text.toString(), color, bold, italic, underlined, strikethrough, obfuscated));
        }
        return parts;
    }

    public static String plain(List<ChatPart> line) {
        StringBuilder builder = new StringBuilder();
        for (ChatPart part : line) {
            ColorSpec color = part.getColor();
            if (color != null) {
                builder.append('\u00A7').append(color.getLegacy());
            }
            if (part.isBold()) {
                builder.append('\u00A7').append('l');
            }
            if (part.isItalic()) {
                builder.append('\u00A7').append('o');
            }
            if (part.isUnderlined()) {
                builder.append('\u00A7').append('n');
            }
            if (part.isStrikethrough()) {
                builder.append('\u00A7').append('m');
            }
            if (part.isObfuscated()) {
                builder.append('\u00A7').append('k');
            }
            builder.append(part.getText());
        }
        return builder.toString();
    }

    public static void sendPlain(CommandSender viewer, List<List<ChatPart>> lines) {
        for (List<ChatPart> line : lines) {
            viewer.sendMessage(plain(line));
        }
    }

    public static String translate(String raw) {
        if (raw == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', raw);
    }
}
