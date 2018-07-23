package com.nvlad.yii2support.migrations.entities;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class MigrateCommandOptions implements Comparable<MigrateCommandOptions>, Cloneable {
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

    @Override
    public int compareTo(@NotNull MigrateCommandOptions o) {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MigrateCommandOptions)) {
            return false;
        }

        MigrateCommandOptions options = (MigrateCommandOptions) obj;

        return options.isDefault == isDefault
                && options.useTablePrefix == useTablePrefix
                && StringUtil.equals(options.command, command)
                && StringUtil.equals(options.migrationTable, migrationTable)
                && StringUtil.equals(options.db, db)
                && options.migrationPath.equals(migrationPath)
                && options.migrationNamespaces.equals(migrationNamespaces);
    }

    @Override
    public int hashCode() {
        return (isDefault ? 1 : 0)
                + command.hashCode()
                + migrationTable.hashCode()
                + db.hashCode()
                + migrationPath.hashCode()
                + migrationNamespaces.hashCode()
                + (useTablePrefix ? 1 : 0);
    }

    @Override
    public MigrateCommandOptions clone() {
        MigrateCommandOptions clone = new MigrateCommandOptions();
        clone.isDefault = isDefault;
        clone.command = command;
        clone.migrationTable = migrationTable;
        clone.db = db;
        clone.migrationPath = new SmartList<>(migrationPath);
        clone.migrationNamespaces = new SmartList<>(migrationNamespaces);
        clone.useTablePrefix = useTablePrefix;

        return clone;

    }
}
