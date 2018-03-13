package com.nvlad.yii2support.views.settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class EditThemePathMapDialog extends DialogWrapper {
    private final EditThemePathMapEntry myPanel;

    protected EditThemePathMapDialog(@Nullable Project project, String path, String alias) {
        super(project);
        setTitle("Edit Path Map");

        myPanel = new EditThemePathMapEntry(path, alias);
        init();
    }

    @NotNull
    public String getPath() {
        return myPanel.getPath();
    }

    @NotNull
    public String getAlias() {
        return myPanel.getAlias();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return myPanel;
    }
}
