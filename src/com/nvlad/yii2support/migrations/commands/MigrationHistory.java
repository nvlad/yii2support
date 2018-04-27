package com.nvlad.yii2support.migrations.commands;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.project.Project;
import com.nvlad.yii2support.common.YiiCommandLineUtil;
import com.nvlad.yii2support.migrations.MigrationManager;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.MigrationStatus;
import com.nvlad.yii2support.migrations.util.MigrationUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MigrationHistory extends CommandBase {
    private static Pattern historyEntryPattern = Pattern.compile("\\((\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\) (m\\d{6}_\\d{6}_[\\w+_-]+)");

    private final Map<String, Collection<Migration>> migrationMap;
    private Set<Migration> migrations;

    public MigrationHistory(Project project) {
        super(project);
        migrationMap = MigrationManager.getInstance(myProject).getMigrations();
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

            GeneralCommandLine commandLine = YiiCommandLineUtil.create(myProject, "migrate/history", params);

            executeCommandLine(commandLine);

            for (Migration migration : migrations) {
                migration.status = MigrationStatus.NotApply;
            }

            migrations.clear();
        } catch (ExecutionException e) {
            e.printStackTrace();
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
                    migration.applyAt = date;
                    migration.status = MigrationStatus.Success;

                    migrations.remove(migration);
                    return;
                }
            }
        }
    }
}
