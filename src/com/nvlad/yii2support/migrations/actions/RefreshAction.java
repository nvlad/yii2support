package com.nvlad.yii2support.migrations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AnActionButton;
import com.nvlad.yii2support.migrations.MigrationManager;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.MigrationStatus;
import com.nvlad.yii2support.migrations.ui.MigrationPanel;
import com.nvlad.yii2support.migrations.util.MigrationUtil;
import com.nvlad.yii2support.utils.Yii2SupportSettings;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

@SuppressWarnings("ComponentNotRegistered")
public class RefreshAction extends AnActionButton {
    public RefreshAction() {
        super("Refresh migrations", AllIcons.Actions.Refresh);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        MigrationPanel panel = (MigrationPanel) getContextComponent();
        Map<String, Collection<Migration>> migrationMap = panel.getMigrationMap();
        Map<String, Collection<Migration>> newMigrationsMap = MigrationManager.getInstance(project).getMigrations();
        boolean newestFirst = Yii2SupportSettings.getInstance(project).newestFirst;
        if (true || !newMigrationsMap.equals(migrationMap)) {
            panel.setMigrationMap(newMigrationsMap);
            MigrationUtil.updateTree(panel.getTree(), newMigrationsMap, false, newestFirst);
        }

        final Map<String, Date> history = MigrationManager.getInstance(project).migrateHistory();
        if (history == null) {
            return;
        }

        migrationMap = panel.getMigrationMap();
        migrationMap.forEach((path, migrations) -> {
            for (Migration migration : migrations) {
                migration.status = MigrationStatus.NotApply;
                if (history.containsKey(migration.name)) {
                    migration.status = MigrationStatus.Success;
                    migration.applyAt = history.get(migration.name);
                }
            }
        });

        MigrationUtil.updateTree(panel.getTree(), migrationMap, false, newestFirst);
    }

    @Override
    public boolean displayTextInToolbar() {
        return false;
    }
}
