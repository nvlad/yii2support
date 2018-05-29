package com.nvlad.yii2support;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.Nullable;

@State(name = "Yii2 Support", storages = @Storage(file = "$APP_CONFIG$/yii2support.xml"))
public class PluginGlobalSettings implements PersistentStateComponent<PluginGlobalSettings> {
    public String version;
    public String uuid;
    public String username;

    @Nullable
    @Override
    public PluginGlobalSettings getState() {
        return this;
    }

    @Override
    public void loadState(PluginGlobalSettings settings) {
        XmlSerializerUtil.copyBean(settings, this);
    }

    public static PluginGlobalSettings getInstance() {
        return ServiceManager.getService(PluginGlobalSettings.class);
    }
}
