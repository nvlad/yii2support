package com.nvlad.yii2support.migrations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.nvlad.yii2support.migrations.commands.MigrationHistory;
import com.nvlad.yii2support.migrations.services.MigrationService;
import com.nvlad.yii2support.migrations.ui.toolWindow.MigrationPanel;

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

        MigrationPanel panel = getPanel();
        panel.updateTree();
        panel.updateUI();

        MigrationHistory migrationHistory = new MigrationHistory(project);
        executeCommand(project, migrationHistory);
    }

    @Override
    public boolean isEnabled() {
        return getTree().isEnabled();
    }
}
