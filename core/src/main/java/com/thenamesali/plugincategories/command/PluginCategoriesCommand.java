package com.thenamesali.plugincategories.command;

import com.thenamesali.plugincategories.Settings;
import com.thenamesali.plugincategories.model.CategoryData;
import com.thenamesali.plugincategories.navigation.NavigationHandler;
import com.thenamesali.plugincategories.scan.ScanManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PluginCategoriesCommand implements CommandExecutor, TabCompleter {

    private final NavigationHandler navigation;
    private final Settings settings;
    private final ScanManager scanManager;

    public PluginCategoriesCommand(NavigationHandler navigation, Settings settings, ScanManager scanManager) {
        this.navigation = navigation;
        this.settings = settings;
        this.scanManager = scanManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.navigation.handle(sender, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<String>();
        if (!sender.hasPermission(this.settings.getPermission())) {
            return suggestions;
        }
        if (args.length == 1) {
            addMatching(suggestions, args[0], "view", "back", "reload");
            for (CategoryData category : this.scanManager.latest().getCategories()) {
                addMatching(suggestions, args[0], category.getName());
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("view")) {
            for (CategoryData category : this.scanManager.latest().getCategories()) {
                addMatching(suggestions, args[1], category.getName());
            }
        }
        return suggestions;
    }

    private void addMatching(List<String> suggestions, String token, String... options) {
        String lower = token.toLowerCase(Locale.ROOT);
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                suggestions.add(option);
            }
        }
    }
}
