package com.thenamesali.plugincategories.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CategoryData {

    private final String name;
    private final List<JarInfo> jars;

    public CategoryData(String name, List<JarInfo> jars) {
        this.name = name;
        this.jars = Collections.unmodifiableList(new ArrayList<JarInfo>(jars));
    }

    public String getName() {
        return this.name;
    }

    public List<JarInfo> getJars() {
        return this.jars;
    }
}
