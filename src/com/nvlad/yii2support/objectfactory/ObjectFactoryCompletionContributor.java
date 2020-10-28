package com.nvlad.yii2support.objectfactory;

import com.intellij.codeInsight.completion.AutoCompletionContext;
import com.intellij.codeInsight.completion.AutoCompletionDecision;
import com.intellij.codeInsight.completion.CompletionInitializationContext;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ArrayAccessExpression;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.nvlad.yii2support.common.Patterns;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObjectFactoryCompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {
    public ObjectFactoryCompletionContributor() {
        extend(CompletionType.BASIC, ElementPattern(), new ObjectFactoryCompletionProvider());
    }

    @Nullable
    @Override
    public AutoCompletionDecision handleAutoCompletionPossibility(@NotNull AutoCompletionContext context) {
        return super.handleAutoCompletionPossibility(context);
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        if ((typeChar == '\'' || typeChar == '"') &&
                (position.getParent() instanceof ArrayCreationExpression || position.getParent() instanceof ArrayAccessExpression)) {
            return true;
        }

        return false;
    }

    private static ElementPattern<PsiElement> ElementPattern() {
        return PlatformPatterns.psiElement()
            .withParent(PlatformPatterns.psiElement(StringLiteralExpression.class)
                .withParent(PlatformPatterns.or(
                    PlatformPatterns.psiElement().withParent(ArrayCreationExpression.class),
                    PlatformPatterns.psiElement().withParent(ArrayAccessExpression.class),
                    PlatformPatterns.psiElement(PhpPsiElement.class),
                    Patterns.withHashKey().withParent(PlatformPatterns.psiElement().withParent(ArrayCreationExpression.class))
                    )
                )
            );
    }
}
