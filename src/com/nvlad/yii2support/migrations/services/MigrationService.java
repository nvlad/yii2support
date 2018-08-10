package com.nvlad.yii2support.migrations.services;

import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.SmartList;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.nvlad.yii2support.common.FileUtil;
import com.nvlad.yii2support.migrations.entities.MigrateCommand;
import com.nvlad.yii2support.migrations.entities.MigrateCommandComparator;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.DefaultMigrateCommand;
import com.nvlad.yii2support.utils.Yii2SupportSettings;

import java.util.*;

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
    private Map<MigrateCommand, Collection<Migration>> myMigrationMap;
    private List<Migration> myMigrations;
    private Set<MigrationServiceListener> listeners;

    private MigrationService(Project project) {
        myProject = project;
        myPhpIndex = PhpIndex.getInstance(project);
        baseUrlLength = project.getBaseDir().getUrl().length();
        listeners = new HashSet<>();
    }


    public Map<MigrateCommand, Collection<Migration>> getMigrationCommandMap() {
        if (myMigrationMap == null) {
            myMigrationMap = new HashMap<>();
            sync();
        }

        return myMigrationMap;
    }

    public void sync() {
        Collection<PhpClass> migrations;
        try {
            migrations = myPhpIndex.getAllSubclasses("\\yii\\db\\MigrationInterface");
        } catch (IndexNotReadyException e) {
            return;
        }

        List<MigrateCommand> commands = new SmartList<>(Yii2SupportSettings.getInstance(myProject).migrateCommands);
        commands.add(new DefaultMigrateCommand(commands));
        commands.sort(new MigrateCommandComparator());

        Map<MigrateCommand, Collection<Migration>> migrationMap = new HashMap<>();
        List<Migration> migrationList = new SmartList<>();
        for (MigrateCommand command : commands) {
            migrationMap.put(command, new SmartList<>());
        }

        for (PhpClass migrationClass : migrations) {
            if (migrationClass.isAbstract() || !Migration.isValidMigrationClass(migrationClass)) {
                continue;
            }

            VirtualFile virtualFile = FileUtil.getVirtualFile(migrationClass.getContainingFile());
            if (virtualFile.getUrl().length() < baseUrlLength) {
                continue;
            }

            String path = virtualFile.getUrl().substring(baseUrlLength + 1);
            int pathLength = path.length();
            path = path.substring(0, pathLength - virtualFile.getName().length() - 1);
            Migration migration = getMigrationForClass(migrationClass, path);
            for (MigrateCommand command : commands) {
                if (command.containsMigration(myProject, migration)) {
                    migrationMap.get(command).add(migration);
                    migrationList.add(migration);
                    break;
                }
            }
        }

        if (!migrationMap.equals(myMigrationMap)) {
            myMigrationMap = migrationMap;
            myMigrations = migrationList;

            for (MigrationServiceListener listener : listeners) {
                listener.treeChanged();
            }
        }
    }

    public List<Migration> getMigrations() {
        return myMigrations;
    }

    public void addListener(MigrationServiceListener listener) {
        listeners.add(listener);
    }

    public void removeListener(MigrationServiceListener listener) {
        listeners.remove(listener);
    }

    private Migration getMigrationForClass(PhpClass phpClass, String path) {
        if (myMigrations != null) {
            for (Migration migration : myMigrations) {
                if (migration.migrationClass.equals(phpClass)) {
                    return migration;
                }
            }
        }

        return new Migration(phpClass, path);
    }
}
