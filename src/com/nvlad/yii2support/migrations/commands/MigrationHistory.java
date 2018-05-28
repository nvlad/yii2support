package com.nvlad.yii2support.migrations.commands;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.project.Project;
import com.nvlad.yii2support.common.YiiCommandLineUtil;
import com.nvlad.yii2support.migrations.MigrationService;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.MigrationStatus;
import com.nvlad.yii2support.migrations.util.MigrationUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MigrationHistory extends CommandBase {
    private static final Pattern historyEntryPattern = Pattern.compile("\\((\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\) (m\\d{6}_\\d{6}_[\\w+_-]+)");

    private final Map<String, Collection<Migration>> migrationMap;
    private final Set<Migration> migrations;
    private Map<String, DefaultMutableTreeNode> treeNodeMap;

    public MigrationHistory(Project project) {
        super(project);
        migrationMap = MigrationService.getInstance(myProject).getMigrations();
        migrations = new HashSet<>();
    }

    @Override
    public void run() {
        for (Collection<Migration> migrationCollection : migrationMap.values()) {
            migrations.addAll(migrationCollection);
        }

        try {
            LinkedList<String> params = new LinkedList<>();
            params.add("all");
            fillParams(params);

            ProcessHandler processHandler = YiiCommandLineUtil.configureHandler(myProject, "migrate/history", params);
            Integer exitCode = 1;
            if (processHandler != null) {
                exitCode = executeProcess(processHandler);
            }

            MigrationStatus status = exitCode == 0 ? MigrationStatus.NotApply : MigrationStatus.Unknown;
            for (Migration migration : migrations) {
                migration.status = status;
                migration.upDuration = null;
                migration.downDuration = null;
                migration.applyAt = null;
            }

            migrations.clear();
        } catch (ExecutionException e) {
            YiiCommandLineUtil.processError(e);
        }
    }

    @Override
    void processOutput(String text) {
        Matcher matcher = historyEntryPattern.matcher(text);
        if (matcher.find()) {
            String migrationName = matcher.group(2);
            Date date = MigrationUtil.applyDate(matcher.group(1));
            updateMigration(migrationName, date);
        }
    }

    private void updateMigration(String name, Date date) {
        for (Collection<Migration> migrationCollection : migrationMap.values()) {
            for (Migration migration : migrationCollection) {
                if (migration.name.equals(name)) {
                    migration.status = MigrationStatus.Success;
                    migration.applyAt = date;
                    migration.upDuration = null;
                    migration.downDuration = null;

                    migrations.remove(migration);

                    repaintMigrationNode(migration);
                    return;
                }
            }
        }
    }

    DefaultMutableTreeNode findTreeNode(Migration migration) {
        if (myComponent instanceof JTree) {
            if (treeNodeMap == null) {
                JTree tree = (JTree) myComponent;
                DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
                Enumeration pathEnumeration = root.children();

                treeNodeMap = new HashMap<>();
                while (pathEnumeration.hasMoreElements()) {
                    DefaultMutableTreeNode pathNode = ((DefaultMutableTreeNode) pathEnumeration.nextElement());
                    Enumeration migrationEnumeration = pathNode.children();
                    while (migrationEnumeration.hasMoreElements()) {
                        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) migrationEnumeration.nextElement();
                        treeNodeMap.put(((Migration) treeNode.getUserObject()).name, treeNode);
                    }
                }
            }

            return treeNodeMap.get(migration.name);
        }

        return null;
    }
}
