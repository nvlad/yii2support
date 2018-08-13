package com.nvlad.yii2support.migrations.ui.toolWindow;

import com.nvlad.yii2support.migrations.entities.MigrateCommand;
import com.nvlad.yii2support.migrations.entities.Migration;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MigrationsMouseListener implements MouseListener {
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            JTree migrationsTree = (JTree) e.getComponent();
            TreePath leadSelectionPath =  migrationsTree.getLeadSelectionPath();
            if (leadSelectionPath == null)  {
                return;
            }

            DefaultMutableTreeNode object = (DefaultMutableTreeNode) leadSelectionPath.getLastPathComponent();
            if (object.getUserObject() instanceof String || object.getUserObject() instanceof MigrateCommand) {
                if (migrationsTree.isExpanded(leadSelectionPath)) {
                    migrationsTree.collapsePath(leadSelectionPath);
                } else {
                    migrationsTree.expandPath(leadSelectionPath);
                }
                return;
            }

            Migration migration = (Migration) object.getUserObject();
            if (migration.migrationClass.canNavigate()) {
                migration.migrationClass.navigate(true);
            }
        }

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
