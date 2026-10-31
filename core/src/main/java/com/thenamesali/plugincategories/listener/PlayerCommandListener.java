package com.thenamesali.plugincategories.listener;

import com.thenamesali.plugincategories.Settings;
import com.thenamesali.plugincategories.navigation.NavigationHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.Locale;

public final class PlayerCommandListener implements Listener {

    private final Settings settings;
    private final NavigationHandler navigation;

    public PlayerCommandListener(Settings settings, NavigationHandler navigation) {
        this.settings = settings;
        this.navigation = navigation;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (message == null || message.length() < 2 || message.charAt(0) != '/') {
            return;
        }
        String[] args = message.substring(1).trim().split("\\s+");
        if (args.length == 0 || args[0].isEmpty()) {
            return;
        }
        String root = args[0].toLowerCase(Locale.ROOT);
        if (!this.settings.getCommandRoots().contains(root)) {
            return;
        }
        Player player = event.getPlayer();
        if (!player.hasPermission(this.settings.getPermission())) {
            return;
        }
        event.setCancelled(true);
        this.navigation.handle(player, Arrays.copyOfRange(args, 1, args.length));
    }
}
