package com.nvlad.yii2support.views.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;

public class ViewSettings implements Configurable {
    private JPanel mainPanel;
    private JPanel viewPathMap;

    public ViewSettings(Project project) {
        Yii2SupportSettings mySettings = Yii2SupportSettings.getInstance(project);

        ((ViewPathMapPanel) viewPathMap).setData(new ArrayList<>(mySettings.viewPathMap.entrySet()));
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Views";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return mainPanel;
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

    private void createUIComponents() {
        // TODO: place custom component creation code here
        viewPathMap = new ViewPathMapPanel();
    }

    @Override
    public void disposeUIResources() {
    }
}
