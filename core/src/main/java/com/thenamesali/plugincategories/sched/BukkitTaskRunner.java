package com.thenamesali.plugincategories.sched;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class BukkitTaskRunner implements TaskRunner {

    private final Plugin plugin;

    public BukkitTaskRunner(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runAsync(Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, task);
    }

    @Override
    public void runGlobal(Runnable task) {
        Bukkit.getScheduler().runTask(this.plugin, task);
    }

    @Override
    public void runGlobalDelayed(long delayTicks, Runnable task) {
        Bukkit.getScheduler().runTaskLater(this.plugin, task, delayTicks);
    }
}
