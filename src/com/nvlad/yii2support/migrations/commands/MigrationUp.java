package com.nvlad.yii2support.migrations.commands;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.project.Project;
import com.nvlad.yii2support.common.YiiCommandLineUtil;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.MigrationStatus;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MigrationUp extends CommandBase {
    private static Pattern migrateUpPattern = Pattern.compile("\\*\\*\\* (applying|applied) (m\\d{6}_\\d{6}_.+?)\\s+(\\(time: ([\\d.]+)s\\))?");

    private final String myPath;
    private final List<Migration> myMigrations;

    public MigrationUp(Project project, String path, @NotNull List<Migration> migrations) {
        super(project);

        myPath = path;
        myMigrations = migrations;
    }

    @Override
    public void run() {
        LinkedList<String> params = new LinkedList<>();
        params.add(String.valueOf(myMigrations.size()));
        fillParams(params);
        params.add("--migrationPath=" + myPath);
        params.add("--interactive=0");

        try {
            GeneralCommandLine commandLine = YiiCommandLineUtil.create(myProject, "migrate/up", params);

            executeCommandLine(commandLine);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
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

                    repaintMigrationNode(migration);
                    break;
                case "applied":
                    migration.status = MigrationStatus.Success;
                    migration.upDuration = Duration.parse("PT" + matcher.group(4) + "S");

                    repaintMigrationNode(migration);
                    break;
            }
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

    private Set<DefaultMutableTreeNode> treeNodes;
    private DefaultMutableTreeNode findTreeNode(Migration migration) {
        if (myComponent instanceof JTree) {
            if (treeNodes == null) {
                JTree tree = (JTree) myComponent;
                DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
                Enumeration pathEnumeration = root.children();
                while (pathEnumeration.hasMoreElements()) {
                    DefaultMutableTreeNode pathNode = ((DefaultMutableTreeNode) pathEnumeration.nextElement());
                    String path = (String) pathNode.getUserObject();
                    if (path.equals(myPath)) {
                        treeNodes = new HashSet<>();
                        Enumeration migrationEnumeration = pathNode.children();
                        while (migrationEnumeration.hasMoreElements()) {
                            treeNodes.add((DefaultMutableTreeNode) migrationEnumeration.nextElement());
                        }
                        break;
                    }
                }
            }

            for (DefaultMutableTreeNode treeNode : treeNodes) {
                if (((Migration) treeNode.getUserObject()).name.equals(migration.name)) {
                    return treeNode;
                }
            }
        }

        return null;
    }
}
