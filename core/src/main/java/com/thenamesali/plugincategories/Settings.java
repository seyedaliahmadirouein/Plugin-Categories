package com.thenamesali.plugincategories;

import com.thenamesali.plugincategories.chat.ColorSpec;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class Settings {

    private final PluginCategoriesPlugin plugin;

    private String permission;
    private String adminPermission;
    private boolean categoryDetection;
    private boolean hoverMessages;
    private boolean pluginHover;
    private boolean debug;
    private List<String> commandRoots;
    private List<String> excludedFolders;
    private String navigationCommand;
    private long rescanIntervalMillis;
    private ColorSpec successColor;
    private ColorSpec failedColor;
    private ColorSpec categoryColor;
    private ColorSpec headerColor;
    private ColorSpec backColor;
    private boolean backBold;
    private String countFormat;
    private String categoryPrefix;
    private String categoryHover;
    private String backHover;
    private int backPadding;
    private String msgScanning;
    private String msgUnknownCategory;
    private String msgNoPermission;
    private String msgReloaded;

    public Settings(PluginCategoriesPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        FileConfiguration config = this.plugin.getConfig();
        this.permission = config.getString("permission", "plugincategories.use");
        this.adminPermission = config.getString("admin-permission", "plugincategories.admin");
        this.categoryDetection = config.getBoolean("category-detection", true);
        this.hoverMessages = config.getBoolean("hover-messages", true);
        this.pluginHover = config.getBoolean("plugin-hover", false);
        this.debug = config.getBoolean("debug", false);
        List<String> commands = config.contains("commands") ? config.getStringList("commands") : Arrays.asList("pl", "plugins");
        this.commandRoots = normalize(commands);
        List<String> excluded = config.contains("excluded-folders") ? config.getStringList("excluded-folders") : Arrays.asList("update");
        this.excludedFolders = normalize(excluded);
        this.navigationCommand = normalizeCommand(config.getString("navigation-command", "pl"));
        long seconds = config.getLong("rescan-interval-seconds", 30L);
        if (seconds < 1L) {
            seconds = 1L;
        }
        this.rescanIntervalMillis = seconds * 1000L;
        this.successColor = ColorSpec.parse(config.getString("colors.success", "&a"), ColorSpec.fromLegacy('a'));
        this.failedColor = ColorSpec.parse(config.getString("colors.failed", "&c"), ColorSpec.fromLegacy('c'));
        this.categoryColor = ColorSpec.parse(config.getString("colors.category", "&f"), ColorSpec.fromLegacy('f'));
        this.headerColor = ColorSpec.parse(config.getString("colors.header", "&f"), ColorSpec.fromLegacy('f'));
        this.backColor = ColorSpec.parse(config.getString("colors.back", "&c"), ColorSpec.fromLegacy('c'));
        this.backBold = config.getBoolean("colors.back-bold", true);
        this.countFormat = config.getString("count-format", "Plugins (%success%) (%failed%)");
        this.categoryPrefix = config.getString("category-prefix", "- ");
        this.categoryHover = config.getString("category-hover", "&7Click to view &f%category%");
        this.backHover = config.getString("back-hover", "&7Click to go back");
        this.backPadding = clamp(config.getInt("back-button-padding", 24), 0, 80);
        this.msgScanning = config.getString("messages.scanning", "&7Plugin data is still loading, please try again in a moment.");
        this.msgUnknownCategory = config.getString("messages.unknown-category", "&cUnknown category: &f%category%");
        this.msgNoPermission = config.getString("messages.no-permission", "&cYou do not have permission to use this command.");
        this.msgReloaded = config.getString("messages.reloaded", "&aPlugin Categories configuration reloaded and a rescan was started.");
    }

    public void reload() {
        this.plugin.reloadConfig();
        load();
    }

    private List<String> normalize(List<String> input) {
        List<String> output = new ArrayList<String>();
        for (String entry : input) {
            if (entry == null) {
                continue;
            }
            String value = entry.trim().toLowerCase(Locale.ROOT);
            if (!value.isEmpty() && !output.contains(value)) {
                output.add(value);
            }
        }
        return output;
    }

    private String normalizeCommand(String input) {
        if (input == null) {
            return "pl";
        }
        String value = input.trim().toLowerCase(Locale.ROOT);
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        return value.isEmpty() ? "pl" : value;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public String getPermission() {
        return this.permission;
    }

    public String getAdminPermission() {
        return this.adminPermission;
    }

    public boolean isCategoryDetection() {
        return this.categoryDetection;
    }

    public boolean isHoverMessages() {
        return this.hoverMessages;
    }

    public boolean isPluginHover() {
        return this.pluginHover;
    }

    public boolean isDebugEnabled() {
        return this.debug;
    }

    public List<String> getCommandRoots() {
        return this.commandRoots;
    }

    public List<String> getExcludedFolders() {
        return this.excludedFolders;
    }

    public String getNavigationCommand() {
        return this.navigationCommand;
    }

    public long getRescanIntervalMillis() {
        return this.rescanIntervalMillis;
    }

    public ColorSpec getSuccessColor() {
        return this.successColor;
    }

    public ColorSpec getFailedColor() {
        return this.failedColor;
    }

    public ColorSpec getCategoryColor() {
        return this.categoryColor;
    }

    public ColorSpec getHeaderColor() {
        return this.headerColor;
    }

    public ColorSpec getBackColor() {
        return this.backColor;
    }

    public boolean isBackBold() {
        return this.backBold;
    }

    public String getCountFormat() {
        return this.countFormat;
    }

    public String getCategoryPrefix() {
        return this.categoryPrefix;
    }

    public String getCategoryHover() {
        return this.categoryHover;
    }

    public String getBackHover() {
        return this.backHover;
    }

    public int getBackPadding() {
        return this.backPadding;
    }

    public String getMsgScanning() {
        return this.msgScanning;
    }

    public String getMsgUnknownCategory() {
        return this.msgUnknownCategory;
    }

    public String getMsgNoPermission() {
        return this.msgNoPermission;
    }

    public String getMsgReloaded() {
        return this.msgReloaded;
    }
}
