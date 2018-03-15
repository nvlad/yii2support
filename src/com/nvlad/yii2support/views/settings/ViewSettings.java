package com.nvlad.yii2support.views.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.util.indexing.FileBasedIndex;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import com.nvlad.yii2support.views.index.ViewFileIndex;
import com.nvlad.yii2support.views.util.ViewUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewSettings implements Configurable {
    private final Yii2SupportSettings mySettings;
    private Project myProject;
    private JPanel mainPanel;
    private JPanel viewPathMap;
    private JTextField defaultViewClass;
    private JComboBox defaultViewFileExt;
    private final List<Map.Entry<String, String>> currentThemePathMap;

    public ViewSettings(Project project) {
        myProject = project;
        mySettings = Yii2SupportSettings.getInstance(project);

        currentThemePathMap = new ArrayList<>(mySettings.viewPathMap.entrySet());
        ((ThemePathMapPanel) viewPathMap).setData(new ArrayList<>(mySettings.viewPathMap.entrySet()));

        defaultViewClass.setText(mySettings.defaultViewClass);
        defaultViewFileExt.getModel().setSelectedItem(mySettings.defaultViewExtension);
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
        return !mySettings.defaultViewClass.equals(defaultViewClass.getText())
                || !mySettings.defaultViewExtension.equals(defaultViewFileExt.getModel().getSelectedItem())
                || ((ThemePathMapPanel) viewPathMap).getData().hashCode() != currentThemePathMap.hashCode();
    }

    @Override
    public void apply() {
        if (((ThemePathMapPanel) viewPathMap).getData().hashCode() != currentThemePathMap.hashCode()
                || !mySettings.defaultViewExtension.equals(defaultViewFileExt.getModel().getSelectedItem())) {
            currentThemePathMap.clear();
            currentThemePathMap.addAll(((ThemePathMapPanel) viewPathMap).getData());
            mySettings.viewPathMap.clear();
            for (Map.Entry<String, String> entry : currentThemePathMap) {
                mySettings.viewPathMap.put(entry.getKey(), entry.getValue());
            }
            mySettings.defaultViewExtension = defaultViewFileExt.getModel().getSelectedItem().toString();

            ViewUtil.resetPathMapPatterns(myProject);
            FileBasedIndex.getInstance().requestRebuild(ViewFileIndex.identity);
        }
        mySettings.defaultViewClass = defaultViewClass.getText();
    }

    @Override
    public void reset() {
        List<Map.Entry<String, String>> data = ((ThemePathMapPanel) viewPathMap).getData();
        data.clear();
        data.addAll(currentThemePathMap);
        viewPathMap.updateUI();

        defaultViewClass.setText(mySettings.defaultViewClass);
        defaultViewFileExt.getModel().setSelectedItem(mySettings.defaultViewExtension);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        viewPathMap = new ThemePathMapPanel(myProject);
    }

    @Override
    public void disposeUIResources() {
    }
}
