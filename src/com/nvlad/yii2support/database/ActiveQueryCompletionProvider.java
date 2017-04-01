package com.nvlad.yii2support.database;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
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
public class ActiveQueryCompletionProvider extends com.intellij.codeInsight.completion.CompletionProvider<CompletionParameters> {


    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {

        MethodReference methodRef = ClassUtils.getMethodRef(completionParameters.getPosition(), 10);
        if (methodRef != null) {
            String prefix = completionResultSet.getPrefixMatcher().getPrefix();
            // comma fix
            completionResultSet = adjustPrefix(',', completionResultSet);
            completionResultSet = adjustPrefix('.', completionResultSet);
            completionResultSet = adjustPrefix('{', completionResultSet);
            completionResultSet = adjustPrefix('[', completionResultSet);
            completionResultSet = adjustPrefix('%', completionResultSet);
            completionResultSet = adjustPrefix('(', completionResultSet);

            Method method = (Method) methodRef.resolve();

            int paramPosition = ClassUtils.paramIndexForElement(completionParameters.getPosition());
            if (method != null && paramPosition >= 0 &&
                    method.getParameters().length > paramPosition &&
                    method.getParameters().length > 0 &&
                    (method.getParameters()[paramPosition].getName().equals("condition") ||
                            method.getParameters()[paramPosition].getName().startsWith("column"))
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
                        ClassUtils.getClass(index, "\\yii\\db\\ActiveRecord"))) {


                    String tableName = getTable(prefix, activeRecordClass);
                    if ( tableName == null || tableName.isEmpty())
                        return;


                    if (tableName != null) {
                        tableName = DatabaseUtils.clearTablePrefixTags(ClassUtils.removeQuotes(tableName));
                        ArrayList<LookupElementBuilder> lookups = DatabaseUtils.getLookupItemsByTable(tableName, completionParameters.getPosition().getProject(), (PhpExpression) completionParameters.getPosition().getParent());
                        if (lookups != null && !lookups.isEmpty()) {
                            addAllElementsWithPriority(lookups, completionResultSet, 2); // columns
                        } else {
                            ArrayList<LookupElementBuilder> items = DatabaseUtils.getLookupItemsByAnnotations(activeRecordClass, (PhpExpression) completionParameters.getPosition().getParent());
                            addAllElementsWithPriority(lookups, completionResultSet, 2); // fields
                        }
                        if (!isTabledPrefix(prefix)) {
                            lookups = DatabaseUtils.getLookupItemsTables(completionParameters.getPosition().getProject(), (PhpExpression) completionParameters.getPosition().getParent());
                            addAllElementsWithPriority(lookups, completionResultSet, 1); // tables
                        }
                    }
                }
            } else if (method.getParameters().length > paramPosition &&
                    method != null && method.getParameters().length > 0 &&
                    (method.getParameters()[paramPosition].getName().startsWith("table"))) {

                // cancel codecompletion in case of "table" have ,
                if (method.getParameters()[paramPosition].getName().equals("table") && methodRef.getParameters().length > paramPosition) {
                    PsiElement element = methodRef.getParameters()[paramPosition];
                    String content = element.getText();
                    if (content.indexOf(',') >= 0)
                        return;
                }

                ArrayList<LookupElementBuilder> lookups = DatabaseUtils.getLookupItemsTables(completionParameters.getPosition().getProject(), (PhpExpression) completionParameters.getPosition().getParent());
                addAllElementsWithPriority(lookups, completionResultSet, 1); //tables
            }
        }

    }

    @Nullable
    private String getTable(String stringToComplete, PhpClass activeRecordClass) {
        if (stringToComplete.length() > 2 && stringToComplete.contains(".")) {
            Pattern pattern = Pattern.compile("((\\{{2}%?)(?=[\\w-]+}{2})(?<te>[\\w-]+)}{2}|(?<tu>[\\w-]+))\\.((\\[\\[)?[\\w-]*)?$");
            Matcher matcher = pattern.matcher(stringToComplete);
            if (matcher.matches()) {

                return matcher.group(1).startsWith("{{") ? matcher.group("te") : matcher.group("tu");
            }
        }
        if (activeRecordClass != null)
            return DatabaseUtils.getTableByActiveRecordClass(activeRecordClass);
        else
            return null;
    }

    private boolean isTabledPrefix(String prefix) {
        Pattern pattern = Pattern.compile("((\\{{2}%?)(?=[\\w-]+}{2})[\\w-]+}{2}|[\\w-]+)\\.((\\[\\[)?[\\w-]*)?$");
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

    private void addAllElementsWithPriority(ArrayList<LookupElementBuilder> lookups, @NotNull CompletionResultSet completionResultSet, int priority) {
        addAllElementsWithPriority(lookups, completionResultSet, priority, false);
    }

    private void addAllElementsWithPriority(ArrayList<LookupElementBuilder> lookups, @NotNull CompletionResultSet completionResultSet, int priority, boolean bold) {
        for (LookupElementBuilder element : lookups) {
            element = element.withBoldness(bold);
            completionResultSet.addElement(PrioritizedLookupElement.withPriority(element, priority));
        }
    }

}
