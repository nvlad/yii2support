package com.nvlad.yii2support.views.settings;

import com.intellij.ui.AddEditRemovePanel;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ViewPathMapTableModel extends AddEditRemovePanel.TableModel<Map.Entry<String, String>> {
    @Override
    public int getColumnCount() {
        return 2;
    }

    @Nullable
    @Override
    public String getColumnName(int i) {
        return i == 0 ? "Search Path" : "Path Alias";
    }

    @Override
    public Object getField(Map.Entry<String, String> entry, int i) {
        return i == 0 ? entry.getKey() : entry.getValue();
    }
}
