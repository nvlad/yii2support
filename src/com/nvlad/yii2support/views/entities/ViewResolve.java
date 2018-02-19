package com.nvlad.yii2support.views.entities;

public class ViewResolve {
    public String key;
    public String application;
    public String module;
    public String theme;
    public ViewResolveFrom from;
    public String relativePath;

    public ViewResolve() {
    }

    public ViewResolve(String key) {
        this.key = key;
    }

    public ViewResolve(ViewResolveFrom from) {
        this.from = from;
    }
}
