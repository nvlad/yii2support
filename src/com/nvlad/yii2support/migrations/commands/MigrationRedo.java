package com.nvlad.yii2support.migrations.commands;

import com.intellij.openapi.project.Project;
import com.nvlad.yii2support.migrations.entities.MigrateCommand;
import com.nvlad.yii2support.migrations.entities.Migration;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class MigrationRedo extends CommandUpDownRedoBase {
    public MigrationRedo(@NotNull Project project, @NotNull List<Migration> migrations, @NotNull MigrateCommand command, String path) {
        super(project, migrations, command, path);
        direction = "reverting";
    }

    @Override
    public void run() {
        LinkedList<String> params = new LinkedList<>();
        params.add(String.valueOf(myMigrations.size()));
        prepareCommandParams(params, myCommand, myPath);
        executeActionWithParams("redo", params);
    }
}
