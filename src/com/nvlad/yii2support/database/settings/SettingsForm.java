package com.nvlad.yii2support.database.settings;

import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.SmartList;
import com.intellij.util.ui.UIUtil;
import com.nvlad.yii2support.migrations.entities.MigrateCommand;
import com.nvlad.yii2support.migrations.services.MigrationService;
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
    private JComboBox<DbSelectItem> DbSourceSelect;
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

        for (DbDataSource source : DbPsiFacade.getInstance(project).getDataSources()) {
            DbSelectItem item = new DbSelectItem(source.getUniqueId(), source.getName());
            DbSourceSelect.addItem(item);
            if(source.getUniqueId().equals(settings.dataSourceId)) {
                DbSourceSelect.setSelectedItem(item);
            }
        }

//        migrationPanel = new MigrationPanel(myProject);
    }

    private void adjustInputs() {
        if (tablePrefixTextbox.getText().length() > 0) {
            insertTableNamesWithCheckBox.setSelected(true);
        }

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
                || !getCommandList().equals(((MigrationPanel) migrationPanel).getData())
                || !(getSelectedDataSourceId().equals(settings.dataSourceId));
    }

    @Override
    public void apply() {
        settings.tablePrefix = tablePrefixTextbox.getText();
        settings.insertWithTablePrefix = insertTableNamesWithCheckBox.isSelected();
        settings.dataSourceId = getSelectedDataSourceId();

        List<MigrateCommand> newCommandList = new SmartList<>();
        for (MigrateCommand command : ((MigrationPanel) migrationPanel).getData()) {
            newCommandList.add(command.clone());
        }

        if (!settings.migrateCommands.equals(newCommandList)) {
            settings.migrateCommands = newCommandList;

            MigrationService.getInstance(myProject).sync();
        }
    }

    private void createUIComponents() {
        List<MigrateCommand> commandList = new ArrayList<>(getSettings().migrateCommands.size());
        for (MigrateCommand migrateCommand : getSettings().migrateCommands) {
            commandList.add(migrateCommand.clone());
        }

        migrationPanel = new MigrationPanel(myProject, commandList);
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

    private String getSelectedDataSourceId() {
        if(DbSourceSelect.getSelectedItem() instanceof DbSelectItem){
            return ((DbSelectItem) DbSourceSelect.getSelectedItem()).getKey();
        }
        return "";
    }

    private List<MigrateCommand> getCommandList() {
        return getSettings().migrateCommands;
    }

    static class DbSelectItem
    {
        private final String key;
        private final String name;

        public DbSelectItem(String key, String name)
        {
            this.key = key;
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name;
        }

        public String getKey()
        {
            return key;
        }
    }

}
