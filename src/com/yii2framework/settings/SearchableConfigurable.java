package com.yii2framework.settings;

import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by NVlad on 27.12.2016.
 *
 */
public class SearchableConfigurable implements com.intellij.openapi.options.SearchableConfigurable {
    private SettingsForm settingsForm;

    @NotNull
    @Override
    public String getId() {
        return "yii2framework.settings";
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Yii2 Framework Plugin";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsForm = new SettingsForm();

        return settingsForm.getRootPane();
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }

    @Override
    public void reset() {

    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }
}
