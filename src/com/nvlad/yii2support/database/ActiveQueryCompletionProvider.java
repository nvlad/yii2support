package com.nvlad.yii2support.database;

import com.intellij.codeInsight.ClassUtil;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.database.psi.DbTable;
import com.intellij.database.psi.DbTableImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.PhpReadWriteAccessDetector;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.ClassUtils;
import org.jetbrains.annotations.NotNull;
import com.intellij.database.view.DatabaseView;

import javax.naming.spi.ObjectFactory;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by oleg on 16.02.2017.
 */
public class ActiveQueryCompletionProvider extends com.intellij.codeInsight.completion.CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        MethodReference methodRef = DatabaseUtils.getMethodRef(completionParameters.getPosition());
        if (methodRef != null) {
            Method method = (Method)methodRef.resolve();
            int paramPosition = ClassUtils.paramIndexForElement(completionParameters.getPosition());
            if (method != null && method.getParameters().length > 0
                    &&
                    ( method.getParameters()[paramPosition].getName().equals("condition") || method.getParameters()[paramPosition].getName().startsWith("column"))
               ) {

                PhpClass phpClass = method.getContainingClass();
                PhpClass activeRecordClass = ClassUtils.getPhpClassByCallChain(methodRef);
                if (phpClass == null || activeRecordClass == null)
                    return;
                PhpIndex index = PhpIndex.getInstance(method.getProject());
                if ((ClassUtils.isClassInheritsOrEqual(phpClass,
                        ClassUtils.getClass(index, "\\yii\\db\\Query"))
                        || ClassUtils.isClassInheritsOrEqual(phpClass, ClassUtils.getClass(index, "\\yii\\db\\QueryTrait"))
                        || ClassUtils.isClassInheritsOrEqual(phpClass, ClassUtils.getClass(index, "\\yii\\db\\BaseActiveRecord"))
                )
                        && ClassUtils.isClassInheritsOrEqual(activeRecordClass,
                        ClassUtils.getClass(index, "\\yii\\db\\ActiveRecord")) ) {

                    String tableName = DatabaseUtils.getTableByActiveRecordClass(activeRecordClass);
                    if (tableName != null) {
                        tableName = ClassUtils.removeQuotes(tableName);
                        ArrayList<LookupElementBuilder> lookups = DatabaseUtils.getLookupItemsByTable(tableName, completionParameters.getPosition().getProject(), (PhpExpression) completionParameters.getPosition().getParent());
                        if (lookups != null && ! lookups.isEmpty()) {
                            completionResultSet.addAllElements(lookups);
                        } else {
                            completionResultSet.addAllElements(DatabaseUtils.getLookupItemsByAnnotations(activeRecordClass, (PhpExpression) completionParameters.getPosition().getParent()));
                        }
                    }
                }
            } else if (method != null && method.getParameters().length > 0
                    && (method.getParameters()[paramPosition].getName().startsWith("table"))) {
                ArrayList<LookupElementBuilder> lookups = DatabaseUtils.getLookupItemsTables(completionParameters.getPosition().getProject(), (PhpExpression) completionParameters.getPosition().getParent());
                completionResultSet.addAllElements(lookups);
            }
        }

    }
}
