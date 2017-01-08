package com.yii2support.i18n;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 06.01.2017.
 */
public class CompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {
    public CompletionContributor() {
        extend(CompletionType.BASIC, ElementPattern(), new CompletionProvider());
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        return true;
    }

    private static ElementPattern<PsiElement> ElementPattern() {
        return PlatformPatterns.psiElement(PsiElement.class)
                .withParent(PlatformPatterns.psiElement(StringLiteralExpression.class)
                        .withParent(PlatformPatterns.psiElement(ParameterList.class)
                                .withParent(PlatformPatterns.psiElement(MethodReference.class))));
    }
}
