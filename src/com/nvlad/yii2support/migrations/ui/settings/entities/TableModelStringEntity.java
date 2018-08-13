package com.nvlad.yii2support.migrations.ui.settings.entities;

import java.util.List;
import java.util.Vector;

public class TableModelStringEntity {
    private String myString;

    public TableModelStringEntity(String item) {
        myString = item;
    }

    public void setValue(String value) {
        myString = value;
    }

    public String getValue() {
        return myString;
    }

    public static List<TableModelStringEntity> fromList(List<String> items) {
        List<TableModelStringEntity> result = new Vector<>();
        for (String item: items) {
            result.add(new TableModelStringEntity(item));
        }

        return result;
    }

    public static List<String> toStringList(List<TableModelStringEntity> items) {
        List<String> result = new Vector<>();
        for (TableModelStringEntity item: items) {
            result.add(item.getValue());
        }

        return result;
    }
}
