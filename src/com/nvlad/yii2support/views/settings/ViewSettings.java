package com.nvlad.yii2support.views.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.table.JBTable;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ViewSettings implements Configurable {
    private JTable viewPathMap;
    private JPanel mainPanel;
    private Yii2SupportSettings mySettings;

    public ViewSettings(Project project) {
        mySettings = Yii2SupportSettings.getInstance(project);

        viewPathMap.setModel(new ViewPathMapTableModel(mySettings.viewPathMap));
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "View Settings";
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

        viewPathMap = new JBTable();
        viewPathMap.setSize(150, 150);
    }

    @Override
    public void disposeUIResources() {

    }
}
