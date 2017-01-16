package com.yii2support.views;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.yii2support.common.PsiUtil;
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
        ArrayCreationExpression params = (ArrayCreationExpression) item.getParent();

        PsiUtil.deleteArrayElement(item);

        if (!params.getHashElements().iterator().hasNext()) {
            if (params.getPrevSibling() instanceof PsiWhiteSpace) {
                params.getPrevSibling().delete();
            }
            params.getPrevSibling().delete();
            params.delete();
        }
    }
}
