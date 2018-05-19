package com.nvlad.yii2support.views.settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class EditThemePathMapDialog extends DialogWrapper {
    private final EditPathMapEntryPanel myPanel;

    EditThemePathMapDialog(@Nullable Project project, String path, String alias) {
        super(project);
        setTitle("Edit Path Map");

        this.setResizable(false);

        myPanel = new EditPathMapEntryPanel(path, alias);
        myPanel.setAliasLabel("Path Mask:");
        myPanel.setValueLabel("Alias:");

        init();
    }

    @NotNull
    public String getPath() {
        return myPanel.getAlias();
    }

    @NotNull
    public String getAlias() {
        return myPanel.getValue();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return myPanel;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return myPanel.getPreferredFocusedComponent();
    }
}
