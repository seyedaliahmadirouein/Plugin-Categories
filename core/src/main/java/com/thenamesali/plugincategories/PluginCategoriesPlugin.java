package com.thenamesali.plugincategories;

import com.thenamesali.plugincategories.chat.ChatRenderer;
import com.thenamesali.plugincategories.chat.ChatRendererFactory;
import com.thenamesali.plugincategories.command.PluginCategoriesCommand;
import com.thenamesali.plugincategories.listener.PlayerCommandListener;
import com.thenamesali.plugincategories.navigation.NavigationHandler;
import com.thenamesali.plugincategories.scan.ScanManager;
import com.thenamesali.plugincategories.sched.TaskRunner;
import com.thenamesali.plugincategories.sched.TaskRunnerFactory;
import com.thenamesali.plugincategories.view.ViewService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginCategoriesPlugin extends JavaPlugin {

    private Settings settings;
    private TaskRunner taskRunner;
    private ChatRenderer chatRenderer;
    private ScanManager scanManager;
    private ViewService viewService;
    private NavigationHandler navigationHandler;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.settings = new Settings(this);
        this.settings.load();
        ServerEnvironment environment = new ServerEnvironment();
        getLogger().info("Server detected: " + environment.describe());
        this.chatRenderer = ChatRendererFactory.create(this);
        this.taskRunner = TaskRunnerFactory.create(this);
        this.scanManager = new ScanManager(this, this.settings, this.taskRunner);
        this.viewService = new ViewService(this.settings, this.chatRenderer, this.scanManager);
        this.navigationHandler = new NavigationHandler(this.settings, this.scanManager, this.viewService);
        getServer().getPluginManager().registerEvents(new PlayerCommandListener(this.settings, this.navigationHandler), this);
        PluginCommand command = getCommand("plugincategories");
        if (command != null) {
            PluginCategoriesCommand executor = new PluginCategoriesCommand(this.navigationHandler, this.settings, this.scanManager);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        } else {
            getLogger().severe("Command plugincategories is missing from plugin.yml");
        }
        this.scanManager.startupScan();
        getLogger().info("Plugin Categories " + getDescription().getVersion() + " enabled");
    }

    public Settings getSettings() {
        return this.settings;
    }

    public TaskRunner getTaskRunner() {
        return this.taskRunner;
    }

    public ChatRenderer getChatRenderer() {
        return this.chatRenderer;
    }

    public ScanManager getScanManager() {
        return this.scanManager;
    }

    public ViewService getViewService() {
        return this.viewService;
    }

    public NavigationHandler getNavigationHandler() {
        return this.navigationHandler;
    }

    public void debug(String message) {
        if (this.settings != null && this.settings.isDebugEnabled()) {
            getLogger().info("[DEBUG] " + message);
        }
    }
}
