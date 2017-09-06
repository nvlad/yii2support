package com.nvlad.yii2support.utils;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by oleg on 2017-09-06.
 */
@State(name = "Yii2 support", storages = @Storage(id = "yii2-support--application", file = "$APP_CONFIG$/yii2support.xml"))
public class Yii2SupportSettings implements ApplicationComponent, PersistentStateComponent<Yii2SupportSettings> {

    public String tablePrefix = "";

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
}
