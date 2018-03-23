package com.nvlad.yii2support.migrations;

import com.nvlad.yii2support.migrations.entities.Migration;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MigrationsMouseListener implements MouseListener {
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            JTree migrationsTree = (JTree) e.getComponent();
            DefaultMutableTreeNode object = (DefaultMutableTreeNode) migrationsTree.getLeadSelectionPath().getLastPathComponent();
            if (!(object.getUserObject() instanceof Migration)) {
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
