package com.yii2support.views;

import com.intellij.codeInsight.template.macro.SplitWordsMacro;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by NVlad on 02.01.2017.
 */
public class PsiReference extends PsiReferenceBase<PsiElement> {
    private PsiElement myTarget;

    PsiReference(@NotNull PsiElement element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        if (myTarget == null) {
            PsiFile psiFile = ViewsUtil.getViewPsiFile(myElement);
            if (psiFile != null) {
                myTarget = psiFile.getOriginalElement();
            }
        }

        return myTarget;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
