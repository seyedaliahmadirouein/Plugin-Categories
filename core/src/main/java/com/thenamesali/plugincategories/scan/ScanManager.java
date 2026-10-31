package com.thenamesali.plugincategories.scan;

import com.thenamesali.plugincategories.PluginCategoriesPlugin;
import com.thenamesali.plugincategories.Settings;
import com.thenamesali.plugincategories.model.CategoryData;
import com.thenamesali.plugincategories.model.JarInfo;
import com.thenamesali.plugincategories.model.PluginState;
import com.thenamesali.plugincategories.model.ScanResult;
import com.thenamesali.plugincategories.sched.TaskRunner;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ScanManager {

    private final PluginCategoriesPlugin plugin;
    private final Settings settings;
    private final TaskRunner taskRunner;
    private final AtomicBoolean scanning = new AtomicBoolean(false);
    private volatile ScanResult cache = ScanResult.empty();
    private volatile boolean ready = false;
    private volatile long lastScanFinished = 0L;

    public ScanManager(PluginCategoriesPlugin plugin, Settings settings, TaskRunner taskRunner) {
        this.plugin = plugin;
        this.settings = settings;
        this.taskRunner = taskRunner;
    }

    public ScanResult latest() {
        return this.cache;
    }

    public boolean isReady() {
        return this.ready;
    }

    public void startupScan() {
        try {
            this.taskRunner.runGlobalDelayed(1L, () -> requestScan(true));
        } catch (Throwable throwable) {
            requestScan(true);
        }
    }

    public void requestScan(boolean force) {
        if (!this.plugin.isEnabled()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (!force && now - this.lastScanFinished < this.settings.getRescanIntervalMillis()) {
            return;
        }
        if (!this.scanning.compareAndSet(false, true)) {
            return;
        }
        try {
            this.taskRunner.runGlobal(() -> {
                List<PluginSnapshot> snapshot = collectSnapshot();
                this.taskRunner.runAsync(() -> {
                    try {
                        publish(performScan(snapshot));
                    } catch (Throwable throwable) {
                        this.plugin.getLogger().warning("Plugin scan failed: " + throwable);
                    } finally {
                        this.lastScanFinished = System.currentTimeMillis();
                        this.scanning.set(false);
                    }
                });
            });
        } catch (Throwable throwable) {
            this.scanning.set(false);
            this.plugin.getLogger().warning("Failed to schedule plugin scan: " + throwable);
        }
    }

    private List<PluginSnapshot> collectSnapshot() {
        List<PluginSnapshot> snapshot = new ArrayList<PluginSnapshot>();
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            String name;
            try {
                name = plugin.getDescription().getName();
            } catch (Throwable throwable) {
                name = plugin.getName();
            }
            snapshot.add(new PluginSnapshot(name, plugin.isEnabled(), pluginFile(plugin)));
        }
        return snapshot;
    }

    private File pluginFile(Plugin plugin) {
        try {
            java.security.ProtectionDomain domain = plugin.getClass().getProtectionDomain();
            if (domain != null && domain.getCodeSource() != null && domain.getCodeSource().getLocation() != null) {
                return new File(domain.getCodeSource().getLocation().toURI());
            }
        } catch (Throwable ignored) {
        }
        try {
            if (plugin instanceof org.bukkit.plugin.java.JavaPlugin) {
                Method method = org.bukkit.plugin.java.JavaPlugin.class.getDeclaredMethod("getFile");
                method.setAccessible(true);
                return (File) method.invoke(plugin);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private ScanResult performScan(List<PluginSnapshot> snapshot) {
        File folder = pluginsFolder();
        Map<String, PluginSnapshot> byPath = new HashMap<String, PluginSnapshot>();
        Map<String, PluginSnapshot> byName = new HashMap<String, PluginSnapshot>();
        for (PluginSnapshot entry : snapshot) {
            if (entry.getFile() != null) {
                byPath.put(canonicalKey(entry.getFile()), entry);
            }
            String key = entry.getName().toLowerCase(Locale.ROOT);
            if (!byName.containsKey(key)) {
                byName.put(key, entry);
            }
        }
        List<File> rootJars = new ArrayList<File>();
        List<CategoryData> categories = new ArrayList<CategoryData>();
        List<JarInfo> rootInfos = new ArrayList<JarInfo>();
        List<JarInfo> categoryInfos = new ArrayList<JarInfo>();
        List<File> allJarFiles = new ArrayList<File>();
        Map<File, String> nameCache = new HashMap<File, String>();
        File[] entries = folder.listFiles();
        if (entries != null) {
            for (File entry : entries) {
                if (entry.isFile()) {
                    if (isJar(entry)) {
                        rootJars.add(entry);
                    }
                } else if (entry.isDirectory() && this.settings.isCategoryDetection() && !isExcluded(entry)) {
                    List<JarInfo> jars = new ArrayList<JarInfo>();
                    Map<String, JarInfo> dedupe = new LinkedHashMap<String, JarInfo>();
                    File[] children = entry.listFiles();
                    if (children != null) {
                        for (File child : children) {
                            if (child.isFile() && isJar(child)) {
                                JarInfo info = classify(child, byPath, byName, nameCache);
                                String key = info.getName().toLowerCase(Locale.ROOT);
                                if (!dedupe.containsKey(key)) {
                                    dedupe.put(key, info);
                                }
                            }
                        }
                    }
                    jars.addAll(dedupe.values());
                    if (!jars.isEmpty()) {
                        jars.sort(Comparator.comparing(JarInfo::getName, String.CASE_INSENSITIVE_ORDER));
                        categories.add(new CategoryData(cleanName(entry.getName()), jars));
                        for (JarInfo info : jars) {
                            categoryInfos.add(info);
                            allJarFiles.add(info.getFile());
                        }
                    }
                }
            }
        } else {
            this.plugin.debug("Plugins folder could not be read: " + folder.getAbsolutePath());
        }
        for (File jar : rootJars) {
            rootInfos.add(classify(jar, byPath, byName, nameCache));
            allJarFiles.add(jar);
        }
        categories.sort(Comparator.comparing(CategoryData::getName, String.CASE_INSENSITIVE_ORDER));
        int successCount = 0;
        for (PluginSnapshot entry : snapshot) {
            if (entry.isEnabled()) {
                successCount++;
            }
        }
        int failedCount = 0;
        for (JarInfo info : rootInfos) {
            if (!info.isSuccessful()) {
                failedCount++;
            }
        }
        for (JarInfo info : categoryInfos) {
            if (!info.isSuccessful()) {
                failedCount++;
            }
        }
        Set<String> scannedPaths = new HashSet<String>();
        for (File jar : allJarFiles) {
            scannedPaths.add(canonicalKey(jar));
        }
        for (PluginSnapshot entry : snapshot) {
            if (!entry.isEnabled() && (entry.getFile() == null || !scannedPaths.contains(canonicalKey(entry.getFile())))) {
                failedCount++;
            }
        }
        Map<String, JarInfo> merged = new LinkedHashMap<String, JarInfo>();
        for (PluginSnapshot entry : snapshot) {
            merged.put(entry.getName().toLowerCase(Locale.ROOT),
                    new JarInfo(entry.getName(), entry.isEnabled() ? PluginState.ENABLED : PluginState.DISABLED, entry.getFile()));
        }
        for (JarInfo info : rootInfos) {
            if (!merged.containsKey(info.getName().toLowerCase(Locale.ROOT))) {
                merged.put(info.getName().toLowerCase(Locale.ROOT), info);
            }
        }
        for (JarInfo info : categoryInfos) {
            if (!merged.containsKey(info.getName().toLowerCase(Locale.ROOT))) {
                merged.put(info.getName().toLowerCase(Locale.ROOT), info);
            }
        }
        List<JarInfo> allPlugins = new ArrayList<JarInfo>(merged.values());
        allPlugins.sort(Comparator.comparing(JarInfo::getName, String.CASE_INSENSITIVE_ORDER));
        this.plugin.debug("Scanned " + folder.getAbsolutePath() + ": " + snapshot.size() + " loaded plugins, "
                + allJarFiles.size() + " jar files, " + categories.size() + " categories");
        return new ScanResult(categories, allPlugins, successCount, failedCount);
    }

    private JarInfo classify(File jar, Map<String, PluginSnapshot> byPath, Map<String, PluginSnapshot> byName, Map<File, String> nameCache) {
        PluginSnapshot matched = byPath.get(canonicalKey(jar));
        if (matched == null) {
            String jarName = readName(jar, nameCache);
            if (jarName != null) {
                matched = byName.get(jarName.toLowerCase(Locale.ROOT));
            }
        }
        if (matched != null) {
            return new JarInfo(matched.getName(), matched.isEnabled() ? PluginState.ENABLED : PluginState.DISABLED, jar);
        }
        String jarName = readName(jar, nameCache);
        if (jarName != null) {
            return new JarInfo(jarName, PluginState.NOT_LOADED, jar);
        }
        String base = jar.getName();
        int dot = base.lastIndexOf('.');
        if (dot > 0) {
            base = base.substring(0, dot);
        }
        return new JarInfo(base, PluginState.INVALID, jar);
    }

    private String readName(File jar, Map<File, String> cache) {
        if (cache.containsKey(jar)) {
            return cache.get(jar);
        }
        String name = JarNameReader.readName(jar);
        cache.put(jar, name);
        return name;
    }

    private boolean isExcluded(File folder) {
        String name = folder.getName().toLowerCase(Locale.ROOT);
        String ownDataFolder = this.plugin.getDataFolder().getName().toLowerCase(Locale.ROOT);
        if (name.equals(ownDataFolder)) {
            return true;
        }
        return this.settings.getExcludedFolders().contains(name);
    }

    private boolean isJar(File file) {
        return file.getName().toLowerCase(Locale.ROOT).endsWith(".jar");
    }

    private String canonicalKey(File file) {
        try {
            return file.getCanonicalPath().toLowerCase(Locale.ROOT);
        } catch (IOException exception) {
            return file.getAbsolutePath().toLowerCase(Locale.ROOT);
        }
    }

    private String cleanName(String raw) {
        String cleaned = raw.replace("\u00A7", "").trim();
        return cleaned.isEmpty() ? raw.trim() : cleaned;
    }

    private File pluginsFolder() {
        File dataFolder = this.plugin.getDataFolder();
        if (dataFolder != null) {
            File parent = dataFolder.getParentFile();
            if (parent != null && parent.isDirectory()) {
                return parent;
            }
        }
        return new File("plugins");
    }

    private void publish(ScanResult result) {
        this.cache = result;
        this.ready = true;
        this.plugin.debug("Scan finished: " + result.getSuccessCount() + " enabled, " + result.getFailedCount()
                + " failed, " + result.getCategories().size() + " categories");
    }
}
