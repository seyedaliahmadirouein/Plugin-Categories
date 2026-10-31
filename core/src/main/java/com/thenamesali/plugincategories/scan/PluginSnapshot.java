package com.thenamesali.plugincategories.scan;

import java.io.File;

public final class PluginSnapshot {

    private final String name;
    private final boolean enabled;
    private final File file;

    public PluginSnapshot(String name, boolean enabled, File file) {
        this.name = name;
        this.enabled = enabled;
        this.file = file;
    }

    public String getName() {
        return this.name;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public File getFile() {
        return this.file;
    }
}
