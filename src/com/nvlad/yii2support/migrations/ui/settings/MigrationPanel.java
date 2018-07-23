package com.nvlad.yii2support.migrations.ui.settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.AddEditRemovePanel;
import com.nvlad.yii2support.migrations.entities.MigrateCommand;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;

public class MigrationPanel extends AddEditRemovePanel<MigrateCommand> {
    final private Project myProject;

    public MigrationPanel(Project project, List<MigrateCommand> commandList) {
        super(new MigrateCommandTableModel(), commandList);

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
    protected MigrateCommand addItem() {
        return showEditor(myProject, null);
    }

    @Override
    protected boolean removeItem(MigrateCommand command) {
        if (command.isDefault) {
            Messages.showErrorDialog("Do not remove default migrate command.", "Error");

            return false;
        }

        return true;
    }

    @Nullable
    @Override
    protected MigrateCommand editItem(MigrateCommand command) {
        return showEditor(myProject, command);
    }

    @Nullable
    private MigrateCommand showEditor(Project project, MigrateCommand command) {
        MigrationCommandDialog dialog = new MigrationCommandDialog(project);

        dialog.addValidator(optionsDialog -> {
            if (optionsDialog.isNewEntry()) {
                final String commandName = optionsDialog.getCommandName();
                return getData().stream().noneMatch(c -> commandName.equals(c.command));
            }

            return true;
        });

        dialog.setEntry(command);
        dialog.show();

        if (dialog.isOK()) {
            MigrateCommand entry = dialog.getEntry();
            if (entry.isDefault) {
                for (MigrateCommand migrateCommand : getData()) {
                    if (migrateCommand.isDefault) {
                        migrateCommand.isDefault = false;
                        break;
                    }
                }
            }

            return entry;
        }

        return null;
    }
}
