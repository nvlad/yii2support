package com.nvlad.yii2support.ui.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.nvlad.yii2support.common.YiiApplicationUtils;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import com.nvlad.yii2support.views.index.ViewFileIndex;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SettingsForm implements Configurable {
    private JPanel mainPanel;
    private TextFieldWithBrowseButton yiiRootPath;
    private JLabel yiiRootPathLabel;
    final private Project myProject;
    final private Yii2SupportSettings settings;

    public SettingsForm(Project project) {
        myProject = project;
        settings = Yii2SupportSettings.getInstance(project);

        yiiRootPathLabel.setLabelFor(yiiRootPath.getTextField());
        yiiRootPath.setButtonEnabled(true);
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        yiiRootPath.addBrowseFolderListener(new TextBrowseFolderListener(fileChooserDescriptor));
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
        return mainPanel;
    }

    @Override
    public boolean isModified() {
        return !yiiRootPath.getText().trim().equals(StringUtil.notNullize(settings.yiiRootPath));
    }

    @Override
    public void apply() {
        settings.yiiRootPath = StringUtil.nullize(yiiRootPath.getText().trim());

        YiiApplicationUtils.resetYiiRootPath(myProject);
        FileBasedIndex.getInstance().requestRebuild(ViewFileIndex.identity);
    }

    @Override
    public void reset() {
        yiiRootPath.setText(StringUtil.notNullize(settings.yiiRootPath));
    }

    @Override
    public void disposeUIResources() {

    }
}
