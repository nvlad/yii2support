package com.nvlad.yii2support.migrations.ui.settings;

import com.intellij.ui.AddEditRemovePanel;
import com.nvlad.yii2support.migrations.entities.MigrateCommandOptions;
import org.jetbrains.annotations.Nullable;

public class MigrateCommandOptionsTableModel extends AddEditRemovePanel.TableModel<MigrateCommandOptions> {
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
    public Object getField(MigrateCommandOptions options, int i) {
        switch (i) {
            case 0:
                return options.command;
            case 1:
                return options.migrationTable;
            case 2:
                return options.db;
        }

        return null;
    }
}
