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
import com.nvlad.yii2support.migrations.util.MigrationUtil;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class MigrationsToolWindowFactory implements ToolWindowFactory {
    public static final String TOOL_WINDOW_ID = "Migrations";

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MigrationPanel migrationPanel = new MigrationPanel(project, toolWindow);
        ConsolePanel consolePanel = new ConsolePanel(project);

        ((ToolWindowManagerEx) ToolWindowManager.getInstance(project))
                .addToolWindowManagerListener(new MigrationToolWindowManagerListener(project, migrationPanel.getTree()));

        Content navigator = ContentFactory.SERVICE.getInstance().createContent(migrationPanel, "Explorer", false);
        toolWindow.getContentManager().addContent(navigator);

        Content console = ContentFactory.SERVICE.getInstance().createContent(consolePanel, "Output", false);
        toolWindow.getContentManager().addContent(console);


        project.getBaseDir().getFileSystem().addVirtualFileListener(new MigrationsVirtualFileMonitor(project));
//        project.getBaseDir().getFileSystem().addVirtualFileListener(new VirtualFileAdapter() {
//            @Override
//            public void fileCreated(@NotNull VirtualFileEvent event) {
//                PsiFile psiFile = PsiManager.getInstance(project).findFile(event.getFile());
//                if (psiFile instanceof PhpFile) {
//                    ToolWindow window = ToolWindowManager.getInstance(project).getToolWindow(MigrationsToolWindowFactory.TOOL_WINDOW_ID);
//                    if (window.isVisible()) {
//                        myMigrationPanel.updateMigrations();
//                    }
//                }
//            }
//
//            @Override
//            public void fileDeleted(@NotNull VirtualFileEvent event) {
////                myMigrationPanel.updateMigrations();
//            }
//
//            @Override
//            public void fileMoved(@NotNull VirtualFileMoveEvent event) {
////                myMigrationPanel.updateMigrations();
//            }
//
//            @Override
//            public void fileCopied(@NotNull VirtualFileCopyEvent event) {
////                updateMigrations();
//            }
//
//            private void updateMigrations() {
////                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
////                executor.schedule(myMigrationPanel::updateMigrations, 1, TimeUnit.SECONDS);
//            }
//        });
    }

    class MigrationToolWindowManagerListener implements ToolWindowManagerListener {
        private final Project myProject;
        private final JTree myTree;

        private boolean myToolWindowVisible = true;
        private boolean myToolWindowActive = true;
        private boolean myFirstShow = true;

        MigrationToolWindowManagerListener(Project project, JTree tree) {
            myProject = project;
            myTree = tree;
        }

        @Override
        public void toolWindowRegistered(@NotNull String s) {
        }

        @Override
        public void stateChanged() {
            ToolWindow window = ToolWindowManager.getInstance(myProject).getToolWindow(MigrationsToolWindowFactory.TOOL_WINDOW_ID);
            if (window == null) {
                return;
            }

            boolean toolWindowVisible = window.isVisible();
            boolean toolWindowActive = window.isActive();

            if (myFirstShow) {
                myToolWindowVisible = !toolWindowVisible;
                myFirstShow = false;
            }

            if (myToolWindowVisible != toolWindowVisible || myToolWindowActive != toolWindowActive) {
                myToolWindowVisible = toolWindowVisible;
                myToolWindowActive = toolWindowActive;

                if (myToolWindowVisible && myToolWindowActive) {
                    updateMigrations();
                }
            }
        }

        private void updateMigrations() {
            boolean newestFirst = Yii2SupportSettings.getInstance(myProject).newestFirst;
            MigrationManager manager = MigrationManager.getInstance(myProject);

            manager.refresh();
            MigrationUtil.updateTree(myTree, manager.getMigrations(), newestFirst);
        }
    }
}
