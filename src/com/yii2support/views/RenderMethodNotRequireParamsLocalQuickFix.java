package com.yii2support.views;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 16.01.2017.
 */
public class RenderMethodNotRequireParamsLocalQuickFix implements LocalQuickFix {
    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Remove view params.";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement item = descriptor.getPsiElement();
        if (item.getPrevSibling() instanceof PsiWhiteSpace) {
            item.getPrevSibling().delete();
        }
        item.getPrevSibling().delete();
        item.delete();
    }
}
