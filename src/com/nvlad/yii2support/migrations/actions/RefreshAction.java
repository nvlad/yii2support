package com.nvlad.yii2support.migrations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.nvlad.yii2support.migrations.commands.CommandBase;
import com.nvlad.yii2support.migrations.commands.MigrationHistory;
import com.nvlad.yii2support.migrations.entities.DefaultMigrateCommand;
import com.nvlad.yii2support.migrations.entities.MigrateCommand;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.services.MigrationService;
import kotlin.reflect.jvm.internal.impl.utils.SmartList;

import java.util.*;

@SuppressWarnings("ComponentNotRegistered")
public class RefreshAction extends MigrateBaseAction {
    public RefreshAction() {
        super("Refresh migrations", AllIcons.Actions.Refresh);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }

        MigrationService service = MigrationService.getInstance(project);
        service.sync();

        Map<MigrateCommand, Set<Migration>> migrateCommandMap = new HashMap<>();
        MigrateCommand defaultCommand = null;
        for (MigrateCommand command : service.getMigrationCommandMap().keySet()) {
            migrateCommandMap.put(command, new HashSet<>());
            if (command.isDefault) {
                defaultCommand = command;
            }
        }

        for (Migration migration : service.getMigrations()) {
            for (MigrateCommand command : migrateCommandMap.keySet()) {
                if (command.containsMigration(project, migration)) {
                    if (command instanceof DefaultMigrateCommand) {
                        migrateCommandMap.get(defaultCommand).add(migration);
                    } else {
                        migrateCommandMap.get(command).add(migration);
                    }
                }
            }
        }

        List<CommandBase> commands = new SmartList<>();
        for (MigrateCommand command : migrateCommandMap.keySet()) {
            if (migrateCommandMap.get(command).isEmpty()) {
                continue;
            }

            commands.add(new MigrationHistory(project, command, new ArrayList<>(migrateCommandMap.get(command))));
        }

        executeCommand(project, commands);
    }

    @Override
    public boolean isEnabled() {
        return getTree().isEnabled();
    }
}
