package com.nvlad.yii2support.migrations.ui.settings;

import com.intellij.ui.AddEditRemovePanel;
import com.nvlad.yii2support.migrations.entities.MigrateCommand;
import org.jetbrains.annotations.Nullable;

public class MigrateCommandTableModel extends AddEditRemovePanel.TableModel<MigrateCommand> {
    private final String[] columnNames = new String[]{"Command", "Table", "Component"};

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Nullable
    @Override
    public String getColumnName(int i) {
        return columnNames[i];
    }

    @Override
    public Object getField(MigrateCommand command, int i) {
        switch (i) {
            case 0:
                return command.command;
            case 1:
                return command.migrationTable;
            case 2:
                return command.db;
        }

        return null;
    }
}
