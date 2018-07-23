package com.nvlad.yii2support.migrations.ui.settings;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.AbstractTableCellEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;

public class ComboBoxTableCellEditor extends AbstractTableCellEditor {
    private final ComboBox<String> myEditorComponent;
    private final List<String> myCompletionList;

    public ComboBoxTableCellEditor(List<String> completionList) {
        myEditorComponent = new ComboBox<>();
        myCompletionList = completionList;

        myEditorComponent.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (!isSelected) {
            return null;
        }

        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
        if (myCompletionList != null) {
            for (String item: myCompletionList) {
                comboBoxModel.addElement(item);
            }
        }

        comboBoxModel.setSelectedItem(value);

        myEditorComponent.setModel(comboBoxModel);
        myEditorComponent.setEditable(true);
        myEditorComponent.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) {
                return;
            }

            int index = comboBoxModel.getIndexOf(e.getItem());
            if (index == -1) {
                comboBoxModel.addElement((String) e.getItem());
            }
        });

        return myEditorComponent;
    }

    @Override
    public Object getCellEditorValue() {
        return myEditorComponent.getEditor().getItem();
    }
}
