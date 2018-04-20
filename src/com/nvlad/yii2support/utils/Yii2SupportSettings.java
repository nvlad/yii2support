package com.nvlad.yii2support.utils;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import com.nvlad.yii2support.views.index.ViewFileIndex;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

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
    public String dbConnection = "db";
    public String migrationTable = "{{%migration}}";

    public Yii2SupportSettings() {
        viewPathMap = new LinkedHashMap<>();
        viewPathMap.put("@app/themes/*/modules", "@app/modules");
        viewPathMap.put("@app/themes/*/widgets", "@app/widgets");
        viewPathMap.put("@app/themes/*", "@app/views");
    }

    @Nullable
    @Override
    public Yii2SupportSettings getState() {
        return this;
    }

    @Override
    public void loadState(Yii2SupportSettings applicationService) {
        if (this.viewPathMap.hashCode() != applicationService.viewPathMap.hashCode()) {
            FileBasedIndex.getInstance().requestRebuild(ViewFileIndex.identity);
        }
        XmlSerializerUtil.copyBean(applicationService, this);
    }

    public static Yii2SupportSettings getInstance(Project project) {
        return ServiceManager.getService(project, Yii2SupportSettings.class);
    }
}
