package com.thenamesali.plugincategories.model;

import java.io.File;

public final class JarInfo {

    private final String name;
    private final PluginState state;
    private final File file;

    public JarInfo(String name, PluginState state, File file) {
        this.name = name;
        this.state = state;
        this.file = file;
    }

    public String getName() {
        return this.name;
    }

    public PluginState getState() {
        return this.state;
    }

    public File getFile() {
        return this.file;
    }

    public boolean isSuccessful() {
        return this.state == PluginState.ENABLED;
    }
}
