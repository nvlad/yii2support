package com.yii2support.components;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;

/**
 * Created by NVlad on 11.01.2017.
 */
public class ComponentConfigCompletionContributor extends CompletionContributor {
    public ComponentConfigCompletionContributor() {
        extend(CompletionType.BASIC, ElementPattern(), new ComponentConfigCompletionProvider());
    }

    private static ElementPattern<PsiElement> ElementPattern() {
        return PlatformPatterns.psiElement(PsiElement.class)
                .withParent(PlatformPatterns.psiElement(PhpExpression.class)
                        .withParent(PlatformPatterns.psiElement(PhpPsiElement.class)
                                .withParent(PlatformPatterns.psiElement(ArrayCreationExpression.class)
                                        .withParent(PlatformPatterns.psiElement(ParameterList.class)
                                                .withParent(PlatformPatterns.psiElement(NewExpression.class))))));
    }
}
