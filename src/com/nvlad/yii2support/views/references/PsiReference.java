package com.nvlad.yii2support.views.references;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by NVlad on 02.01.2017.
 */
public class PsiReference extends PsiReferenceBase<PsiElement> {
    private final PsiFile myFile;

    PsiReference(@NotNull PsiElement element, PsiFile file) {
        super(element);
        myFile = file;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return myFile;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
