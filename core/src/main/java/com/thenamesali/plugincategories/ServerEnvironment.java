package com.thenamesali.plugincategories;

import org.bukkit.Bukkit;

public final class ServerEnvironment {

    private final String software;

    public ServerEnvironment() {
        String detected;
        if (classExists("io.papermc.paper.threadedregions.RegionizedServer")) {
            detected = "Folia";
        } else if (classExists("com.destroystokyo.paper.PaperConfig") || classExists("io.papermc.paper.configuration.PaperConfig")) {
            detected = "Paper";
        } else if (classExists("org.spigotmc.SpigotConfig")) {
            detected = "Spigot";
        } else {
            detected = "Bukkit";
        }
        this.software = detected;
    }

    public String getSoftware() {
        return this.software;
    }

    public String describe() {
        return this.software + " " + Bukkit.getBukkitVersion() + " (" + Bukkit.getVersion() + ")";
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
