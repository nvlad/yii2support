package com.nvlad.yii2support.migrations.entities;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DefaultMigrateCommand extends MigrateCommand {
    public DefaultMigrateCommand(List<MigrateCommand> commands) {
        super();

        for (MigrateCommand migrateCommand : commands) {
            if (migrateCommand.isDefault) {
                command = migrateCommand.command;
                migrationTable = migrateCommand.migrationTable;
                db = migrateCommand.db;
                useTablePrefix = migrateCommand.useTablePrefix;
            }
        }
    }

    @Override
    public boolean containsMigration(Migration migration) {
        return true;
    }

    @Override
    public int compareTo(@NotNull MigrateCommand command) {
        return -1;
    }
}
