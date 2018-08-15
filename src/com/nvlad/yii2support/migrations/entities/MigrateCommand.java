package com.nvlad.yii2support.migrations.entities;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.SmartList;
import com.nvlad.yii2support.common.YiiAlias;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MigrateCommand implements Comparable<MigrateCommand>, Cloneable {
    public boolean isDefault;
    public String command;
    public String migrationTable;
    public String db;
    public List<String> migrationPath;
    public List<String> migrationNamespaces;
    public boolean useTablePrefix;

    private List<String> myPathCache;

    public MigrateCommand() {
        isDefault = false;
        migrationPath = new SmartList<>();
        migrationNamespaces = new SmartList<>();
        useTablePrefix = false;
    }

    @Override
    public int compareTo(@NotNull MigrateCommand command) {
        if (this.isDefault) {
            return 1;
        }

        if (command.isDefault) {
            return -1;
        }

        return this.command.compareTo(command.command);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MigrateCommand)) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        MigrateCommand options = (MigrateCommand) obj;

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
    public MigrateCommand clone() {
        MigrateCommand clone = new MigrateCommand();
        clone.isDefault = isDefault;
        clone.command = command;
        clone.migrationTable = migrationTable;
        clone.db = db;
        clone.migrationPath = new SmartList<>(migrationPath);
        clone.migrationNamespaces = new SmartList<>(migrationNamespaces);
        clone.useTablePrefix = useTablePrefix;

        return clone;
    }

    public boolean containsMigration(Project project, Migration migration) {
        if (getPathCache(project).contains(migration.path)) {
            return true;
        }

        return migrationNamespaces.contains(migration.namespace);
    }

    private List<String> getPathCache(Project project) {
        if (myPathCache == null) {
            YiiAlias yiiAlias = YiiAlias.getInstance(project);
            myPathCache = new SmartList<>();
            for (String path : migrationPath) {
                String resolvedAlias = yiiAlias.resolveAlias(path, true);
                if (resolvedAlias == null) {
                    continue;
                }

                myPathCache.add(yiiAlias.resolveAlias(resolvedAlias, true));
            }
        }

        return myPathCache;
    }
}
