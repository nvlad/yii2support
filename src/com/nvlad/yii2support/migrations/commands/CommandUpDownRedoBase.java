package com.nvlad.yii2support.migrations.commands;

import com.intellij.openapi.project.Project;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.MigrationStatus;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.time.Duration;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class CommandUpDownRedoBase extends CommandBase {
    private static Pattern migrateUpPattern = Pattern.compile("\\*\\*\\* (applying|applied|reverting|reverted) (m\\d{6}_\\d{6}_.+?)\\s+(\\(time: ([\\d.]+)s\\))?");

    final String myPath;
    final List<Migration> myMigrations;
    private Map<String, DefaultMutableTreeNode> treeNodeMap;

    CommandUpDownRedoBase(Project project, String path, @NotNull List<Migration> migrations) {
        super(project);

        myPath = path;
        myMigrations = migrations;
    }

    @Override
    void processOutput(String text) {
        Matcher matcher = migrateUpPattern.matcher(text);

        if (matcher.find()) {
            Migration migration = findMigration(matcher.group(2));
            if (migration == null) {
                return;
            }

            switch (matcher.group(1)) {
                case "applying":
                    migration.status = MigrationStatus.Progress;
                    migration.applyAt = null;
                    migration.upDuration = null;
                    migration.downDuration = null;
                    break;
                case "applied":
                    migration.status = MigrationStatus.Success;
                    migration.upDuration = Duration.parse("PT" + matcher.group(4) + "S");
                    break;
                case "reverting":
                    migration.status = MigrationStatus.Progress;
                    migration.applyAt = null;
                    migration.upDuration = null;
                    migration.downDuration = null;
                    break;
                case "reverted":
                    migration.status = MigrationStatus.NotApply;
                    migration.downDuration = Duration.parse("PT" + matcher.group(4) + "S");
                    break;
            }

            repaintMigrationNode(migration);
        }
    }

    private Migration findMigration(String name) {
        for (Migration migration : myMigrations) {
            if (migration.name.equals(name)) {
                return migration;
            }
        }

        return null;
    }

    private void repaintMigrationNode(Migration migration) {
        DefaultMutableTreeNode treeNode = findTreeNode(migration);
        if (treeNode != null) {
            ((DefaultTreeModel) ((JTree) myComponent).getModel()).nodeChanged(treeNode);
        }
    }

    private DefaultMutableTreeNode findTreeNode(Migration migration) {
        if (myComponent instanceof JTree) {
            if (treeNodeMap == null) {
                JTree tree = (JTree) myComponent;
                DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
                Enumeration pathEnumeration = root.children();
                while (pathEnumeration.hasMoreElements()) {
                    DefaultMutableTreeNode pathNode = ((DefaultMutableTreeNode) pathEnumeration.nextElement());
                    String path = (String) pathNode.getUserObject();
                    if (path.equals(myPath)) {
                        treeNodeMap = new HashMap<>();
                        Enumeration migrationEnumeration = pathNode.children();
                        while (migrationEnumeration.hasMoreElements()) {
                            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) migrationEnumeration.nextElement();
                            treeNodeMap.put(((Migration) treeNode.getUserObject()).name, treeNode);
                        }
                        break;
                    }
                }
            }

            return treeNodeMap.get(migration.name);
        }

        return null;
    }
}
