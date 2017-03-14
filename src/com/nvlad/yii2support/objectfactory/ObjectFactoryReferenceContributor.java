package com.nvlad.yii2support.objectfactory;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceRegistrar;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.nvlad.yii2support.common.Patterns;
import com.nvlad.yii2support.views.ViewsUtil;

import org.jetbrains.annotations.NotNull;

public class ObjectFactoryReferenceContributor extends com.intellij.psi.PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(PlatformPatterns.psiElement(), new ObjectFactoryReferenceProvider());
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
