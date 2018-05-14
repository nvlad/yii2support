package com.nvlad.yii2support.migrations;

import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.nvlad.yii2support.common.FileUtil;
import com.nvlad.yii2support.migrations.entities.Migration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MigrationManager {
    private static final Map<Project, MigrationManager> migrationManagerMap = new HashMap<>();

    public static MigrationManager getInstance(Project project) {
        if (!migrationManagerMap.containsKey(project)) {
            migrationManagerMap.put(project, new MigrationManager(project));
        }

        return migrationManagerMap.get(project);
    }

    private final PhpIndex myPhpIndex;
    private final int baseUrlLength;
    private Map<String, Collection<Migration>> myMigrationMap;

    private MigrationManager(Project project) {
        myPhpIndex = PhpIndex.getInstance(project);
        baseUrlLength = project.getBaseDir().getUrl().length();
    }

    public Map<String, Collection<Migration>> getMigrations() {
        if (myMigrationMap == null) {
            myMigrationMap = new HashMap<>();
            refresh();
        }

        return myMigrationMap;
    }

    public void refresh() {
        Collection<PhpClass> migrations;
        try {
            migrations = myPhpIndex.getAllSubclasses("\\yii\\db\\Migration");
        } catch (IndexNotReadyException e) {
            return;
        }

        Map<String, Collection<Migration>> migrationMap = new HashMap<>();
        for (PhpClass migration : migrations) {
            VirtualFile virtualFile = FileUtil.getVirtualFile(migration.getContainingFile());
            if (virtualFile.getUrl().length() < baseUrlLength) {
                continue;
            }

            String path = virtualFile.getUrl().substring(baseUrlLength + 1);
            int pathLength = path.length();
            path = path.substring(0, pathLength - virtualFile.getName().length() - 1);

            if (!migrationMap.containsKey(path)) {
                migrationMap.put(path, new LinkedList<>());
            }

            migrationMap.get(path).add(new Migration(migration, path));
        }

        if (myMigrationMap == null) {
            myMigrationMap = migrationMap;
        }

        for (String path : myMigrationMap.keySet()) {
            if (!migrationMap.containsKey(path)) {
                myMigrationMap.remove(path);
                continue;
            }

            Collection<Migration> migrationCollection = migrationMap.get(path);
            for (Migration migration : myMigrationMap.get(path)) {
                if (isNotContains(migrationCollection, migration)) {
                    myMigrationMap.get(path).remove(migration);
                }
            }

            migrationCollection = myMigrationMap.get(path);
            for (Migration migration : migrationMap.get(path)) {
                if (isNotContains(migrationCollection, migration)) {
                    migrationCollection.add(migration);
                }
            }
        }

        for (String path : migrationMap.keySet()) {
            if (!myMigrationMap.containsKey(path)) {
                myMigrationMap.put(path, migrationMap.get(path));
            }
        }
    }

    private boolean isNotContains(Collection<Migration> migrations, Migration required) {
        return migrations.stream().noneMatch(migration -> migration.name.equals(required.name));
    }
}
