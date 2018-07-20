package com.nvlad.yii2support.migrations.entities;

import com.intellij.util.SmartList;

import java.io.Serializable;
import java.util.List;

public class MigrateCommandOptions implements Serializable {
    public boolean isDefault;
    public String command;
    public String migrationTable;
    public String db;
    public List<String> migrationPath;
    public List<String> migrationNamespaces;
    public boolean useTablePrefix;

    public MigrateCommandOptions() {
        isDefault = false;
        migrationPath = new SmartList<>();
        migrationNamespaces = new SmartList<>();
        useTablePrefix = false;
    }
}
