package com.nvlad.yii2support.migrations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.nvlad.yii2support.migrations.ui.toolWindow.ConsolePanel;
import com.nvlad.yii2support.migrations.ui.toolWindow.MigrationPanel;
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
    }

    class MigrationToolWindowManagerListener implements ToolWindowManagerListener {
        private final Project myProject;
        private final JTree myTree;
        private final MigrationsVirtualFileMonitor fileMonitor;
        private boolean myToolWindowVisible = true;

        MigrationToolWindowManagerListener(Project project, JTree tree) {
            myProject = project;
            myTree = tree;
            fileMonitor = new MigrationsVirtualFileMonitor(project, myTree);

            project.getBaseDir().getFileSystem().addVirtualFileListener(fileMonitor);
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
            if (myToolWindowVisible != toolWindowVisible) {
                VirtualFileSystem fileSystem = myProject.getBaseDir().getFileSystem();
                if (myToolWindowVisible) {
                    fileSystem.removeVirtualFileListener(fileMonitor);
                } else {
                    MigrationService service = MigrationService.getInstance(myProject);
                    service.refresh();
                    Yii2SupportSettings settings = Yii2SupportSettings.getInstance(myProject);
                    MigrationUtil.updateTree(myTree, service.getMigrations(), settings.newestFirst);

                    fileSystem.addVirtualFileListener(fileMonitor);
                }

                myToolWindowVisible = toolWindowVisible;
            }
        }
    }
}
