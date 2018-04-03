package com.nvlad.yii2support.migrations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileCopyEvent;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.nvlad.yii2support.migrations.ui.MigrationPanel;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MigrationsToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MigrationPanel migrationPanel = new MigrationPanel(project, toolWindow);
        Content content = ContentFactory.SERVICE.getInstance().createContent(migrationPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        project.getBaseDir().getFileSystem().addVirtualFileListener(new VirtualFileAdapter() {
            @Override
            public void fileCreated(@NotNull VirtualFileEvent event) {
                updateMigrations();
            }

            @Override
            public void fileDeleted(@NotNull VirtualFileEvent event) {
//                migrationPanel.updateMigrations();
            }

            @Override
            public void fileMoved(@NotNull VirtualFileMoveEvent event) {
//                migrationPanel.updateMigrations();
            }

            @Override
            public void fileCopied(@NotNull VirtualFileCopyEvent event) {
//                updateMigrations();
            }

            private void updateMigrations() {
//                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
//                executor.schedule(migrationPanel::updateMigrations, 1, TimeUnit.SECONDS);
            }
        });
    }
}
