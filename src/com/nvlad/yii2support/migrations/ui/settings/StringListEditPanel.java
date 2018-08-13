package com.nvlad.yii2support.migrations.ui.settings;

import com.intellij.ui.AddEditRemovePanel;
import com.intellij.util.SmartList;
import com.nvlad.yii2support.migrations.ui.settings.entities.TableModelStringEntity;
import org.jetbrains.annotations.Nullable;

import javax.swing.table.TableColumn;
import java.util.List;

public class StringListEditPanel extends AddEditRemovePanel<TableModelStringEntity> {

    StringListEditPanel(String label, List<String> completionList) {
        super(new StringListTableModel(), new SmartList<>(), label);

        TableColumn tableColumn = getTable().getColumnModel().getColumn(0);
        tableColumn.setCellEditor(new ComboBoxTableCellEditor(completionList));
    }

    public void setStringData(List<String> data) {
        setData(TableModelStringEntity.fromList(data));
    }

    public List<String> getStringData() {
        return TableModelStringEntity.toStringList(getData());
    }

    @Nullable
    @Override
    protected TableModelStringEntity addItem() {
        return new TableModelStringEntity("");
    }

    @Override
    protected boolean removeItem(TableModelStringEntity s) {
        return true;
    }

    @Nullable
    @Override
    protected TableModelStringEntity editItem(TableModelStringEntity s) {
        int index = getData().indexOf(s);
        getTable().editCellAt(index, 0);
        return s;
    }
}
