package com.nvlad.yii2support.migrations.services;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.nvlad.yii2support.migrations.entities.MigrateCommand;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.utils.Yii2SupportSettings;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MigrationService {
    private static final Map<Project, MigrationService> migrationManagerMap = new HashMap<>();

    public static MigrationService getInstance(Project project) {
        if (!migrationManagerMap.containsKey(project)) {
            migrationManagerMap.put(project, new MigrationService(project));
        }

        return migrationManagerMap.get(project);
    }

    private final Project myProject;
    private final PhpIndex myPhpIndex;
    private final int baseUrlLength;
    private Map<String, Collection<Migration>> myMigrationMap;

    private MigrationService(Project project) {
        myProject = project;
        myPhpIndex = PhpIndex.getInstance(project);
        baseUrlLength = project.getBaseDir().getUrl().length();
    }

    public void update() {
        List<MigrateCommand> commands = Yii2SupportSettings.getInstance(myProject).migrateCommands;
    }
}
