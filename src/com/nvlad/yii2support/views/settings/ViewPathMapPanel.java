package com.nvlad.yii2support.views.settings;

import com.intellij.ui.AddEditRemovePanel;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Map;

public class ViewPathMapPanel extends AddEditRemovePanel<Map.Entry<String, String>> {
    public ViewPathMapPanel() {
        super(new ViewPathMapTableModel(), new LinkedList<>(), "View Path Map");
    }

    @Nullable
    @Override
    protected Map.Entry<String, String> addItem() {
        return null;
    }

    @Override
    protected boolean removeItem(Map.Entry<String, String> stringStringEntry) {
        return false;
    }

    @Nullable
    @Override
    protected Map.Entry<String, String> editItem(Map.Entry<String, String> stringStringEntry) {
        return null;
    }
}
