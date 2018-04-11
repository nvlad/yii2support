package com.nvlad.yii2support.migrations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.AnActionButton;
import com.nvlad.yii2support.migrations.MigrationManager;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.MigrationStatus;
import com.nvlad.yii2support.migrations.ui.MigrationPanel;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.Enumeration;
import java.util.Set;

@SuppressWarnings("ComponentNotRegistered")
public class MigrateUpAction extends AnActionButton {
    public MigrateUpAction() {
        super("Migrate Up", AllIcons.Actions.Execute);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        MigrationPanel panel = (MigrationPanel) getContextComponent();
        JTree tree = panel.getTree();

        if (tree.getSelectionModel().getSelectionCount() > 0) {
            TreePath leadSelectionPath = tree.getLeadSelectionPath();
            if (leadSelectionPath == null) {
                return;
            }

            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) leadSelectionPath.getLastPathComponent();
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof String) {
                MigrationManager manager = MigrationManager.getInstance(anActionEvent.getProject());
                Set<String> migrations = manager.migrateUp((String) userObject, 0);
            }

            if (userObject instanceof Migration) {
                Migration migration = (Migration) userObject;
                if (migration.status == MigrationStatus.Success) {
                    return;
                }

                int count = 0;
                Enumeration migrationEnumeration = treeNode.getParent().children();
                while (migrationEnumeration.hasMoreElements()) {
                    Migration tmp = (Migration) ((DefaultMutableTreeNode) migrationEnumeration.nextElement()).getUserObject();
                    if (migration.status != MigrationStatus.Success) {
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
                Set<String> migrations = manager.migrateUp((String) parentUserObject, count);
            }

//
//            final Project project = anActionEvent.getProject();
//            final Yii2SupportSettings settings = Yii2SupportSettings.getInstance(project);
//            settings.newestFirst = !settings.newestFirst;
        }

    }
}
