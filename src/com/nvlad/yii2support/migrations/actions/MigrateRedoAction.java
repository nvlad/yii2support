package com.nvlad.yii2support.migrations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.nvlad.yii2support.migrations.commands.MigrationRedo;
import com.nvlad.yii2support.migrations.entities.MigrateCommand;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.MigrationStatus;
import com.nvlad.yii2support.utils.Yii2SupportSettings;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

@SuppressWarnings("ComponentNotRegistered")
public class MigrateRedoAction extends MigrateBaseAction {
    public MigrateRedoAction() {
        super("Redo Migrate", AllIcons.Actions.Rollback);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        DefaultMutableTreeNode treeNode = getSelectedNode();
        if (treeNode == null) {
            return;
        }

        LinkedList<Migration> migrationsToRedo = new LinkedList<>();
        String migrationPath;
        MigrationRedo migrationRedo = null;

        Project project = anActionEvent.getProject();
        if (project == null) {
            return;
        }

        Object userObject = treeNode.getUserObject();
        if (userObject instanceof String) {
            Enumeration migrationEnumeration = treeNode.children();
            while (migrationEnumeration.hasMoreElements()) {
                Migration migration = (Migration) ((DefaultMutableTreeNode) migrationEnumeration.nextElement()).getUserObject();
                if (migration.status == MigrationStatus.Success || migration.status == MigrationStatus.RollbackError) {
                    migrationsToRedo.add(migration);
                }
            }

            migrationPath = getMigrationPath(project, treeNode);
            migrationRedo = new MigrationRedo(project, migrationsToRedo, getCommand(treeNode), migrationPath);
        }

        if (userObject instanceof MigrateCommand) {
            Enumeration migrationEnumeration = treeNode.children();
            while (migrationEnumeration.hasMoreElements()) {
                Migration migration = (Migration) ((DefaultMutableTreeNode) migrationEnumeration.nextElement()).getUserObject();
                if (migration.status == MigrationStatus.Success || migration.status == MigrationStatus.RollbackError) {
                    migrationsToRedo.add(migration);
                }
            }

            migrationPath = getMigrationPath(project, treeNode);
            migrationRedo = new MigrationRedo(project, migrationsToRedo, (MigrateCommand) userObject, migrationPath);
        }

        if (userObject instanceof Migration) {
            Migration selectedMigration = (Migration) userObject;
            if (selectedMigration.status != MigrationStatus.Success && selectedMigration.status != MigrationStatus.RollbackError) {
                return;
            }

            LinkedList<Migration> migrationList = new LinkedList<>();
            Enumeration migrationEnumeration = treeNode.getParent().children();
            while (migrationEnumeration.hasMoreElements()) {
                migrationList.add((Migration) ((DefaultMutableTreeNode) migrationEnumeration.nextElement()).getUserObject());
            }

            Yii2SupportSettings settings = Yii2SupportSettings.getInstance(anActionEvent.getProject());
            Iterator<Migration> migrationIterator = settings.newestFirst ? migrationList.iterator() : migrationList.descendingIterator();
            while (migrationIterator.hasNext()) {
                Migration migration = migrationIterator.next();
                if (migration.status == MigrationStatus.Success || migration.status == MigrationStatus.RollbackError) {
                    migrationsToRedo.add(migration);
                }

                if (migration == selectedMigration) {
                    break;
                }
            }

            if (migrationsToRedo.size() == 0) {
                return;
            }

            migrationPath = getMigrationPath(project, treeNode.getParent());
            migrationRedo = new MigrationRedo(project, migrationsToRedo, getCommand(treeNode), migrationPath);
        }


        if (migrationRedo != null) {
            executeCommand(project, migrationRedo);
        }
    }

    @Override
    public boolean isEnabled() {
        DefaultMutableTreeNode treeNode = getSelectedNode();
        if (treeNode == null || !getTree().isEnabled()) {
            return false;
        }

        return enableDownButtons(treeNode);
    }
}
