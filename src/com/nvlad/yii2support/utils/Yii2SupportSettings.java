package com.nvlad.yii2support.utils;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.SmartList;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.nvlad.yii2support.common.YiiApplicationTemplate;
import com.nvlad.yii2support.common.YiiApplicationUtils;
import com.nvlad.yii2support.migrations.entities.MigrateCommand;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by oleg on 2017-09-06.
 */
@State(name = "Yii2 Support", storages = @Storage("yii2settings.xml"))
public class Yii2SupportSettings implements PersistentStateComponent<Yii2SupportSettings> {
    // Yii Settings
    public String yiiRootPath = null;

    // Database Settings
    public String tablePrefix = "";
    public boolean insertWithTablePrefix = false;

    // View Settings
    public String defaultViewExtension = "php";
    public String defaultViewClass = "\\yii\\web\\View";
    @MapAnnotation(sortBeforeSave = false)
    public Map<String, String> viewPathMap;

    // Migrations
    public boolean newestFirst = false;
    public List<MigrateCommand> migrateCommands;

    // Aliases
    @MapAnnotation(sortBeforeSave = false)
    public Map<String, String> aliasMap;

    private Project myProject;

    public Yii2SupportSettings() {
        viewPathMap = new LinkedHashMap<>();
        viewPathMap.put("@app/themes/*/modules", "@app/modules");
        viewPathMap.put("@app/themes/*/widgets", "@app/widgets");
        viewPathMap.put("@app/themes/*", "@app/views");

        migrateCommands =  new SmartList<>();
        aliasMap = new HashMap<>();
    }

    public Yii2SupportSettings(Project project) {
        this();

        myProject = project;
        initProjectConfiguration(project);
    }

    @Nullable
    @Override
    public Yii2SupportSettings getState() {
        return this;
    }

    @Override
    public void loadState(Yii2SupportSettings settings) {
        if (myProject != null) {
            this.aliasMap.clear();
            this.migrateCommands.clear();
            settings.initProjectConfiguration(myProject);
        }

        XmlSerializerUtil.copyBean(settings, this);
    }

    public void initProjectConfiguration(Project project) {
        YiiApplicationTemplate template = YiiApplicationUtils.getAppTemplate(project, yiiRootPath);
        if (aliasMap.isEmpty()) {
            aliasMap.put("@vendor", "vendor");
            aliasMap.put("@runtime", "@app/runtime");
            aliasMap.put("@webroot", "@app/web");
            switch (template) {
                case Unknown:
                case Basic:
                    aliasMap.put("@yii2support-console-command-app-root", "");
                    break;
                case Advanced:
                    aliasMap.put("@yii2support-console-command-app-root", "@console");
                    aliasMap.put("@common", "common");
                    aliasMap.put("@frontend", "frontend");
                    aliasMap.put("@backend", "backend");
                    aliasMap.put("@console", "console");
                case StarterKit:
                    aliasMap.put("@base", "");
                    aliasMap.put("@api", "api");
                    aliasMap.put("@storage", "storage");
                    break;
            }
        }

        if (migrateCommands.isEmpty()) {
            MigrateCommand command;
            switch (template) {
                case Unknown:
                case Basic:
                case Advanced:
                    command = new MigrateCommand();
                    command.command = "migrate";
                    command.migrationPath.add("@app/migrations");
                    command.migrationTable = "{{%migration}}";
                    command.db = "db";
                    command.isDefault = true;
                    command.useTablePrefix = false;
                    migrateCommands.add(command);
                    break;
                case StarterKit:
                    command = new MigrateCommand();
                    command.command = "migrate";
                    command.migrationPath.add("@common/migrations/db");
                    command.migrationTable = "{{%system_db_migration}}";
                    command.db = "db";
                    command.isDefault = true;
                    command.useTablePrefix = false;
                    migrateCommands.add(command);

                    command = new MigrateCommand();
                    command.command = "rbac-migrate";
                    command.migrationPath.add("@common/migrations/rbac/");
                    command.migrationTable = "{{%system_rbac_migration}}";
                    command.db = "db";
                    command.isDefault = false;
                    command.useTablePrefix = false;
                    migrateCommands.add(command);
                    break;
            }
        }
    }

    public static Yii2SupportSettings getInstance(Project project) {
        return ServiceManager.getService(project, Yii2SupportSettings.class);
    }
}
