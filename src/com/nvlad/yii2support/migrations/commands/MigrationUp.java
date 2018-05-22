package com.nvlad.yii2support.migrations.commands;

import com.intellij.openapi.project.Project;
import com.nvlad.yii2support.migrations.entities.Migration;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class MigrationUp extends CommandUpDownRedoBase {
    public MigrationUp(Project project, String path, @NotNull List<Migration> migrations) {
        super(project, path, migrations);
    }

    @Override
    public void run() {
        LinkedList<String> params = new LinkedList<>();
        params.add(String.valueOf(myMigrations.size()));
        fillParams(params);
        params.add("--migrationPath=" + myPath);
        params.add("--interactive=0");

        executeCommandWithParams("migrate/up", params);
    }
}
