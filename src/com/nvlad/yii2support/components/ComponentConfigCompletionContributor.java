package com.nvlad.yii2support.components;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.NewExpression;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.nvlad.yii2support.common.Patterns;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 11.01.2017.
 */
public class ComponentConfigCompletionContributor extends CompletionContributor {
    public ComponentConfigCompletionContributor() {
        extend(CompletionType.BASIC, ElementPattern(), new ComponentConfigCompletionProvider());
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        if ((typeChar == '\'' || typeChar == '"') && position.getParent() instanceof ArrayCreationExpression) {
            return true;
        }

        return false;
    }

    private static ElementPattern<PsiElement> ElementPattern() {
        //noinspection unchecked
        return PlatformPatterns.psiElement()
                .withParent(PlatformPatterns.psiElement(StringLiteralExpression.class)
                        .withParent(PlatformPatterns.or(
                                PlatformPatterns.psiElement().withSuperParent(3, NewExpression.class),
                                Patterns.withHashKey()
                                        .withParent(PlatformPatterns.psiElement().withSuperParent(3, NewExpression.class))
                        )));
    }
}
