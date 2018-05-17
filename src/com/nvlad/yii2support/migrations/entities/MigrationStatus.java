package com.nvlad.yii2support.migrations.entities;

public enum MigrationStatus {
    Progress,
    Unknown,
    NotApply,
    Success,
    ApplyError,
    RollbackError,
}
