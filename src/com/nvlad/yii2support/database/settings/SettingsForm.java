package com.nvlad.yii2support.database.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.UIUtil;
import com.nvlad.yii2support.migrations.entities.MigrateCommandOptions;
import com.nvlad.yii2support.migrations.ui.settings.MigrationPanel;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class SettingsForm implements Configurable {
    final private Project myProject;
    private JPanel mainPanel;
    private JTextField tablePrefixTextbox;
    private JCheckBox insertTableNamesWithCheckBox;
    private JPanel tablePanel;
    private JPanel migrationPanel;
    private Yii2SupportSettings settings;

    public SettingsForm(Project project) {
        myProject = project;

        settings = getSettings();

        tablePrefixTextbox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                adjustInputs();
            }
        });

        UIUtil.addBorder(tablePanel, IdeBorderFactory.createTitledBorder("Table Prefix Support", false));
        UIUtil.addBorder(migrationPanel, IdeBorderFactory.createTitledBorder("Migrations", false));

//        migrationPanel = new MigrationPanel(myProject);
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
        return !tablePrefixTextbox.getText().equals(settings.tablePrefix)
                || settings.insertWithTablePrefix != insertTableNamesWithCheckBox.isSelected()
                || !getOptionsList().equals(((MigrationPanel) migrationPanel).getData());
    }

    @Override
    public void apply() {
        settings.tablePrefix = tablePrefixTextbox.getText();
        settings.insertWithTablePrefix = insertTableNamesWithCheckBox.isSelected();
        settings.migrateCommandOptions = ((MigrationPanel) migrationPanel).getData();
    }

    private void createUIComponents() {
        List<MigrateCommandOptions> optionsList = new ArrayList<>(getSettings().migrateCommandOptions.size());
        for (MigrateCommandOptions migrateCommandOption : getSettings().migrateCommandOptions) {
            optionsList.add(migrateCommandOption.clone());
        }

        migrationPanel = new MigrationPanel(myProject, optionsList);
    }

    @Override
    public void reset() {
        tablePrefixTextbox.setText(settings.tablePrefix);
        insertTableNamesWithCheckBox.setSelected(settings.insertWithTablePrefix);
//        migrationTable.setText(settings.migrationTable);
//        dbConnection.setText(settings.dbConnection);
        adjustInputs();
    }

    @Override
    public void disposeUIResources() {
    }

    private Yii2SupportSettings getSettings() {
        if (settings == null) {
            settings = Yii2SupportSettings.getInstance(myProject);
        }

        return settings;
    }

    private List<MigrateCommandOptions> getOptionsList() {
        return getSettings().migrateCommandOptions;
    }
}
