package com.nvlad.yii2support.migrations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.nvlad.yii2support.migrations.ui.MigrationPanel;
import org.jetbrains.annotations.NotNull;

public class MigrationsToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MigrationPanel migrationPanel = new MigrationPanel(project);
        Content content = ContentFactory.SERVICE.getInstance().createContent(migrationPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
