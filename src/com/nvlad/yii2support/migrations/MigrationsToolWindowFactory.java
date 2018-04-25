package com.nvlad.yii2support.migrations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.nvlad.yii2support.migrations.ui.ConsolePanel;
import com.nvlad.yii2support.migrations.ui.MigrationPanel;
import org.jetbrains.annotations.NotNull;

public class MigrationsToolWindowFactory implements ToolWindowFactory {
    public static final String TOOL_WINDOW_ID = "Migrations";

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MigrationPanel migrationPanel = new MigrationPanel(project, toolWindow);
        Content navigator = ContentFactory.SERVICE.getInstance().createContent(migrationPanel, "Navigator", false);
        toolWindow.getContentManager().addContent(navigator);

        ConsolePanel consolePanel = new ConsolePanel(project);
        Content console = ContentFactory.SERVICE.getInstance().createContent(consolePanel, "Output", false);
        toolWindow.getContentManager().addContent(console);

        MigrationManager.getInstance(project).setConsoleView(consolePanel.getConsoleView());

        ((ToolWindowManagerEx) ToolWindowManager.getInstance(project)).addToolWindowManagerListener(new ToolWindowManagerListener() {
            private boolean myToolWindowVisible = true;
            private boolean myToolWindowActive = true;

            @Override
            public void toolWindowRegistered(@NotNull String s) {
            }

            @Override
            public void stateChanged() {
                ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow(MigrationsToolWindowFactory.TOOL_WINDOW_ID);
                boolean toolWindowVisible = window.isVisible();
                boolean toolWindowActive = window.isActive();

                if (myToolWindowVisible != toolWindowVisible || myToolWindowActive != toolWindowActive) {
                    myToolWindowVisible = toolWindowVisible;
                    myToolWindowActive = toolWindowActive;

                    if (myToolWindowVisible && myToolWindowActive) {
                        migrationPanel.updateMigrations();
                    }
                }
            }
        });

//        project.getBaseDir().getFileSystem().addVirtualFileListener(new VirtualFileAdapter() {
//            @Override
//            public void fileCreated(@NotNull VirtualFileEvent event) {
//                PsiFile psiFile = PsiManager.getInstance(project).findFile(event.getFile());
//                if (psiFile instanceof PhpFile) {
//                    ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow(MigrationsToolWindowFactory.TOOL_WINDOW_ID);
//                    if (window.isVisible()) {
//                        migrationPanel.updateMigrations();
//                    }
//                }
//            }
//
//            @Override
//            public void fileDeleted(@NotNull VirtualFileEvent event) {
////                migrationPanel.updateMigrations();
//            }
//
//            @Override
//            public void fileMoved(@NotNull VirtualFileMoveEvent event) {
////                migrationPanel.updateMigrations();
//            }
//
//            @Override
//            public void fileCopied(@NotNull VirtualFileCopyEvent event) {
////                updateMigrations();
//            }
//
//            private void updateMigrations() {
////                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
////                executor.schedule(migrationPanel::updateMigrations, 1, TimeUnit.SECONDS);
//            }
//        });
    }
}
