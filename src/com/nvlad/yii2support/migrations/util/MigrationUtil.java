package com.nvlad.yii2support.migrations.util;

import com.nvlad.yii2support.migrations.entities.Migration;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MigrationUtil {
    private static final SimpleDateFormat migrationApplyDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Nullable
    public static Date parseApplyDate(String date) {
        try {
            return migrationApplyDateFormat.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static List<String> migrationPaths(List<Migration> migrations) {
        List<String> paths = new LinkedList<>();
        for (Migration migration : migrations) {
            if (!paths.contains(migration.path)) {
                paths.add(migration.path);
            }
        }

        return paths;
    }

    public static List<String> migrationNamespaces(List<Migration> migrations) {
        List<String> namespaces = new LinkedList<>();
        for (Migration migration : migrations) {
            if (migration.namespace.equals("\\") && !namespaces.contains(migration.namespace)) {
                namespaces.add(migration.namespace);
            }
        }

        return namespaces;
    }
}
