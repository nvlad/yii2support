package com.nvlad.yii2support.migrations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.nvlad.yii2support.migrations.MigrationManager;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.MigrationStatus;
import com.nvlad.yii2support.migrations.ui.MigrationPanel;
import com.nvlad.yii2support.utils.Yii2SupportSettings;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

@SuppressWarnings("ComponentNotRegistered")
public class MigrateUpAction extends MigrateBaseAction {
    public MigrateUpAction() {
        super("Migrate Up", AllIcons.Actions.Execute);
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
            migrations = manager.migrateUp((String) userObject, 0);
        }

        if (userObject instanceof Migration) {
            Migration migration = (Migration) userObject;
            if (migration.status == MigrationStatus.Success) {
                return;
            }

            LinkedList<Migration> migrationList = new LinkedList<>();
            Enumeration migrationEnumeration = treeNode.getParent().children();
            while (migrationEnumeration.hasMoreElements()) {
                migrationList.add((Migration) ((DefaultMutableTreeNode) migrationEnumeration.nextElement()).getUserObject());
            }

            int count = 0;
            Yii2SupportSettings settings = Yii2SupportSettings.getInstance(anActionEvent.getProject());
            Iterator<Migration> migrationIterator = settings.newestFirst ? migrationList.descendingIterator() : migrationList.iterator();
            while (migrationIterator.hasNext()) {
                Migration tmp = migrationIterator.next();
                if (tmp.status != MigrationStatus.Success) {
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
            migrations = manager.migrateUp((String) parentUserObject, count);
        }

        if(migrations != null && migrations.size() > 0) {
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
            return ((Migration) userObject).status != MigrationStatus.Success;
        }

        if (userObject instanceof String) {
            Enumeration migrationEnumeration = treeNode.children();
            while (migrationEnumeration.hasMoreElements()) {
                Object tmp = ((DefaultMutableTreeNode) migrationEnumeration.nextElement()).getUserObject();
                if (tmp instanceof Migration) {
                    if (((Migration) tmp).status != MigrationStatus.Success) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
