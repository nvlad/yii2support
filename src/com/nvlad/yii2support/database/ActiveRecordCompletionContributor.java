package com.nvlad.yii2support.database;

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
import com.nvlad.yii2support.database.ActiveRecordCompletionProvider;
import org.jetbrains.annotations.NotNull;

public class ActiveRecordCompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {
    public ActiveRecordCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new ActiveRecordCompletionProvider());
    }

    private static ElementPattern<PsiElement> ElementPattern() {

        return PlatformPatterns.psiElement()
                .withParent(PlatformPatterns.psiElement(StringLiteralExpression.class)
                        .withParent(PlatformPatterns.or(
                                PlatformPatterns.psiElement().withParent(ArrayCreationExpression.class),
                                Patterns.withHashKey()
                                        .withParent(PlatformPatterns.psiElement().withParent(ArrayCreationExpression.class))
                        )));


        // return PlatformPatterns.or(PlatformPatterns.psiElement().withSuperParent(3, ArrayCreationExpression.class),
        //         PlatformPatterns.psiElement().withSuperParent(4, ArrayCreationExpression.class));
    }
}
