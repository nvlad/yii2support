package com.nvlad.yii2support.migrations.actions;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.content.Content;
import com.nvlad.yii2support.migrations.MigrationsToolWindowFactory;
import com.nvlad.yii2support.migrations.commands.CommandBase;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.MigrationStatus;
import com.nvlad.yii2support.migrations.ui.ConsolePanel;
import com.nvlad.yii2support.migrations.ui.MigrationPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.Enumeration;

abstract class MigrateBaseAction extends AnActionButton {
    MigrateBaseAction(String name, Icon icon) {
        super(name, icon);
    }

    @NotNull
    MigrationPanel getPanel() {
        return (MigrationPanel) getContextComponent();
    }

    @NotNull
    JTree getTree() {
        return getPanel().getTree();
    }

    @Nullable
    DefaultMutableTreeNode getSelectedNode() {
        JTree tree = getTree();

        if (tree.getSelectionModel().getSelectionCount() > 0) {
            TreePath leadSelectionPath = tree.getLeadSelectionPath();
            if (leadSelectionPath == null) {
                return null;
            }

            return (DefaultMutableTreeNode) leadSelectionPath.getLastPathComponent();
        }

        return null;
    }

    void executeCommand(Project project, CommandBase command) {
        ToolWindow window = ToolWindowManager
                .getInstance(project).getToolWindow(MigrationsToolWindowFactory.TOOL_WINDOW_ID);

        if (window != null) {
            Content content = window.getContentManager().getContent(1);
            if (content != null) {
                ConsolePanel consolePanel = (ConsolePanel) content.getComponent();
                command.setConsoleView(consolePanel.getConsoleView());
            }
        }

        command.repaintComponent(getTree());

        ApplicationManager.getApplication().executeOnPooledThread(command);
    }

    boolean enableDownButtons(DefaultMutableTreeNode treeNode) {
        Object userObject = treeNode.getUserObject();
        if (userObject instanceof Migration) {
            return ((Migration) userObject).status == MigrationStatus.Success;
        }

        if (userObject instanceof String) {
            Enumeration migrationEnumeration = treeNode.children();
            while (migrationEnumeration.hasMoreElements()) {
                Object migration = ((DefaultMutableTreeNode) migrationEnumeration.nextElement()).getUserObject();
                if (migration instanceof Migration) {
                    if (((Migration) migration).status == MigrationStatus.Success) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    boolean enableUpButtons(DefaultMutableTreeNode treeNode) {
        Object userObject = treeNode.getUserObject();
        if (userObject instanceof Migration) {
            return ((Migration) userObject).status != MigrationStatus.Success;
        }

        if (userObject instanceof String) {
            Enumeration migrationEnumeration = treeNode.children();
            while (migrationEnumeration.hasMoreElements()) {
                Object migration = ((DefaultMutableTreeNode) migrationEnumeration.nextElement()).getUserObject();
                if (migration instanceof Migration) {
                    if (((Migration) migration).status != MigrationStatus.Success) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
