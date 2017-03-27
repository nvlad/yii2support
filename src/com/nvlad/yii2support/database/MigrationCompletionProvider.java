package com.nvlad.yii2support.database;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.ClassUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Created by oleg on 24.03.2017.
 */
public class MigrationCompletionProvider extends com.intellij.codeInsight.completion.CompletionProvider<CompletionParameters>  {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        Object possibleMethodRef = completionParameters.getPosition().getParent().getParent().getParent();
        if (possibleMethodRef instanceof MethodReference) {
            MethodReference methodRef = (MethodReference)possibleMethodRef;
            Method classMethod = (Method)methodRef.resolve();
            if (classMethod == null || !( classMethod.getParent() instanceof  PhpClass))
                return;
            PhpClass phpClass = (PhpClass)classMethod.getParent();
            Project project = completionParameters.getPosition().getProject();
            PhpIndex index = PhpIndex.getInstance(project);
            if (phpClass != null && ClassUtils.isClassInheritsOrEqual(phpClass, ClassUtils.getClass(index, "\\yii\\db\\Migration"))) {
                Method method = (Method)methodRef.resolve();
                if (method != null) {
                    int paramIndex = ClassUtils.paramIndexForElement(completionParameters.getPosition());
                    if (method.getParameters()[paramIndex].getName().equals("table") || method.getParameters()[paramIndex].getName().equals("refTable")) {
                        completionResultSet.addAllElements(DatabaseUtils.getLookupItemsTables(project,  (PhpExpression) completionParameters.getPosition().getParent()));
                    } else if  (method.getParameters()[paramIndex].getName().startsWith("column")) {
                        //methodRef.getParameterList().getParameters()[0].getText()

                        for (int i = 0; method.getParameters().length > i ; i++) {
                            Parameter param = method.getParameters()[i];
                            if (param.getName().equals("table")) {
                                String tableName = null;
                                //((FieldReference)methodRef.getParameters()[i]).resolve().getChildren()[0].getText()
                                PsiElement methodParam = methodRef.getParameters()[i];
                                if (methodParam instanceof FieldReference) {
                                    Object possibleField = ((FieldReference)methodRef.getParameters()[i]).resolve();
                                    if (possibleField instanceof Field) {
                                        Field field = (Field)possibleField;
                                        if (field.getChildren().length > 0) {
                                            if (field.getChildren()[0] instanceof StringLiteralExpression) {
                                                tableName = field.getChildren()[0].getText();
                                            }
                                        }
                                    }
                                } else if (methodParam instanceof StringLiteralExpression) {
                                    tableName = methodRef.getParameters()[i].getText();
                                }
                                tableName = ClassUtils.removeQuotes(tableName);
                                tableName = DatabaseUtils.clearTablePrefixTags(tableName);
                                completionResultSet.addAllElements(DatabaseUtils.getLookupItemsByTable(tableName, project, (PhpExpression) completionParameters.getPosition().getParent()));
                            }
                        }
                    }
                }
            }

        }
    }
}
