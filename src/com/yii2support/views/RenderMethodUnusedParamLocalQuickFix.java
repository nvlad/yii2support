package com.yii2support.views;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 16.01.2017.
 */
public class RenderMethodUnusedParamLocalQuickFix implements LocalQuickFix {
    private String myParam;

    RenderMethodUnusedParamLocalQuickFix(String param) {
        myParam = param;
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return "Remove unused \"%param%\" param".replace("%param%", myParam);
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Unused view param";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement item = descriptor.getPsiElement();
        PsiElement next = item.getNextSibling();
        String endArray = ((ArrayCreationExpression) item.getParent()).isShortSyntax() ? "]" : ")";

        if (next instanceof PsiWhiteSpace && next.getNextSibling().getText() != null) {
            if (next.getNextSibling().getText().equals(endArray)) {
                next = next.getNextSibling();
            }
        }
        if (next.getText().equals(endArray)) {
            if (item.getPrevSibling() instanceof PsiWhiteSpace) {
                item.getPrevSibling().delete();
            }
            if (item.getPrevSibling().getText().equals(",")) {
                item.getPrevSibling().delete();
            }
        }
        if (next.getText().equals(",")) {
            if (next.getNextSibling() instanceof PsiWhiteSpace) {
                next.getNextSibling().delete();
            }
            next.delete();
        }
        item.delete();
    }
}
