package com.nvlad.yii2support.objectfactory;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDirectory;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;

import java.util.Hashtable;

/**
 * Created by oleg on 16.02.2017.
 */
public class ObjectFactoryCompletionProvider extends com.intellij.codeInsight.completion.CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {

        if (!(completionParameters.getPosition().getParent().getParent().getParent() instanceof ArrayCreationExpression) &&
                !(completionParameters.getPosition().getParent().getParent().getParent().getParent() instanceof ArrayCreationExpression)) {
            return;
        }
        ArrayCreationExpression arrayCreation = null;

        if (completionParameters.getPosition().getParent().getParent().getParent() instanceof ArrayCreationExpression) {
            arrayCreation = (ArrayCreationExpression) completionParameters.getPosition().getParent().getParent().getParent();
        } else if (completionParameters.getPosition().getParent().getParent().getParent().getParent() instanceof ArrayCreationExpression &&
                completionParameters.getPosition().getParent().getParent().getParent() instanceof ArrayHashElement &&
                completionParameters.getPosition().getParent().getParent().toString() != "Array value") {
            arrayCreation = (ArrayCreationExpression) completionParameters.getPosition().getParent().getParent().getParent().getParent();
        } else {
            return;
        }

        PhpClass phpClass = ObjectFactoryUtils.findClassByArray(arrayCreation);
        if (phpClass == null) {
            phpClass = ObjectFactoryUtils.getPhpClassByYiiCreateObject(arrayCreation);
        }
        if (phpClass == null) {
            PhpFile file = (PhpFile) completionParameters.getOriginalFile();
            PsiDirectory dir = file.getContainingDirectory();
            phpClass = ObjectFactoryUtils.getPhpClassInConfig(dir, arrayCreation);
        }

        if (phpClass == null) {
            phpClass = ObjectFactoryUtils.getPhpClassInWidget(arrayCreation);
        }
        if (phpClass == null) {
            phpClass = ObjectFactoryUtils.getPhpClassInGridColumns(arrayCreation);
        }

        Hashtable<String, Object> uniqTracker = new Hashtable<>();

        PhpExpression position = (PhpExpression) completionParameters.getPosition().getParent();
        if (phpClass != null) {
            for (Field field : ClassUtils.getClassFields(phpClass)) {
                uniqTracker.put(field.getName(), field);
                LookupElementBuilder lookupBuilder = buildLookup(field, position);
                completionResultSet.addElement(lookupBuilder);
            }

            for (Method method : ClassUtils.getClassSetMethods(phpClass)) {
                if (uniqTracker.get(ClassUtils.getAsPropertyName(method)) == null) {
                    LookupElementBuilder lookupBuilder = buildLookup(method, position);
                    completionResultSet.addElement(lookupBuilder);
                }
            }
        }
    }

    @NotNull
    private LookupElementBuilder buildLookup(PhpClassMember field, PhpExpression position) {
        String lookupString = field instanceof Method ? ClassUtils.getAsPropertyName((Method) field) : field.getName();
        LookupElementBuilder builder =  LookupElementBuilder.create(field, lookupString).withIcon(field.getIcon())
                .withInsertHandler((insertionContext, lookupElement) -> {

                    Document document = insertionContext.getDocument();
                    int insertPosition = insertionContext.getSelectionEndOffset();

                    if (position.getParent().getParent() instanceof ArrayCreationExpression) {
                        document.insertString(insertPosition + 1, " => ");
                        insertPosition += 5;
                        insertionContext.getEditor().getCaretModel().getCurrentCaret().moveToOffset(insertPosition);
                    }
                });
        if (field instanceof Field) {
            builder = builder.withTypeText(field.getType().toString());
        }
        return builder;
    }

}
