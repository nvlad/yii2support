package com.nvlad.yii2support.views.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.PsiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by NVlad on 16.01.2017.
 */
class UnusedParametersLocalQuickFix implements LocalQuickFix {
    private Collection<String> myUnusedParams;

    UnusedParametersLocalQuickFix() {
    }

    UnusedParametersLocalQuickFix(Collection<String> unusedParams) {
        myUnusedParams = unusedParams;
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return getFamilyName();
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Remove all unused parameters";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement viewParameters = descriptor.getPsiElement();
        if (viewParameters instanceof MethodReference) {
            viewParameters = ((MethodReference) viewParameters).getParameters()[1];
        }

        if (myUnusedParams != null) {
            final Set<PsiElement> unused = new HashSet<>();
            if (viewParameters instanceof ArrayCreationExpression) {
                final ArrayCreationExpression params = (ArrayCreationExpression) viewParameters;
                for (ArrayHashElement element : params.getHashElements()) {
                    if (element.getKey() != null) {
                        final String key = ((StringLiteralExpression) element.getKey()).getContents();
                        if (myUnusedParams.contains(key)) {
                            unused.add(element);
                        }
                    }
                }
                for (PsiElement element : unused) {
                    PsiUtil.deleteArrayElement(element);
                }

                if (!params.getHashElements().iterator().hasNext()) {
                    if (params.getPrevSibling() instanceof PsiWhiteSpace) {
                        params.getPrevSibling().delete();
                    }
                    params.getPrevSibling().delete();
                    params.delete();
                }
            }
            if (viewParameters instanceof FunctionReference) {
                for (PsiElement element : ((FunctionReference) viewParameters).getParameters()) {
                    if (element instanceof StringLiteralExpression) {
                        if (myUnusedParams.contains(((StringLiteralExpression) element).getContents())) {
                            unused.add(element);
                        }
                    }
                }
                for (PsiElement element : unused) {
                    PsiUtil.deleteFunctionParam(element);
                }
            }
        } else {
            if (viewParameters.getPrevSibling() instanceof PsiWhiteSpace) {
                viewParameters.getPrevSibling().delete();
            }
            viewParameters.getPrevSibling().delete();
            viewParameters.delete();
        }
    }
}
