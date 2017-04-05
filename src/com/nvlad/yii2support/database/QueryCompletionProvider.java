package com.nvlad.yii2support.database;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.nvlad.yii2support.common.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by oleg on 16.02.2017.
 */
public class QueryCompletionProvider extends com.intellij.codeInsight.completion.CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        MethodReference methodRef = ClassUtils.getMethodRef(completionParameters.getPosition(), 10);
        if (methodRef != null) {
            String prefix = completionResultSet.getPrefixMatcher().getPrefix();
            completionResultSet = adjustPrefix(',', completionResultSet);
            completionResultSet = adjustPrefix('.', completionResultSet);
            completionResultSet = adjustPrefix('{', completionResultSet);
            completionResultSet = adjustPrefix('[', completionResultSet);
            completionResultSet = adjustPrefix('%', completionResultSet);
            completionResultSet = adjustPrefix('(', completionResultSet);

            Method method = (Method) methodRef.resolve();
            if (method == null) {
                return;
            }

            int paramPosition = ClassUtils.paramIndexForElement(completionParameters.getPosition());

            PhpClass phpClass = method.getContainingClass();
            if (phpClass == null)
                return;

            PhpIndex index = PhpIndex.getInstance(method.getProject());
            if ( (ClassUtils.isClassInheritsOrEqual(phpClass, ClassUtils.getClass(index, "\\yii\\db\\Query"))
                    || ClassUtils.isClassInheritsOrEqual(phpClass, ClassUtils.getClass(index, "\\yii\\db\\QueryTrait"))
                    || ClassUtils.isClassInheritsOrEqual(phpClass, ClassUtils.getClass(index, "\\yii\\db\\BaseActiveRecord")) )) {


                PhpClass activeRecordClass = ClassUtils.getPhpClassByCallChain(methodRef);

                /*----- ActiveQuery condition and column paramters ----*/
                Project project = completionParameters.getPosition().getProject();
                if (activeRecordClass != null && paramPosition >= 0 &&
                        method.getParameters().length > paramPosition &&
                        method.getParameters().length > 0 &&
                        (method.getParameters()[paramPosition].getName().equals("condition") ||
                                method.getParameters()[paramPosition].getName().startsWith("column"))) {

                    String tableName = getTable(prefix, activeRecordClass);
                    if (tableName == null || tableName.isEmpty())
                        return;

                    ArrayList<LookupElementBuilder> lookups = DatabaseUtils.getLookupItemsByTable(tableName, project, (PhpExpression) completionParameters.getPosition().getParent());
                    if (lookups != null && !lookups.isEmpty()) {
                        addAllElementsWithPriority(lookups, completionResultSet, 2, true); // columns
                    } else {
                        ArrayList<LookupElementBuilder> items = DatabaseUtils.getLookupItemsByAnnotations(activeRecordClass, (PhpExpression) completionParameters.getPosition().getParent());
                        addAllElementsWithPriority(lookups, completionResultSet, 2, true); // fields
                    }
                    if (!isTabledPrefix(prefix)) {
                        lookups = DatabaseUtils.getLookupItemsTables(project, (PhpExpression) completionParameters.getPosition().getParent());
                        addAllElementsWithPriority(lookups, completionResultSet, 1); // tables
                    }
                /*---  table parameter -----*/
                } else if (method.getParameters().length > paramPosition &&
                        method.getParameters().length > 0 &&
                        (method.getParameters()[paramPosition].getName().startsWith("table"))) {

                    // cancel codecompletion in case of "table" have ,
                    if (method.getParameters()[paramPosition].getName().equals("table") && methodRef.getParameters().length > paramPosition) {
                        PsiElement element = methodRef.getParameters()[paramPosition];
                        String content = element.getText();
                        if (content.indexOf(',') >= 0)
                            return;
                    }

                    if (isTabledPrefix(prefix)) {
                        ArrayList<LookupElementBuilder> lookups = DatabaseUtils.getLookupItemsTables(project, (PhpExpression) completionParameters.getPosition().getParent());
                        addAllElementsWithPriority(lookups, completionResultSet, 1); // tables
                    }
                    /*---  Query -----*/
                } else if (activeRecordClass == null &&
                        (method.getParameters()[paramPosition].getName().equals("condition") || method.getParameters()[paramPosition].getName().startsWith("column"))) {
                    ArrayList<LookupElementBuilder> lookups = null;
                    PhpExpression expr = (PhpExpression) completionParameters.getPosition().getParent();
                    if (isTabledPrefix(prefix)) {
                        lookups = DatabaseUtils.getLookupItemsByTable(getTable(prefix, null), project, expr);
                    } else {
                        lookups = DatabaseUtils.getLookupItemsTables(project, expr);
                    }
                    addAllElementsWithPriority(lookups, completionResultSet, 1); // tables
                }
            }
        }

    }

    @Nullable
    private String getTable(String stringToComplete, @Nullable PhpClass activeRecordClass) {
        if (stringToComplete.length() > 2 && stringToComplete.contains(".")) {
            // match "{{%table}}.[[co", "{{%table}}.[[", "{{%table}}.", "{{%table}}.col", "{{table}}.", "table.[[col",
            // "table.[[", "table.col" and "table." at end of string and return "tn" group with table name
            Pattern pattern = Pattern.compile(".*?((?<tn>[\\w-]+)(}{2})?)\\.((\\[\\[)?[\\w-]*)?$");
            Matcher matcher = pattern.matcher(stringToComplete);
            if (matcher.matches()) {
                return matcher.group("tn");
            }
        }

        if (activeRecordClass != null) {
            String tableName =  DatabaseUtils.getTableByActiveRecordClass(activeRecordClass);
            return DatabaseUtils.clearTablePrefixTags(ClassUtils.removeQuotes(tableName));
        }
        else
            return null;
    }

    private boolean isTabledPrefix(String prefix) {
        // match "{{%table}}.[[co", "{{%table}}.[[", "{{%table}}.", "{{%table}}.col", "{{table}}.", "table.[[col",
        // "table.[[", "table.col" and "table." at end of string
        Pattern pattern = Pattern.compile("[\\w-]+(}{2})?\\.(\\[{2})?[\\w-]*?$");
        Matcher matcher = pattern.matcher(prefix);
        return matcher.find();
    }

    @NotNull
    private CompletionResultSet adjustPrefix(Character chr, @NotNull CompletionResultSet completionResultSet) {
        String currentColumn = completionResultSet.getPrefixMatcher().getPrefix();
        if (currentColumn.indexOf(chr) != -1) {
            currentColumn = currentColumn.substring(currentColumn.lastIndexOf(chr) + 1).trim();
            completionResultSet = completionResultSet.withPrefixMatcher(currentColumn);
        }
        return completionResultSet;
    }

    private void addAllElementsWithPriority(ArrayList<LookupElementBuilder> lookups, @NotNull CompletionResultSet completionResultSet, double priority) {
        addAllElementsWithPriority(lookups, completionResultSet, priority, false);
    }

    private void addAllElementsWithPriority(ArrayList<LookupElementBuilder> lookups, @NotNull CompletionResultSet completionResultSet, double priority, boolean bold) {
        for (LookupElementBuilder element : lookups) {
            element = element.withBoldness(bold);
            completionResultSet.addElement(PrioritizedLookupElement.withPriority(element, priority));
        }
    }

}
