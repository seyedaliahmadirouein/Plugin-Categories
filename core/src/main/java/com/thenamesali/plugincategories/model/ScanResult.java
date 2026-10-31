package com.thenamesali.plugincategories.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ScanResult {

    private final List<CategoryData> categories;
    private final List<JarInfo> allPlugins;
    private final int successCount;
    private final int failedCount;

    public ScanResult(List<CategoryData> categories, List<JarInfo> allPlugins, int successCount, int failedCount) {
        this.categories = Collections.unmodifiableList(new ArrayList<CategoryData>(categories));
        this.allPlugins = Collections.unmodifiableList(new ArrayList<JarInfo>(allPlugins));
        this.successCount = successCount;
        this.failedCount = failedCount;
    }

    public static ScanResult empty() {
        return new ScanResult(Collections.<CategoryData>emptyList(), Collections.<JarInfo>emptyList(), 0, 0);
    }

    public List<CategoryData> getCategories() {
        return this.categories;
    }

    public List<JarInfo> getAllPlugins() {
        return this.allPlugins;
    }

    public int getSuccessCount() {
        return this.successCount;
    }

    public int getFailedCount() {
        return this.failedCount;
    }
}
