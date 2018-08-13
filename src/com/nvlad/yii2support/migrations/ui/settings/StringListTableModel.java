package com.nvlad.yii2support.migrations.ui.settings;

import com.nvlad.yii2support.migrations.ui.settings.entities.TableModelStringEntity;
import org.jetbrains.annotations.Nullable;

public class StringListTableModel extends com.intellij.ui.AddEditRemovePanel.TableModel<TableModelStringEntity> {
    @Override
    public int getColumnCount() {
        return 1;
    }

    @Nullable
    @Override
    public String getColumnName(int i) {
        return null;
    }

    @Override
    public Object getField(TableModelStringEntity tableModelStringEntity, int i) {
        return tableModelStringEntity.getValue();
    }

    @Override
    public boolean isEditable(int column) {
        return true;
    }

    @Override
    public void setValue(Object aValue, TableModelStringEntity data, int columnIndex) {
        data.setValue((String) aValue);
    }
}
