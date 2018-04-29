package com.nvlad.yii2support.migrations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.nvlad.yii2support.migrations.commands.MigrationHistory;
import com.nvlad.yii2support.migrations.ui.MigrationPanel;

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

        MigrationHistory migrationHistory = new MigrationHistory(project);
        executeCommand(project, migrationHistory);
    }

    @Override
    public boolean isEnabled() {
        return getTree().isEnabled();
    }
}
