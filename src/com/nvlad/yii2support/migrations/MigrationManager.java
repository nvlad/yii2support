package com.nvlad.yii2support.migrations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.nvlad.yii2support.common.DatabaseUtils;
import com.nvlad.yii2support.common.FileUtil;
import com.nvlad.yii2support.migrations.entities.Migration;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MigrationManager {
    private static final Map<Project, MigrationManager> migrationManagerMap = new HashMap<>();

    public static MigrationManager getInstance(Project project) {
        if (!migrationManagerMap.containsKey(project)) {
            migrationManagerMap.put(project, new MigrationManager(project));
        }

        return migrationManagerMap.get(project);
    }

    private final Project myProject;

    private MigrationManager(Project project) {
        myProject = project;
    }

    public Map<String, Collection<Migration>> getMigrations() {
        PhpIndex phpIndex = PhpIndex.getInstance(myProject);
        Collection<PhpClass> migrations = phpIndex.getAllSubclasses("\\yii\\db\\Migration");
        int baseUrlLength = myProject.getBaseDir().getUrl().length();

        Map<String, Collection<Migration>> migrationMap = new HashMap<>();
        for (PhpClass migration : migrations) {
            VirtualFile virtualFile = FileUtil.getVirtualFile(migration.getContainingFile());
            if (virtualFile.getUrl().length() < baseUrlLength) {
                continue;
            }

            String path = virtualFile.getUrl().substring(baseUrlLength);
            int pathLength = path.length();
            path = path.substring(0, pathLength - virtualFile.getName().length() - 1);

            if (!migrationMap.containsKey(path)) {
                migrationMap.put(path, new LinkedList<>());
            }

            migrationMap.get(path).add(new Migration(migration, path));
        }

        for (Map.Entry<String, Collection<Migration>> entry : migrationMap.entrySet()) {
            ((LinkedList<Migration>) entry.getValue()).sort(new MigrationComparator());
        }

        return migrationMap;
    }

    @Nullable
    public Collection<String> getAppliedMigrations() {
        if (!DatabaseUtils.HasConnections(myProject)) {
            return null;
        }

        return null;
    }

    class MigrationComparator implements Comparator<Migration> {
        @Override
        public int compare(Migration m1, Migration m2) {
//            return m1.createdAt.compareTo(m2.createdAt);
            return m2.createdAt.compareTo(m1.createdAt);
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }
    }
}
