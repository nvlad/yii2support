package com.nvlad.yii2support.migrations.ui.settings;

import com.intellij.openapi.project.Project;
import com.intellij.ui.AddEditRemovePanel;
import com.nvlad.yii2support.migrations.entities.MigrateCommandOptions;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.JTableHeader;
import java.security.InvalidParameterException;
import java.util.List;

public class MigrationPanel extends AddEditRemovePanel<MigrateCommandOptions> {
    final private Project myProject;
    public MigrationPanel(Project project, List<MigrateCommandOptions> optionsList) {
        super(new MigrateCommandOptionsTableModel(), optionsList);

        myProject = project;
        getTable().setTableHeader(new JTableHeader(getTable().getColumnModel()));
    }

    @Nullable
    @Override
    protected MigrateCommandOptions addItem() {
        return showEditor(myProject, null);
    }

    @Override
    protected boolean removeItem(MigrateCommandOptions migrateCommandOptions) {
        if (migrateCommandOptions.isDefault) {
            throw new InvalidParameterException("Do not delete default migrate command.");
        }

        return true;
    }

    @Nullable
    @Override
    protected MigrateCommandOptions editItem(MigrateCommandOptions migrateCommandOptions) {
        return showEditor(myProject, migrateCommandOptions);
    }

    @Nullable
    private MigrateCommandOptions showEditor(Project project, MigrateCommandOptions migrateCommandOptions) {
        MigrationCommandOptionsDialog dialog = new MigrationCommandOptionsDialog(project);

        dialog.addValidator(optionsDialog -> {
            if (optionsDialog.isNewEntry()) {
                final String commandName = optionsDialog.getCommandName();
                return getData().stream().noneMatch(options -> commandName.equals(options.command));
            }

            return true;
        });

        dialog.setEntry(migrateCommandOptions);
        dialog.show();

        if (dialog.isOK()) {
            return dialog.getEntry();
        }

        return null;
    }
}
