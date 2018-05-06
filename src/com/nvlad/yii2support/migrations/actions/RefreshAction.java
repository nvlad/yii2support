package com.nvlad.yii2support.migrations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.nvlad.yii2support.migrations.MigrationManager;
import com.nvlad.yii2support.migrations.commands.MigrationHistory;
import com.nvlad.yii2support.migrations.util.MigrationUtil;
import com.nvlad.yii2support.utils.Yii2SupportSettings;

import javax.swing.*;

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

        MigrationManager migrationManager = MigrationManager.getInstance(project);
        migrationManager.refresh();

        JTree tree = getTree();
        Yii2SupportSettings settings = Yii2SupportSettings.getInstance(project);
        MigrationManager manager = MigrationManager.getInstance(project);
        MigrationUtil.updateTree(tree, manager.getMigrations(), settings.newestFirst);

        MigrationHistory migrationHistory = new MigrationHistory(project);
        executeCommand(project, migrationHistory);
    }

    @Override
    public boolean isEnabled() {
        return getTree().isEnabled();
    }
}
