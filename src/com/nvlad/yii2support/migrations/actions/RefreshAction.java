package com.nvlad.yii2support.migrations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.nvlad.yii2support.migrations.MigrationsToolWindowFactory;
import com.nvlad.yii2support.migrations.commands.MigrationHistory;
import com.nvlad.yii2support.migrations.ui.ConsolePanel;

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

        ToolWindow window = ToolWindowManager
                .getInstance(project).getToolWindow(MigrationsToolWindowFactory.TOOL_WINDOW_ID);
        if (window != null) {
            Content content = window.getContentManager().getContent(1);
            if (content != null) {
                ConsolePanel consolePanel = (ConsolePanel) content.getComponent();
                migrationHistory.setConsoleView(consolePanel.getConsoleView());
            }
        }

        migrationHistory.repaintComponent(getTree());

        ApplicationManager.getApplication().executeOnPooledThread(migrationHistory);

//        ApplicationManager.getApplication().executeOnPooledThread(() -> {
//            MigrationPanel panel = (MigrationPanel) getContextComponent();
////            panel.updateMigrations();
//
//
//        });
    }
}
