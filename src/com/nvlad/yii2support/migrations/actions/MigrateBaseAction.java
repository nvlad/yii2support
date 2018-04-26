package com.nvlad.yii2support.migrations.actions;

import com.intellij.ui.AnActionButton;
import com.nvlad.yii2support.migrations.ui.MigrationPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

abstract class MigrateBaseAction extends AnActionButton {
    MigrateBaseAction(String name, Icon icon) {
        super(name, icon);
    }

    @Nullable
    DefaultMutableTreeNode getSelectedNode() {
        MigrationPanel panel = (MigrationPanel) getContextComponent();
        JTree tree = panel.getTree();

        if (tree.getSelectionModel().getSelectionCount() > 0) {
            TreePath leadSelectionPath = tree.getLeadSelectionPath();
            if (leadSelectionPath == null) {
                return null;
            }

            return (DefaultMutableTreeNode) leadSelectionPath.getLastPathComponent();
        }

        return null;
    }

}
