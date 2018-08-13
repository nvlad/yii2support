package com.nvlad.yii2support.migrations.commands;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.nvlad.yii2support.common.YiiCommandLineUtil;
import com.nvlad.yii2support.migrations.entities.MigrateCommand;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.MigrationStatus;
import com.nvlad.yii2support.migrations.ui.toolWindow.MigrationPanel;
import com.nvlad.yii2support.migrations.util.MigrationUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MigrationHistory extends CommandBase {
    private static final Pattern historyEntryPattern = Pattern.compile("\\((\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\) ([\\w\\\\-]*?\\\\)?([mM]\\d{6}_?\\d{6}\\D.+)");
    private Map<Migration, DefaultMutableTreeNode> treeNodeMap;
    private final List<Migration> myMigrations;

    public MigrationHistory(Project project, MigrateCommand command, List<Migration> migrations) {
        super(project, command);
        myMigrations = migrations;
    }

    @Override
    public void run() {
        try {
            LinkedList<String> params = new LinkedList<>();
            params.add("all");
            prepareCommandParams(params, null);

            ProcessHandler processHandler = YiiCommandLineUtil.configureHandler(myProject, myCommand.command + "/history", params);
            Integer exitCode = 1;
            if (processHandler != null) {
                exitCode = executeProcess(processHandler);
            }

            MigrationStatus status = exitCode == 0 ? MigrationStatus.NotApply : MigrationStatus.Unknown;
            for (Migration migration : myMigrations) {
                migration.status = status;
                migration.upDuration = null;
                migration.downDuration = null;
                migration.applyAt = null;
            }

            MigrationPanel component = (MigrationPanel) myComponent.getParent().getParent().getParent();
            ApplicationManager.getApplication().invokeLater(() -> {
                component.updateTree();
                component.updateUI();
            });
        } catch (ExecutionException e) {
            YiiCommandLineUtil.processError(e);
        }
    }

    @Override
    void processOutput(String text) {
        Matcher matcher = historyEntryPattern.matcher(text);
        if (matcher.find()) {
            String migrationNamespace = "\\" + StringUtil.defaultIfEmpty(matcher.group(2), "");
            String migrationName = matcher.group(3);
            Date date = MigrationUtil.parseApplyDate(matcher.group(1));
            updateMigration(migrationNamespace, migrationName, date);
        }

        if (text.contains("No migration has been done before.")) {
            if (treeNodeMap == null) {
                findTreeNode(myMigrations.get(0));

            }
        }
    }

    private void updateMigration(String namespace, String name, Date date) {
        for (Migration migration : myMigrations) {
            if (StringUtil.equals(name, migration.name) && StringUtil.equals(namespace, migration.namespace)) {
                migration.status = MigrationStatus.Success;
                migration.applyAt = date;
                migration.upDuration = null;
                migration.downDuration = null;

                myMigrations.remove(migration);

                repaintMigrationNode(migration);
                return;
            }
        }
    }

    DefaultMutableTreeNode findTreeNode(Migration migration) {
        if (myComponent instanceof JTree) {
            if (treeNodeMap == null) {
                JTree tree = (JTree) myComponent;
                DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
                treeNodeMap = buildTreeNodeMap(root);
            }

            return treeNodeMap.get(migration);
        }

        return null;
    }

    private Map<Migration, DefaultMutableTreeNode> buildTreeNodeMap(DefaultMutableTreeNode node) {
        Map<Migration, DefaultMutableTreeNode> result = new HashMap<>();

        Enumeration enumeration = node.children();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode item = (DefaultMutableTreeNode) enumeration.nextElement();
            if (item.getUserObject() instanceof Migration) {
                result.put((Migration) item.getUserObject(), item);

                if (node.getChildCount() > 0) {
                    result.putAll(buildTreeNodeMap(item));
                }
            }
        }

        return result;
    }
}
