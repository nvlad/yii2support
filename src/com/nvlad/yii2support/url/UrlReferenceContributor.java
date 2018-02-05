package com.nvlad.yii2support.url;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceRegistrar;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.nvlad.yii2support.common.Patterns;
import com.nvlad.yii2support.views.ViewsUtil;

import org.jetbrains.annotations.NotNull;

public class UrlReferenceContributor extends com.intellij.psi.PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(ElementPattern(), new UrlReferenceProvider());
    }

    private static ElementPattern<PsiElement> ElementPattern() {
        return
                PlatformPatterns.or(
                        PlatformPatterns.psiElement().withSuperParent(4, MethodReference.class),
                        PlatformPatterns.psiElement().withSuperParent(5, MethodReference.class),
                        PlatformPatterns.psiElement().withSuperParent(3, MethodReference.class)
                );
    }
}
