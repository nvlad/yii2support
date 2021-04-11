package com.nvlad.yii2support.widgetsconfig;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.Patterns;

public class WidgetConfigCompletionContributor extends CompletionContributor {
    public WidgetConfigCompletionContributor() {
        extend(CompletionType.BASIC, ElementPattern(), new WidgetConfigCompletionProvider());
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
