package com.nvlad.yii2support.database;

import com.intellij.database.model.DasColumn;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.database.psi.DbTable;
import com.intellij.database.view.DatabaseView;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;

/**
 * Created by oleg on 23.03.2017.
 */
public class DatabaseUtils {
    public static DatabaseLookup[] getLookupItemsByTable(String table, Project project) {
        DbPsiFacade facade =  DbPsiFacade.getInstance(project);
        DbDataSource source = (DbDataSource)facade.getDataSources().toArray()[0];
        for (Object item: source.getModel().traverser()) {
            if (item instanceof DbTable && ((DbTable)item).getName().equals(table)) {
                TableInfo tableInfo = new TableInfo((DbTable) item);
                ArrayList<DatabaseLookup> list = new ArrayList<>();
                for (DasColumn column: tableInfo.getColumns()) {
                    list.add(new DatabaseLookup(column.getName(), column.getDataType().typeName));
                }
                return (DatabaseLookup[])list.toArray();
            }
        }
        return new DatabaseLookup[0];
    }
}
