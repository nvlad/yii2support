package com.yii2framework.completion.views;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 27.12.2016.
 */
public class CompletionProvider extends com.intellij.codeInsight.completion.CompletionProvider {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        PhpPsiElement psiElement = (PhpPsiElement) completionParameters.getPosition().getParent().getParent().getParent();
        String psiElementName = psiElement.getName();

        if (psiElementName.startsWith("render")) {
            fillCompletionResultSet(completionResultSet);
        }
    }

    private void fillCompletionResultSet(@NotNull CompletionResultSet completionResultSet) {
        completionResultSet.addElement(LookupElementBuilder.create("renderView - NVlad"));
    }
}
