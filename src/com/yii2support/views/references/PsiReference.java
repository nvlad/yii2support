package com.yii2support.views.references;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.yii2support.views.ViewsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by NVlad on 02.01.2017.
 */
public class PsiReference extends PsiReferenceBase<PsiElement> {
    PsiReference(@NotNull PsiElement element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return ViewsUtil.getViewFile(myElement);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
