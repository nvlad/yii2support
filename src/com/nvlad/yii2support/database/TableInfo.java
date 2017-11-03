package com.nvlad.yii2support.database;

import com.google.common.collect.Lists;
import com.intellij.database.model.DasColumn;
import com.intellij.database.model.DasTable;
import com.intellij.database.util.DasUtil;
import com.intellij.util.containers.JBIterable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by oleg on 23.03.2017.
 */
public class TableInfo {
    private final DasTable tableElement;

    private List<DasColumn> columns = new ArrayList<>();

    private List<String> primaryKeys = new ArrayList<>();

    public TableInfo(DasTable tableElement) {
        this.tableElement = tableElement;
        List<DasColumn> columns = new ArrayList<>();

        JBIterable<? extends DasColumn> columnsIter = DasUtil.getColumns(tableElement);
        List<? extends DasColumn> dasColumns = columnsIter.toList();
        for (DasColumn dasColumn : dasColumns) {
            columns.add(dasColumn);

            if (DasUtil.isPrimary(dasColumn)) {
                primaryKeys.add(dasColumn.getName());
            }
        }

        this.columns = columns;
    }

    public String getTableName() {
        return tableElement.getName();
    }

    public List<DasColumn> getColumns() {
        return columns;
    }

    public List<String> getColumnsName() {
        List<String> columnsName = Lists.newArrayList();
        for (DasColumn column : columns) {
            columnsName.add(column.getName());
        }
        return columnsName;
    }

    public List<String> getPrimaryKeys() {
        return this.primaryKeys;
    }

    public List<DasColumn> getNonPrimaryColumns() {
        Set<String> pKNameSet = new HashSet<>();
        for (String pkName : getPrimaryKeys()) {
            pKNameSet.add(pkName);
        }

        List<DasColumn> ret = new ArrayList<>();
        for (DasColumn column : columns) {
            if (!pKNameSet.contains(column.getName())) {
                ret.add(column);
            }
        }

        return ret;
    }
}
