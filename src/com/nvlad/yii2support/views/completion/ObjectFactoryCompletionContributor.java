package com.nvlad.yii2support.views.completion;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.nvlad.yii2support.common.Patterns;
import com.nvlad.yii2support.views.ViewsUtil;

/**
 * Created by oleg on 16.02.2017.
 */
public class ObjectFactoryCompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {
    public ObjectFactoryCompletionContributor() {
        extend(CompletionType.BASIC, ElementPattern(), new ObjectFactoryCompletionProvider());
    }

    private static ElementPattern<PsiElement> ElementPattern() {
        return PlatformPatterns.psiElement()
                .withSuperParent(3, Patterns.arrayCreation());
    }
}
