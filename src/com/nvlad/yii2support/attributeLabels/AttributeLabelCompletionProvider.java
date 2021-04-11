package com.nvlad.yii2support.attributeLabels;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.ClassUtils;
import org.jetbrains.annotations.NotNull;

public class AttributeLabelCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        PsiElement position = completionParameters.getPosition();
        if (position.getParent() instanceof PhpExpression) {
            PhpExpression phpExpression = (PhpExpression) position.getParent();
            PhpClass phpClass = ClassUtils.getClassIfInMethod(position, "attributeLabels");
            if (phpClass != null) {
                for (Field field : ClassUtils.getClassFields(phpClass)) {
                    LookupElementBuilder lookupBuilder = buildLookup(field, phpExpression);
                    completionResultSet.addElement(lookupBuilder);
                }
            }
        }
    }

    @NotNull
    private LookupElementBuilder buildLookup(PhpClassMember field, PhpExpression position) {
        String lookupString = field instanceof Method ? ClassUtils.getAsPropertyName((Method) field) : field.getName();
        LookupElementBuilder builder = LookupElementBuilder.create(field, lookupString).withIcon(field.getIcon());

        builder = builder.withInsertHandler((insertionContext, lookupElement) -> {
            Document document = insertionContext.getDocument();
            int insertPosition = insertionContext.getSelectionEndOffset();
            if (position.getParent().getParent() instanceof ArrayCreationExpression) {
                document.insertString(insertPosition + 1, " => '',");
                insertPosition += 6;
                insertionContext.getEditor().getCaretModel().getCurrentCaret().moveToOffset(insertPosition);
            }
        });

        if (field instanceof Field) {
            builder = builder.withTypeText(field.getType().toString());
        }

        return builder;
    }
}
