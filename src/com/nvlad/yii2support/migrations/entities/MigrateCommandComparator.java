package com.nvlad.yii2support.migrations.entities;

import java.util.Comparator;

public class MigrateCommandComparator implements Comparator<MigrateCommand> {
    @Override
    public int compare(MigrateCommand o1, MigrateCommand o2) {
        if (o1.isDefault || o2 instanceof DefaultMigrateCommand) {
            return -1;
        }

        if (o2.isDefault || o1 instanceof DefaultMigrateCommand) {
            return 1;
        }

        return o1.command.compareTo(o2.command);
    }
}
