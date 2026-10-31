package com.thenamesali.plugincategories.chat;

import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public final class ChatRendererFactory {

    private ChatRendererFactory() {
    }

    public static ChatRenderer create(Plugin plugin) {
        Logger logger = plugin.getLogger();
        if (classExists("net.kyori.adventure.text.Component")
                && classExists("net.kyori.adventure.audience.Audience")
                && classExists("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer")) {
            try {
                ChatRenderer renderer = new AdventureChatRenderer();
                logger.info("Chat renderer: Adventure API components");
                return renderer;
            } catch (Throwable throwable) {
                logger.warning("Adventure API was detected but could not be initialized, falling back to the BungeeCord chat API: " + throwable);
            }
        }
        logger.info("Chat renderer: BungeeCord chat API");
        return new BungeeChatRenderer();
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Throwable throwable) {
            return false;
        }
    }
}
