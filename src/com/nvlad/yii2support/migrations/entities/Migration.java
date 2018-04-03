package com.nvlad.yii2support.migrations.entities;

import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.nvlad.yii2support.migrations.util.MigrationUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class Migration implements Comparable<Migration> {
    public PhpClass migrationClass;
    public String name;
    public String path;
    public MigrationStatus status;
    public Date createdAt;

    public Migration(PhpClass clazz, String path) {
        this.migrationClass = clazz;
        this.name = clazz.getName();
        this.path = path;
        this.status = MigrationStatus.Unknown;
        createdAt = MigrationUtil.createDateFromName(this.name);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(@NotNull Migration migration) {
        return createdAt.compareTo(migration.createdAt);
    }
}
