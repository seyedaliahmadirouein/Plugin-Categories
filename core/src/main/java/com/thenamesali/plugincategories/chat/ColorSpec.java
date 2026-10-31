package com.thenamesali.plugincategories.chat;

import org.bukkit.ChatColor;

import java.util.Locale;

public final class ColorSpec {

    private static final int[] LEGACY_RGB = {
            0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
            0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF
    };

    private final char legacy;
    private final Integer hex;

    private ColorSpec(char legacy, Integer hex) {
        this.legacy = legacy;
        this.hex = hex;
    }

    public static ColorSpec fromLegacy(char code) {
        return new ColorSpec(Character.toLowerCase(code), null);
    }

    public static ColorSpec parse(String raw, ColorSpec fallback) {
        if (raw == null) {
            return fallback;
        }
        String value = raw.trim();
        if (value.isEmpty()) {
            return fallback;
        }
        if (value.startsWith("#") && value.length() == 7) {
            try {
                int rgb = Integer.parseInt(value.substring(1), 16);
                return new ColorSpec(nearestLegacy(rgb), Integer.valueOf(rgb));
            } catch (NumberFormatException exception) {
                return fallback;
            }
        }
        if (value.length() == 2 && (value.charAt(0) == '&' || value.charAt(0) == '\u00A7')) {
            char code = Character.toLowerCase(value.charAt(1));
            if ("0123456789abcdef".indexOf(code) >= 0) {
                return fromLegacy(code);
            }
            return fallback;
        }
        if (value.length() == 1) {
            char code = Character.toLowerCase(value.charAt(0));
            if ("0123456789abcdef".indexOf(code) >= 0) {
                return fromLegacy(code);
            }
        }
        try {
            ChatColor bukkit = ChatColor.valueOf(value.toUpperCase(Locale.ROOT));
            return fromLegacy(bukkit.getChar());
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }

    public static char nearestLegacy(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        char best = 'f';
        int bestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < LEGACY_RGB.length; i++) {
            int candidate = LEGACY_RGB[i];
            int dr = r - ((candidate >> 16) & 0xFF);
            int dg = g - ((candidate >> 8) & 0xFF);
            int db = b - (candidate & 0xFF);
            int distance = dr * dr + dg * dg + db * db;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = "0123456789abcdef".charAt(i);
            }
        }
        return best;
    }

    public char getLegacy() {
        return this.legacy;
    }

    public Integer getHex() {
        return this.hex;
    }
}
