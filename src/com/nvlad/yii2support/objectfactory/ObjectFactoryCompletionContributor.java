package com.nvlad.yii2support.objectfactory;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.NewExpression;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.elements.impl.ArrayCreationExpressionImpl;
import com.nvlad.yii2support.common.Patterns;
import org.jetbrains.annotations.NotNull;

public class ObjectFactoryCompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {
    public ObjectFactoryCompletionContributor() {
        extend(CompletionType.BASIC, ElementPattern(), new ObjectFactoryCompletionProvider());
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        if ((typeChar == '\'' || typeChar == '"') && position.getParent() instanceof ArrayCreationExpression) {
            return true;
        }

        return false;
    }

    private static ElementPattern<PsiElement> ElementPattern() {

        return PlatformPatterns.psiElement()
                .withParent(PlatformPatterns.psiElement(StringLiteralExpression.class)
                        .withParent(PlatformPatterns.or(
                                PlatformPatterns.psiElement().withParent(ArrayCreationExpression.class),
                                Patterns.withHashKey()
                                        .withParent(PlatformPatterns.psiElement().withParent(ArrayCreationExpression.class))
                        )));

    }
}
