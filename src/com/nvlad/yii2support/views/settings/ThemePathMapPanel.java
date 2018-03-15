package com.nvlad.yii2support.views.settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.AddEditRemovePanel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ThemePathMapPanel extends AddEditRemovePanel<Map.Entry<String, String>> {
    private Project myProject;

    ThemePathMapPanel(Project project) {
        super(new ThemePathMapTableModel(), new LinkedList<>(), "Themes Path Map");

        myProject = project;
    }

    @Nullable
    @Override
    protected Map.Entry<String, String> addItem() {
        EditThemePathMapDialog dialog = new EditThemePathMapDialog(myProject, "@app/themes/*", "@app/views");
        dialog.show();

        if (dialog.getExitCode() == DialogWrapper.CANCEL_EXIT_CODE) {
            return null;
        }

        return getEntryWithStrings(dialog.getPath(), dialog.getAlias());
    }

    @Override
    protected boolean removeItem(Map.Entry<String, String> entry) {
        return true;
    }

    @Nullable
    @Override
    protected Map.Entry<String, String> editItem(Map.Entry<String, String> entry) {
        EditThemePathMapDialog dialog = new EditThemePathMapDialog(myProject, entry.getKey(), entry.getValue());
        dialog.show();

        if (dialog.getExitCode() == DialogWrapper.CANCEL_EXIT_CODE) {
            return null;
        }

        return getEntryWithStrings(dialog.getPath(), dialog.getAlias());
    }

    protected void initPanel() {
        this.setLayout(new BorderLayout());
        JPanel panel = ToolbarDecorator
                .createDecorator(this.getTable())
                .setAddAction(button -> ThemePathMapPanel.this.doAdd())
                .setRemoveAction(button -> ThemePathMapPanel.this.doRemove())
                .setEditAction(button -> {
                    if (ThemePathMapPanel.this.getTable().isEditing()) {
                        ThemePathMapPanel.this.getTable().getCellEditor().stopCellEditing();
                    } else {
                        ThemePathMapPanel.this.doEdit();
                    }
                })
                .setMoveUpAction(button -> {
                    List<Map.Entry<String, String>> data = this.getData();
                    JBTable table = this.getTable();
                    int[] selectedRows = table.getSelectedRows();
                    table.clearSelection();
                    for (int index : selectedRows) {
                        Map.Entry<String, String> entry = data.get(index - 1);
                        data.set(index - 1, data.get(index));
                        data.set(index, entry);
                        table.addRowSelectionInterval(index - 1, index - 1);
                    }
                    this.updateUI();
                })
                .setMoveDownAction(button -> {
                    List<Map.Entry<String, String>> data = this.getData();
                    JBTable table = this.getTable();
                    int[] selectedRows = ArrayUtil.reverseArray(table.getSelectedRows());
                    table.clearSelection();
                    for (int index : selectedRows) {
                        Map.Entry<String, String> entry = data.get(index + 1);
                        data.set(index + 1, data.get(index));
                        data.set(index, entry);
                        table.addRowSelectionInterval(index + 1, index + 1);
                    }
                    this.updateUI();
                })
                .createPanel();
        this.add(panel, "Center");
        String label = this.getLabelText();
        if (label != null) {
            UIUtil.addBorder(panel, IdeBorderFactory.createTitledBorder(label, false));
        }
    }

    @NotNull
    private Map.Entry<String, String> getEntryWithStrings(String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return map.entrySet().iterator().next();
    }
}
