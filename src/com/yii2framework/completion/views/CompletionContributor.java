package com.yii2framework.completion.views;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;

/**
 * Created by NVlad on 27.12.2016.
 */
public class CompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {
    public CompletionContributor() {
        extend(CompletionType.BASIC, ElementPattern(), new CompletionProvider());
    }

    private static ElementPattern<PsiElement> ElementPattern() {
        return PlatformPatterns.psiElement(PsiElement.class)
                .withParent(PlatformPatterns.psiElement(StringLiteralExpression.class)
                        .withParent(PlatformPatterns.psiElement(ParameterList.class)
                                .withParent(PlatformPatterns.psiElement(MethodReference.class))));
    }
}
