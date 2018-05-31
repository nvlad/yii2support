package com.nvlad.yii2support.migrations.entities;

import java.util.Comparator;

public class MigrationComparator implements Comparator<Migration> {
    private final boolean myNewestFirst;

    public MigrationComparator(boolean newestFirst) {
        myNewestFirst = newestFirst;
    }

    @Override
    public int compare(Migration m1, Migration m2) {
        if (myNewestFirst) {
            return m2.createdAt.compareTo(m1.createdAt);
        }

        return m1.createdAt.compareTo(m2.createdAt);
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
