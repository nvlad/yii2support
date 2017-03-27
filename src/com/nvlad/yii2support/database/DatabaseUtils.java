package com.nvlad.yii2support.database;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.database.model.DasColumn;
import com.intellij.database.model.DasObject;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.database.psi.DbTable;
import com.intellij.database.view.DatabaseView;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocProperty;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.ClassUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Created by oleg on 23.03.2017.
 */
public class DatabaseUtils {
    @Nullable
    public static ArrayList<LookupElementBuilder> getLookupItemsByTable(String table, Project project, PhpExpression position) {
        ArrayList<LookupElementBuilder> list = new ArrayList<>();
        if (table == null ||table.isEmpty())
            return list;
        DbPsiFacade facade =  DbPsiFacade.getInstance(project);
        List<DbDataSource> dataSources = facade.getDataSources();
        for (DbDataSource source: dataSources) {
            for (Object item : source.getModel().traverser().children(source.getModel().getCurrentRootNamespace()) ) {
                if (item instanceof DbTable && ((DbTable) item).getName().equals(table)) {
                    TableInfo tableInfo = new TableInfo((DbTable) item);
                    for (DasColumn column : tableInfo.getColumns()) {
                        list.add(DatabaseUtils.buildLookup(column, position));
                    }
                }
            }
        }
        return list;
    }

    public static ArrayList<LookupElementBuilder> getLookupItemsTables(Project project, PhpExpression position) {
        DbPsiFacade facade =  DbPsiFacade.getInstance(project);
        List<DbDataSource> dataSources = facade.getDataSources();
        ArrayList<LookupElementBuilder> list = new  ArrayList<>();
        for (DbDataSource source: dataSources) {
            for (Object item : source.getModel().traverser().children(source.getModel().getCurrentRootNamespace()) ) {
                if (item instanceof DbTable) {
                    list.add(DatabaseUtils.buildLookup(item, position));
                }
            }
        }
        return list;
    }

    @Nullable
    public static MethodReference getMethodRef(PsiElement element) {
        PsiElement parent = element.getParent();
         if (parent == null)
             return null;
          else if (parent instanceof MethodReference)
            return (MethodReference)parent;
        else return getMethodRef(parent);
    }

    public static ArrayList<LookupElementBuilder> getLookupItemsByAnnotations(PhpClass phpClass, PhpExpression position) {
        if (phpClass == null)
            return null;
        final ArrayList<LookupElementBuilder> result = new ArrayList<>();
        final Field[] fields = phpClass.getOwnFields();
        for (Field field : fields) {
            if (field instanceof PhpDocProperty) {
                result.add(buildLookup(field, position));
            }
        }

        return result;
    }

    @NotNull
    static private LookupElementBuilder buildLookup(Object field, PhpExpression position) {
        String lookupString = "-";
        if (field instanceof DasObject)
            lookupString = ((DasObject)field).getName();
        if (field instanceof Field) {
            lookupString = ((Field) field).getName();
        }
        LookupElementBuilder builder =  LookupElementBuilder.create(field, lookupString)
                .withInsertHandler((insertionContext, lookupElement) -> {

                    Document document = insertionContext.getDocument();
                    int insertPosition = insertionContext.getSelectionEndOffset();

                    /*
                    if (position.getParent().getParent() instanceof ArrayCreationExpression) {
                        document.insertString(insertPosition + 1, " => ");
                        insertPosition += 5;
                        insertionContext.getEditor().getCaretModel().getCurrentCaret().moveToOffset(insertPosition);
                    }

                    if (position instanceof StringLiteralExpression && !(position.getParent().getParent() instanceof ArrayHashElement)) {
                        document.insertString(insertPosition , " ");
                        insertPosition += 1;
                        insertionContext.getEditor().getCaretModel().getCurrentCaret().moveToOffset(insertPosition);
                    }
                    */
                });
        if (field instanceof Field) {
            builder.withIcon(((Field) field).getIcon());
            builder = builder.withTypeText(((Field) field).getType().toString());
        }
        if (field instanceof DasColumn) {
            DasColumn column = (DasColumn)field;
            builder = builder.withTypeText(column.getDataType().typeName)
            .withTailText("(" + column.getDbParent().getDbParent().getName() + '.' +column.getTableName() + ")", true);
        }
        return builder;
    }

    @Nullable
    public static String getTableByActiveRecordClass(PhpClass phpClass) {
        Method[] methods = phpClass.getOwnMethods();
        for (Method method: methods) {
            if (method.getName().equals("tableName")) {
                for (PsiElement elem: method.getChildren()) {
                    if (elem.getChildren().length > 0) {
                        for (PsiElement element: elem.getChildren()) {
                            if (element instanceof PhpReturn) {
                                if ((element).getChildren().length > 0)
                                    return clearTablePrefixTags( (element).getChildren()[0].getText());
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static String clearTablePrefixTags(String str) {
        return str.replace("{{%", "").replace("}}", "").replace("{{", "");
    }


}
