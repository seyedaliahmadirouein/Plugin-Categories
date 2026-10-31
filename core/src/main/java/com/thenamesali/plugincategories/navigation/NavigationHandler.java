package com.thenamesali.plugincategories.navigation;

import com.thenamesali.plugincategories.Settings;
import com.thenamesali.plugincategories.chat.LegacyText;
import com.thenamesali.plugincategories.scan.ScanManager;
import com.thenamesali.plugincategories.view.ViewService;
import org.bukkit.command.CommandSender;

public final class NavigationHandler {

    private final Settings settings;
    private final ScanManager scanManager;
    private final ViewService viewService;

    public NavigationHandler(Settings settings, ScanManager scanManager, ViewService viewService) {
        this.settings = settings;
        this.scanManager = scanManager;
        this.viewService = viewService;
    }

    public void handle(CommandSender sender, String[] args) {
        if (args.length > 0 && equalsAny(args[0], "reload")) {
            if (!sender.hasPermission(this.settings.getAdminPermission())) {
                sender.sendMessage(LegacyText.translate(this.settings.getMsgNoPermission()));
                return;
            }
            this.settings.reload();
            this.scanManager.requestScan(true);
            sender.sendMessage(LegacyText.translate(this.settings.getMsgReloaded()));
            return;
        }
        if (!sender.hasPermission(this.settings.getPermission())) {
            sender.sendMessage(LegacyText.translate(this.settings.getMsgNoPermission()));
            return;
        }
        if (args.length == 0 || equalsAny(args[0], "back", "menu", "main")) {
            this.viewService.showMain(sender);
            return;
        }
        if (equalsAny(args[0], "view", "open", "category")) {
            if (args.length < 2) {
                this.viewService.showMain(sender);
                return;
            }
            this.viewService.showCategory(sender, join(args, 1));
            return;
        }
        this.viewService.showCategory(sender, join(args, 0));
    }

    private String join(String[] args, int from) {
        StringBuilder builder = new StringBuilder();
        for (int i = from; i < args.length; i++) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(args[i]);
        }
        return builder.toString();
    }

    private boolean equalsAny(String input, String... options) {
        for (String option : options) {
            if (input.equalsIgnoreCase(option)) {
                return true;
            }
        }
        return false;
    }
}
