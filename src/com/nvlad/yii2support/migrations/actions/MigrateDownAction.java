package com.nvlad.yii2support.migrations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.AnActionButton;
import com.nvlad.yii2support.migrations.MigrationManager;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.MigrationStatus;
import com.nvlad.yii2support.migrations.ui.MigrationPanel;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

@SuppressWarnings("ComponentNotRegistered")
public class MigrateDownAction extends AnActionButton {
    public MigrateDownAction() {
        super("Migrate Down", AllIcons.Actions.Rollback);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        DefaultMutableTreeNode treeNode = getSelectedNode();
        if (treeNode == null) {
            return;
        }

        Set<String> migrations = null;
        Object userObject = treeNode.getUserObject();
        if (userObject instanceof String) {
            MigrationManager manager = MigrationManager.getInstance(anActionEvent.getProject());
            migrations = manager.migrateDown((String) userObject, 0);
        }

        if (userObject instanceof Migration) {
            Migration migration = (Migration) userObject;
            if (migration.status != MigrationStatus.Success) {
                return;
            }

            LinkedList<Migration> migrationList = new LinkedList<>();
            Enumeration migrationEnumeration = treeNode.getParent().children();
            while (migrationEnumeration.hasMoreElements()) {
                migrationList.add((Migration) ((DefaultMutableTreeNode) migrationEnumeration.nextElement()).getUserObject());
            }

            int count = 0;
            Yii2SupportSettings settings = Yii2SupportSettings.getInstance(anActionEvent.getProject());
            Iterator<Migration> migrationIterator = settings.newestFirst ? migrationList.iterator() : migrationList.descendingIterator();
            while (migrationIterator.hasNext()) {
                Migration tmp = migrationIterator.next();
                if (tmp.status == MigrationStatus.Success) {
                    count++;
                }

                if (tmp == migration) {
                    break;
                }
            }

            if (count == 0) {
                return;
            }

            MigrationManager manager = MigrationManager.getInstance(anActionEvent.getProject());
            Object parentUserObject = ((DefaultMutableTreeNode) treeNode.getParent()).getUserObject();
            migrations = manager.migrateDown((String) parentUserObject, count);
        }

        if (migrations != null && migrations.size() > 0) {
            MigrationPanel panel = (MigrationPanel) getContextComponent();
            panel.updateMigrations();
        }
    }

    @Override
    public boolean isEnabled() {
        DefaultMutableTreeNode treeNode = getSelectedNode();
        if (treeNode == null) {
            return false;
        }

        Object userObject = treeNode.getUserObject();
        if (userObject instanceof Migration) {
            return ((Migration) userObject).status == MigrationStatus.Success;
        }

        if (userObject instanceof String) {
            Enumeration migrationEnumeration = treeNode.children();
            while (migrationEnumeration.hasMoreElements()) {
                Object tmp = ((DefaultMutableTreeNode) migrationEnumeration.nextElement()).getUserObject();
                if (tmp instanceof Migration) {
                    if (((Migration) tmp).status == MigrationStatus.Success) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Nullable
    private DefaultMutableTreeNode getSelectedNode() {
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
