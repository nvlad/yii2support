package com.nvlad.yii2support.views.settings;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.Map;

public class ViewPathMapTableModel implements TableModel {
    private Map<String, String> myData;

    public ViewPathMapTableModel(Map<String, String> data) {
        this.myData = data;
    }

    @Override
    public int getRowCount() {
        return myData.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return "test";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return "skdfjksldfksdf";
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

    }

    @Override
    public void addTableModelListener(TableModelListener l) {

    }

    @Override
    public void removeTableModelListener(TableModelListener l) {

    }
}
