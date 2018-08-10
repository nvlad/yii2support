package com.nvlad.yii2support.migrations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.nvlad.yii2support.migrations.commands.MigrationUp;
import com.nvlad.yii2support.migrations.entities.DefaultMigrateCommand;
import com.nvlad.yii2support.migrations.entities.MigrateCommand;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.MigrationStatus;
import com.nvlad.yii2support.utils.Yii2SupportSettings;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

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

        LinkedList<Migration> migrationsToUp = new LinkedList<>();
        String migrationPath;
        MigrationUp migrationUp = null;

        Project project = anActionEvent.getProject();
        if (project == null) {
            return;
        }

        Object userObject = treeNode.getUserObject();
        if (userObject instanceof String) {
            Enumeration migrationEnumeration = treeNode.children();
            while (migrationEnumeration.hasMoreElements()) {
                Migration migration = (Migration) ((DefaultMutableTreeNode) migrationEnumeration.nextElement()).getUserObject();
                if (migration.status != MigrationStatus.Success) {
                    migrationsToUp.add(migration);
                }
            }

            migrationPath = getMigrationPath(project, treeNode);
            migrationUp = new MigrationUp(project, migrationsToUp, getCommand(treeNode), migrationPath);
        }

        if (userObject instanceof MigrateCommand && !(userObject instanceof DefaultMigrateCommand)) {
            Enumeration migrationEnumeration = treeNode.children();
            while (migrationEnumeration.hasMoreElements()) {
                Migration migration = (Migration) ((DefaultMutableTreeNode) migrationEnumeration.nextElement()).getUserObject();
                if (migration.status != MigrationStatus.Success) {
                    migrationsToUp.add(migration);
                }
            }

            migrationPath = getMigrationPath(project, treeNode);
            migrationUp = new MigrationUp(project, migrationsToUp, getCommand(treeNode), migrationPath);
        }

        if (userObject instanceof Migration) {
            Migration selectedMigration = (Migration) userObject;
            if (selectedMigration.status == MigrationStatus.Success) {
                return;
            }

            LinkedList<Migration> migrationList = new LinkedList<>();
            Enumeration migrationEnumeration = treeNode.getParent().children();
            while (migrationEnumeration.hasMoreElements()) {
                migrationList.add((Migration) ((DefaultMutableTreeNode) migrationEnumeration.nextElement()).getUserObject());
            }

            Yii2SupportSettings settings = Yii2SupportSettings.getInstance(anActionEvent.getProject());
            Iterator<Migration> migrationIterator = settings.newestFirst ? migrationList.descendingIterator() : migrationList.iterator();
            while (migrationIterator.hasNext()) {
                Migration migration = migrationIterator.next();
                if (migration.status != MigrationStatus.Success) {
                    migrationsToUp.add(migration);
                }

                if (migration == selectedMigration) {
                    break;
                }
            }

            if (migrationsToUp.size() == 0) {
                return;
            }

            migrationPath = getMigrationPath(project, treeNode.getParent());
            migrationUp = new MigrationUp(project, migrationsToUp, getCommand(treeNode), migrationPath);
        }


        if (migrationUp != null) {
            executeCommand(project, migrationUp);
        }
    }

    @Override
    public boolean isEnabled() {
        DefaultMutableTreeNode treeNode = getSelectedNode();
        if (treeNode == null || !getTree().isEnabled()) {
            return false;
        }

        return enableUpButtons(treeNode);
    }
}
