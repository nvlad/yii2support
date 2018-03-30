package com.nvlad.yii2support.migrations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AnActionButton;
import com.nvlad.yii2support.migrations.MigrationManager;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.util.MigrationUtil;
import com.nvlad.yii2support.utils.Yii2SupportSettings;

import javax.swing.*;
import java.util.Collection;
import java.util.Map;

public class RefreshAction extends AnActionButton {
    private Map<String, Collection<Migration>> migrationMap;

    public RefreshAction() {
        super("Refresh migrations", AllIcons.Actions.Refresh);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        JTree tree = (JTree) getContextComponent();
        Project project = anActionEvent.getProject();
            Map<String, Collection<Migration>> newMigrationsMap = MigrationManager.getInstance(project).getMigrations();
//            if (!newMigrationsMap.equals(migrationMap)) {
            migrationMap = newMigrationsMap;
            boolean newestFirst = Yii2SupportSettings.getInstance(project).newestFirst;
            MigrationUtil.updateTree(tree, migrationMap, false, newestFirst);
//            }

        System.out.println("RefreshAction.actionPerformed()");
    }

    @Override
    public boolean displayTextInToolbar() {
        return false;
    }
}
