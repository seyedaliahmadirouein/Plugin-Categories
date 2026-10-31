package com.thenamesali.plugincategories.sched;

import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public final class TaskRunnerFactory {

    private TaskRunnerFactory() {
    }

    public static TaskRunner create(Plugin plugin) {
        Logger logger = plugin.getLogger();
        if (classExists("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler")
                && classExists("io.papermc.paper.threadedregions.scheduler.AsyncScheduler")) {
            try {
                TaskRunner runner = (TaskRunner) Class.forName("com.thenamesali.plugincategories.folia.FoliaTaskRunner")
                        .getConstructor(Plugin.class)
                        .newInstance(plugin);
                logger.info("Scheduler: Folia compatible region schedulers");
                return runner;
            } catch (Throwable throwable) {
                logger.warning("Folia scheduler API was detected but the adapter could not be loaded, falling back to the Bukkit scheduler: " + throwable);
            }
        }
        logger.info("Scheduler: Bukkit scheduler");
        return new BukkitTaskRunner(plugin);
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
