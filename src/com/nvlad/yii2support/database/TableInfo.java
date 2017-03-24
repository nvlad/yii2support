package com.nvlad.yii2support.database;

import com.google.common.collect.Lists;
import com.intellij.database.DatabaseDataKeys;
import com.intellij.database.dataSource.DatabaseArtifactManager;
import com.intellij.database.dataSource.DatabaseDriver;
import com.intellij.database.dataSource.DatabaseDriverManager;
import com.intellij.database.model.DasColumn;
import com.intellij.database.model.DatabaseSystem;
import com.intellij.database.psi.DbTable;
import com.intellij.database.util.DasUtil;
import com.intellij.database.vfs.DatabaseElementVirtualFileImpl;
import com.intellij.database.view.DatabaseStructure;
import com.intellij.database.view.editors.DatabaseTableEditor;
import com.intellij.util.containers.JBIterable;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by oleg on 23.03.2017.
 */
public class TableInfo {
    private final DbTable tableElement;

    private List<DasColumn> columns = new ArrayList<DasColumn>();

    private List<String> primaryKeys = new ArrayList<String>();

    public TableInfo(DbTable tableElement) {
        this.tableElement = tableElement;
        List<DasColumn> columns = new ArrayList<DasColumn>();

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
        Set<String> pKNameSet = new HashSet<String>();
        for (String pkName : getPrimaryKeys()) {
            pKNameSet.add(pkName);
        }

        List<DasColumn> ret = new ArrayList<DasColumn>();
        for (DasColumn column : columns) {
            if (!pKNameSet.contains(column.getName())) {
                ret.add(column);
            }
        }

        return ret;
    }
}
