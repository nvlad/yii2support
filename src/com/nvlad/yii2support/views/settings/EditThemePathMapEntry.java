package com.nvlad.yii2support.views.settings;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class EditThemePathMapEntry extends JPanel {
    private JPanel myMainPanel;
    private JTextField path;
    private JTextField alias;

    public EditThemePathMapEntry(String path, String alias) {
        add(myMainPanel);

        this.path.setText(path);
        this.alias.setText(alias);
    }

    @NotNull
    public String getPath() {
        return StringUtil.notNullize(this.path.getText());
    }


    @NotNull
    public String getAlias() {
        return StringUtil.notNullize(this.alias.getText());
    }

    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return this.path;
    }
}
