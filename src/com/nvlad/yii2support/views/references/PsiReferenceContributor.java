package com.nvlad.yii2support.views.references;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceRegistrar;
import com.nvlad.yii2support.common.Patterns;
import com.nvlad.yii2support.views.ViewsUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 02.01.2017.
 */
public class PsiReferenceContributor extends com.intellij.psi.PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(ElementPattern(), new PsiReferenceProvider());
    }

    private static ElementPattern<PsiElement> ElementPattern() {
        return PlatformPatterns.psiElement(PsiElement.class)
                .withSuperParent(2, Patterns.methodWithName(ViewsUtil.renderMethods));
    }
}
