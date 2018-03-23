package com.nvlad.yii2support.migrations.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.nvlad.yii2support.common.DatabaseUtils;
import com.nvlad.yii2support.common.FileUtil;
import com.nvlad.yii2support.migrations.entities.Migration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MigrationUtil {
    @NotNull
    public static Map<String, Collection<Migration>> getMigrations(Project project) {
        PhpIndex phpIndex = PhpIndex.getInstance(project);
        Collection<PhpClass> migrations = phpIndex.getAllSubclasses("\\yii\\db\\Migration");
        int baseUrlLength = project.getBaseDir().getUrl().length();

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
                migrationMap.put(path, new HashSet<>());
            }

            migrationMap.get(path).add(new Migration(migration, path));
        }

        return migrationMap;
    }

    @Nullable
    public static Collection<String> getAppliedMigrations(Project project) {
        if (!DatabaseUtils.HasConnections(project)) {
            return null;
        }

        return null;
    }
}
