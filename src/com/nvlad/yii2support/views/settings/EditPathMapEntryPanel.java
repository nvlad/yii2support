package com.nvlad.yii2support.views.settings;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.uiDesigner.core.Spacer;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.awt.*;

public class EditPathMapEntryPanel extends JBPanel {
    private final JBLabel aliasLabel;
    private final JBLabel valueLabel;
    private final JBTextField aliasTextField;
    private final JBTextField valueTextField;

    EditPathMapEntryPanel() {
        setLayout(new VerticalLayout(5));
        Dimension dimension = new Dimension(300, -1);
        setMinimumSize(dimension);

        aliasLabel = new JBLabel("Alias:");
        aliasTextField = new JBTextField();
        valueLabel = new JBLabel("Path:");
        valueTextField = new JBTextField();

        add(aliasLabel);
        add(aliasTextField);
        add(new Spacer() {
            @Override
            public Dimension getMinimumSize() {
                return new Dimension(0, 5);
            }
        });
        add(valueLabel);
        add(valueTextField);
    }

    EditPathMapEntryPanel(String alias, String value) {
        this();

        aliasTextField.setText(alias);
        valueTextField.setText(value);
    }

    void setAliasLabel(String label) {
        aliasLabel.setText(label);
    }

    void setValueLabel(String label) {
        valueLabel.setText(label);
    }

    String getAlias() {
        return aliasTextField.getText();
    }

    String getValue() {
        return valueTextField.getText();
    }


    public JComponent getPreferredFocusedComponent() {
        return aliasTextField;
    }
}
