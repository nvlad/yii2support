package com.nvlad.yii2support.migrations.ui.toolWindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.nvlad.yii2support.migrations.services.MigrationService;
import com.nvlad.yii2support.migrations.services.MigrationServiceListener;
import com.nvlad.yii2support.migrations.services.MigrationsVirtualFileMonitor;
import com.nvlad.yii2support.migrations.util.TreeUtil;
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
        private final MigrationsVirtualFileMonitor fileMonitor;
        private final VirtualFileSystem fileSystem;
        private final MigrationService service;
        private final MigrationServiceListener serviceListener;
        private boolean myToolWindowVisible = true;

        MigrationToolWindowManagerListener(Project project, JTree tree) {
            myProject = project;
            fileMonitor = new MigrationsVirtualFileMonitor(project);
            fileSystem = myProject.getBaseDir().getFileSystem();
            service = MigrationService.getInstance(project);
            serviceListener = new ServiceListener(tree, project);
        }

        @Override
        public void toolWindowRegistered(@NotNull String s) {
        }

        @Override
        public void stateChanged(@NotNull ToolWindowManager toolWindowManager) {
            ToolWindow window = ToolWindowManager
                    .getInstance(myProject)
                    .getToolWindow(MigrationsToolWindowFactory.TOOL_WINDOW_ID);
            if (window == null) {
                return;
            }

            boolean toolWindowVisible = window.isVisible();
            if (myToolWindowVisible != toolWindowVisible) {
                if (myToolWindowVisible) {
                    fileSystem.removeVirtualFileListener(fileMonitor);
                    service.removeListener(serviceListener);
                } else {
                    service.addListener(serviceListener);
                    fileSystem.addVirtualFileListener(fileMonitor);

                    ApplicationManager.getApplication().executeOnPooledThread(service::sync);
                }

                myToolWindowVisible = toolWindowVisible;
            }
        }
    }

    class ServiceListener implements MigrationServiceListener {
        private final JTree myTree;
        private final MigrationService service;
        private final Yii2SupportSettings settings;

        ServiceListener(JTree tree, Project project) {
            myTree = tree;

            service = MigrationService.getInstance(project);
            settings = Yii2SupportSettings.getInstance(project);
        }

        @Override
        public void treeChanged() {
            TreeUtil.updateTree(myTree, service.getMigrationCommandMap(), settings.newestFirst);
        }
    }
}
