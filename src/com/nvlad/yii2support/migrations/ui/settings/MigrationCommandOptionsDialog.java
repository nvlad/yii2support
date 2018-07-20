package com.nvlad.yii2support.migrations.ui.settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.panels.VerticalLayout;
import com.nvlad.yii2support.migrations.MigrationService;
import com.nvlad.yii2support.migrations.entities.MigrateCommandOptions;
import com.nvlad.yii2support.migrations.ui.settings.entities.TableModelStringEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MigrationCommandOptionsDialog extends DialogWrapper {
    private final Project myProject;
    private MigrateCommandOptions myEntry;
    private JPanel myPanel;
    private JBTextField myCommandField;
    private JBTextField myTableField;
    private JBTextField myDbField;
    private StringListEditPanel myMigrationPathPanel;
    private StringListEditPanel myMigrationNamespacesPanel;
    private JBCheckBox myUseTablePrefixCheckBox;
    private JBCheckBox myIsDefaultCheckBox;
    private Set<MigrationCommandDialogValidator> validators;

    MigrationCommandOptionsDialog(@Nullable Project project) {
        super(project);

        myProject = project;

        setTitle("Migrate Command");
        setResizable(false);

        initComponents();
        init();

        validators = new HashSet<>();

        this.initValidation();
    }

    void setEntry(MigrateCommandOptions options) {
        myEntry = options;
        if (myEntry == null) {
            return;
        }

        myCommandField.setText(options.command);
        myTableField.setText(options.migrationTable);
        myDbField.setText(options.db);
        myMigrationPathPanel.setStringData(options.migrationPath);
        myMigrationNamespacesPanel.setStringData(options.migrationNamespaces);
        myUseTablePrefixCheckBox.setSelected(options.useTablePrefix);
        myIsDefaultCheckBox.setSelected(options.isDefault);
    }

    MigrateCommandOptions getEntry() {
        MigrateCommandOptions entry = new MigrateCommandOptions();
        entry.command = myCommandField.getText();
        entry.migrationTable = myTableField.getText();
        entry.db = myDbField.getText();
        entry.migrationPath = myMigrationPathPanel.getStringData();
        entry.migrationNamespaces = myMigrationNamespacesPanel.getStringData();
        entry.useTablePrefix = myUseTablePrefixCheckBox.isSelected();
        entry.isDefault = myIsDefaultCheckBox.isSelected();

        return entry;
    }

    void addValidator(@NotNull MigrationCommandDialogValidator validator) {
        validators.add(validator);
    }

    boolean isNewEntry() {
        return myEntry == null;
    }

    @NotNull
    String getCommandName() {
        return myCommandField.getText();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return myPanel;
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        if (StringUtil.isEmpty(myCommandField.getText())) {
            return new ValidationInfo("Command name do not be empty.", myCommandField);
        }

        if (StringUtil.isEmpty(myTableField.getText())) {
            return new ValidationInfo("Table do not be empty.");
        }

        if (StringUtil.isEmpty(myDbField.getText())) {
            return new ValidationInfo("Database do not be empty.");
        }

        if (myMigrationPathPanel.getData().size() == 0 && myMigrationNamespacesPanel.getData().size() == 0) {
            return new ValidationInfo("Must be fill Migration Path or Migration Namespace.");
        }

//        if (myEntry != null) {
//            if (!StringUtil.equals(myCommandField.getText(), myEntry.command)) {
//                return true;
//            }
//
//            if (!StringUtil.equals(myTableField.getText(), myEntry.migrationTable)) {
//                return true;
//            }
//        }
//
        for (MigrationCommandDialogValidator validator: validators) {
            if (!validator.hasSave(this)) {
                return new ValidationInfo("Duplicate command name.");
            }
        }

        return super.doValidate();
    }

    private void initComponents() {
        myPanel = new JPanel(new VerticalLayout(5));
        Dimension dimension = new Dimension(500, -1);
        myPanel.setMinimumSize(dimension);

        myPanel.add(new JLabel("Command Name"));
        myCommandField = new JBTextField("migrate");
        myPanel.add(myCommandField);

        myPanel.add(new JLabel("Table"));
        myTableField = new JBTextField("{{%migration}}");
        myPanel.add(myTableField);

        myPanel.add(new JLabel("Database Component"));
        myDbField = new JBTextField("db");
        myPanel.add(myDbField);

        MigrationService service = MigrationService.getInstance(myProject);
        List<String> migrationPaths = new Vector<>(service.getMigrations().keySet());
        Collections.sort(migrationPaths);
        myMigrationPathPanel = new StringListEditPanel("Migration Path", migrationPaths);
        myMigrationPathPanel.setPreferredSize(new Dimension(-1, 140));
        myMigrationPathPanel.getData().add(new TableModelStringEntity("app/migrations"));
        myPanel.add(myMigrationPathPanel);

        myMigrationNamespacesPanel = new StringListEditPanel("Migration Namespaces", Arrays.asList("item_1", "item_2", "item_3", "item_4"));
        myMigrationNamespacesPanel.setPreferredSize(new Dimension(-1, 140));
        myPanel.add(myMigrationNamespacesPanel);

        myUseTablePrefixCheckBox = new JBCheckBox("Use table prefix", false);
        myPanel.add(myUseTablePrefixCheckBox);

        myIsDefaultCheckBox = new JBCheckBox("Default migration command", false);
        myPanel.add(myIsDefaultCheckBox);
    }
}
