package com.nvlad.yii2support.database;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.ClassUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oleg on 24.03.2017.
 */
public class MigrationCompletionProvider extends com.intellij.codeInsight.completion.CompletionProvider<CompletionParameters>  {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {

       MethodReference methodRef = ClassUtils.getMethodRef(completionParameters.getPosition(), 10);
        if (methodRef != null) {

            String currentColumn = completionResultSet.getPrefixMatcher().getPrefix();
            if (currentColumn.indexOf(',') != -1) {
                currentColumn = currentColumn.substring(currentColumn.lastIndexOf(',') + 1).trim();
                completionResultSet = completionResultSet.withPrefixMatcher(currentColumn);
            }

            Method classMethod = (Method)methodRef.resolve();
            if (classMethod == null || !( classMethod.getParent() instanceof  PhpClass))
                return;
            PhpClass phpClass = (PhpClass)classMethod.getParent();
            Project project = completionParameters.getPosition().getProject();
            PhpIndex index = PhpIndex.getInstance(project);
            if (phpClass != null &&
                    (ClassUtils.isClassInheritsOrEqual(phpClass, ClassUtils.getClass(index, "\\yii\\db\\Migration"))
                    || ClassUtils.isClassInheritsOrEqual(phpClass, ClassUtils.getClass(index, "\\yii\\db\\Connection"))
                    || ClassUtils.isClassInheritsOrEqual(phpClass, ClassUtils.getClass(index, "\\yii\\db\\Command"))
                    )) {
                Method method = (Method)methodRef.resolve();
                if (method != null) {
                    int paramIndex = ClassUtils.paramIndexForElement(completionParameters.getPosition());
                    if ( paramIndex == -1 || method.getParameters().length-1 < paramIndex)
                        return;
                    Parameter currentParam = method.getParameters()[paramIndex];
                    String currentParamName = currentParam.getName();
                    if (currentParamName.equals("table") || currentParamName.equals("refTable") ||  currentParamName.equals("condition") ||
                            currentParamName.equals("sql")) {
                        List<LookupElementBuilder> lookups =  DatabaseUtils.getLookupItemsTables(project,  (PhpExpression) completionParameters.getPosition().getParent());
                        completionResultSet.addAllElements(lookups);
                    } else if  (currentParamName.startsWith("column")) {
                        if (currentParamName.equals("column") && methodRef.getParameters().length > paramIndex ) {
                            PsiElement element = methodRef.getParameters()[paramIndex];
                            String content = element.getText();
                            if (content.indexOf(',') >= 0)
                                return;
                        }

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
                                ArrayList<LookupElementBuilder> completionList = DatabaseUtils.getLookupItemsByTable(tableName, project, (PhpExpression) completionParameters.getPosition().getParent());
                                if (completionList != null) {
                                    completionResultSet.addAllElements(completionList);
                                  }

                            }
                        }
                    }
                }
            }

        }
    }
}
