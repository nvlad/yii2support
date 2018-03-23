package com.nvlad.yii2support.migrations.entities;

import com.jetbrains.php.lang.psi.elements.PhpClass;

public class Migration {
    public PhpClass migrationClass;
    public String name;
    public String path;
    public MigrationStatus status;

    public Migration(PhpClass clazz, String path) {
        this.migrationClass = clazz;
        this.name = clazz.getName();
        this.path = path;
        this.status = MigrationStatus.Unknown;
    }

    @Override
    public String toString() {
        return name;
    }
}
