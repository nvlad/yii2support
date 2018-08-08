package com.nvlad.yii2support.migrations.commands;

import com.intellij.database.dataSource.DataSourceUiUtil;
import com.intellij.database.dataSource.LocalDataSource;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.database.util.DbImplUtil;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.nvlad.yii2support.common.YiiCommandLineUtil;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.MigrationStatus;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.time.Duration;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class CommandUpDownRedoBase extends CommandBase {
    private static final Pattern migratePattern = Pattern.compile("\\*\\*\\* (applying|applied|reverting|reverted|failed to apply|failed to revert) ([\\w\\\\-]*?\\\\)?([mM]\\d{6}_?\\d{6}\\D.+?)\\s+(\\(time: ([\\d.]+)s\\))?");

    final String myPath;
    final List<Migration> myMigrations;
    String direction = null;
    private Map<String, DefaultMutableTreeNode> treeNodeMap;
    private Map<Migration, MigrationStatus> migrationStatusMap;

    CommandUpDownRedoBase(Project project, String path, @NotNull List<Migration> migrations) {
        super(project);

        myPath = path;
        myMigrations = migrations;
    }

    @Override
    void processOutput(String text) {
        Matcher matcher = migratePattern.matcher(text);

        if (matcher.find()) {
            Migration migration = findMigration("\\" + StringUtil.defaultIfEmpty(matcher.group(2), ""), matcher.group(3));
            if (migration == null) {
                return;
            }

            switch (matcher.group(1)) {
                case "applying":
                    migration.status = MigrationStatus.Progress;
                    migration.applyAt = null;
                    migration.upDuration = null;

                    direction = "applying";
                    break;
                case "applied":
                    migration.status = MigrationStatus.Success;
                    migration.upDuration = Duration.parse("PT" + matcher.group(5) + "S");
                    break;
                case "failed to apply":
                    migration.status = MigrationStatus.ApplyError;
                    break;
                case "reverting":
                    migration.status = MigrationStatus.Progress;
                    migration.applyAt = null;
                    migration.upDuration = null;
                    migration.downDuration = null;

                    direction = "reverting";
                    break;
                case "reverted":
                    migration.status = MigrationStatus.NotApply;
                    migration.downDuration = Duration.parse("PT" + matcher.group(5) + "S");
                    break;
                case "failed to revert":
                    migration.status = MigrationStatus.RollbackError;
                    break;
            }

            repaintMigrationNode(migration);
        }
    }

    void executeCommandWithParams(String command, List<String> parameters) {
        try {
            ProcessHandler processHandler = YiiCommandLineUtil.configureHandler(myProject, command, parameters);
            if (processHandler == null) {
                return;
            }

            migrationStatusMap = new HashMap<>();
            for (Migration migration : myMigrations) {
                migrationStatusMap.put(migration, migration.status);
            }

            executeProcess(processHandler);

            setErrorStatusForMigrationInProgress();
        } catch (ExecutionException e) {
            YiiCommandLineUtil.processError(e);
        }

        syncDataSources();
    }

    DefaultMutableTreeNode findTreeNode(Migration migration) {
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

    private void setErrorStatusForMigrationInProgress() {
        if (myMigrations.size() > 0) {
            for (Migration migration : myMigrations) {
                if (migration.status == MigrationStatus.Progress || migration.status == migrationStatusMap.get(migration)) {
                    if (direction.equals("reverting")) {
                        migration.status = MigrationStatus.RollbackError;
                    } else {
                        migration.status = MigrationStatus.ApplyError;
                    }
                }
            }
        }
    }

    private void syncDataSources() {
        DbPsiFacade facade = DbPsiFacade.getInstance(myProject);
        for (DbDataSource dataSource : facade.getDataSources()) {
            if (dataSource.getDelegate() instanceof LocalDataSource) {
                if (DbImplUtil.isConnected(dataSource)) {
                    LocalDataSource localDataSource = (LocalDataSource) dataSource.getDelegate();
                    DataSourceUiUtil.performAutoSyncTask(myProject, localDataSource);
                }
            }
        }
    }

    private Migration findMigration(String namespace, String name) {
        for (Migration migration : myMigrations) {
            if (StringUtil.equals(name, migration.name) && StringUtil.equals(namespace, migration.namespace)) {
                return migration;
            }
        }

        return null;
    }
}
