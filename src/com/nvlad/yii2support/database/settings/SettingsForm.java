package com.nvlad.yii2support.database.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.UIUtil;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class SettingsForm implements Configurable {
    private JPanel mainPanel;
    private JTextField tablePrefixTextbox;
    private JCheckBox insertTableNamesWithCheckBox;
    private JPanel tablePanel;
    private JTextField migrationTable;
    private JPanel migrationPanel;
    private JTextField dbConnection;
    private Yii2SupportSettings settings;

    public SettingsForm(Project project) {
        settings = Yii2SupportSettings.getInstance(project);

        tablePrefixTextbox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                adjustInputs();
            }
        });

        UIUtil.addBorder(tablePanel, IdeBorderFactory.createTitledBorder("Table Prefix Support", false));
        UIUtil.addBorder(migrationPanel, IdeBorderFactory.createTitledBorder("Migrations", false));
    }

    private void adjustInputs() {
        if (tablePrefixTextbox.getText().length() > 0)
            insertTableNamesWithCheckBox.setSelected(true);
        insertTableNamesWithCheckBox.setEnabled(tablePrefixTextbox.getText().length() == 0);
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Database";
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
        boolean migrationTableChanged = !settings.migrationTable.equals(migrationTable.getText());
        boolean dbConnectionChanged = !settings.dbConnection.equals(dbConnection.getText());

        return !tablePrefixTextbox.getText().equals(settings.tablePrefix)
                || settings.insertWithTablePrefix != insertTableNamesWithCheckBox.isSelected()
                || migrationTableChanged
                || dbConnectionChanged;
    }

    @Override
    public void apply() {
        settings.tablePrefix = tablePrefixTextbox.getText();
        settings.insertWithTablePrefix = insertTableNamesWithCheckBox.isSelected();

        settings.migrationTable = migrationTable.getText();
        settings.dbConnection = dbConnection.getText();
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    @Override
    public void reset() {
        tablePrefixTextbox.setText(settings.tablePrefix);
        insertTableNamesWithCheckBox.setSelected(settings.insertWithTablePrefix);
        migrationTable.setText(settings.migrationTable);
        dbConnection.setText(settings.dbConnection);
        adjustInputs();
    }

    @Override
    public void disposeUIResources() {
    }
}
