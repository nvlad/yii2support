package com.nvlad.yii2support.views.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.nvlad.yii2support.common.PsiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 16.01.2017.
 */
class UnusedParameterLocalQuickFix implements LocalQuickFix {
    final private String myParam;

    UnusedParameterLocalQuickFix(String param) {
        myParam = param;
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return "Remove unused parameter \"%param%\"".replace("%param%", myParam);
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Remove unused parameter";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement item = descriptor.getPsiElement();

        PsiElement context = item.getContext();
        if (context instanceof ArrayCreationExpression) {
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
        if (context instanceof ParameterList && context.getParent() instanceof FunctionReference) {
            FunctionReference functionReference = (FunctionReference) context.getParent();
            if (functionReference.getName() != null && functionReference.getName().equals("compact")) {
                PsiUtil.deleteFunctionParam(item);

                if (functionReference.getParameters().length == 0) {
                    PsiUtil.deleteFunctionParam(functionReference);
                }
            }
        }
    }
}
