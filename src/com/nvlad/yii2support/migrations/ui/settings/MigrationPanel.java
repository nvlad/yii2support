package com.nvlad.yii2support.migrations.ui.settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.AddEditRemovePanel;
import com.nvlad.yii2support.migrations.entities.MigrateCommandOptions;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

public class MigrationPanel extends AddEditRemovePanel<MigrateCommandOptions> {
    final private Project myProject;

    public MigrationPanel(Project project, List<MigrateCommandOptions> optionsList) {
        super(new MigrateCommandOptionsTableModel(), optionsList);

        myProject = project;
        getTable().setTableHeader(new JTableHeader(getTable().getColumnModel()));
        TableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (getData().get(row).isDefault) {
                    component.setFont(component.getFont().deriveFont(Font.BOLD));
                }

                return component;
            }
        };

        setRenderer(0, renderer);
        setRenderer(1, renderer);
        setRenderer(2, renderer);
    }

    @Nullable
    @Override
    protected MigrateCommandOptions addItem() {
        return showEditor(myProject, null);
    }

    @Override
    protected boolean removeItem(MigrateCommandOptions migrateCommandOptions) {
        if (migrateCommandOptions.isDefault) {
            Messages.showErrorDialog("Do not remove default migrate command.", "Error");

            return false;
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
            MigrateCommandOptions entry = dialog.getEntry();
            if (entry.isDefault) {
                for (MigrateCommandOptions option : getData()) {
                    if (option.isDefault) {
                        option.isDefault = false;
                        break;
                    }
                }
            }

            return entry;
        }

        return null;
    }
}
