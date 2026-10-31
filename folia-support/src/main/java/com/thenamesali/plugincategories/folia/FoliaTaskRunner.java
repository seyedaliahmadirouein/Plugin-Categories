package com.thenamesali.plugincategories.folia;

import com.thenamesali.plugincategories.sched.TaskRunner;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class FoliaTaskRunner implements TaskRunner {

    private final Plugin plugin;

    public FoliaTaskRunner(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runAsync(Runnable task) {
        AsyncScheduler scheduler = Bukkit.getAsyncScheduler();
        scheduler.runNow(this.plugin, scheduledTask -> task.run());
    }

    @Override
    public void runGlobal(Runnable task) {
        GlobalRegionScheduler scheduler = Bukkit.getGlobalRegionScheduler();
        scheduler.run(this.plugin, scheduledTask -> task.run());
    }

    @Override
    public void runGlobalDelayed(long delayTicks, Runnable task) {
        GlobalRegionScheduler scheduler = Bukkit.getGlobalRegionScheduler();
        scheduler.runDelayed(this.plugin, scheduledTask -> task.run(), Math.max(1L, delayTicks));
    }
}
