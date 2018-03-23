package com.nvlad.yii2support.migrations.entities;

public class Migration {
    public String name;
    public String path;
    public MigrationStatus status;

    public Migration(String name, String path) {
        this.name = name;
        this.path = path;
        this.status = MigrationStatus.Unknown;
    }

    @Override
    public String toString() {
        return name;
    }
}
