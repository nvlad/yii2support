package com.nvlad.yii2support.ui.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SettingsForm implements Configurable {
    private JPanel mainPanel;

    public SettingsForm() {
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Yii2 Support";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return null;
//        return mainPanel;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() {

    }

    @Override
    public void reset() {

    }

    @Override
    public void disposeUIResources() {

    }
}
