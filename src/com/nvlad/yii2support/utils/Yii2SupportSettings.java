package com.nvlad.yii2support.utils;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by oleg on 2017-09-06.
 */
@State(name = "Yii2 support", storages = @Storage(id = "yii2-support", file = "$APP_CONFIG$/yii2support.xml"))
public class Yii2SupportSettings implements ApplicationComponent, PersistentStateComponent<Yii2SupportSettings> {

    public String tablePrefix = "";
    public boolean insertWithTablePrefix = false;

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Yii2SupportSettings";
    }

    @Nullable
    @Override
    public Yii2SupportSettings getState() {
        return this;
    }

    @Override
    public void loadState(Yii2SupportSettings applicationService) {
        XmlSerializerUtil.copyBean(applicationService, this);
    }

    public static Yii2SupportSettings getInstance(Project project) {
        return ServiceManager.getService(project, Yii2SupportSettings.class);
    }
}
