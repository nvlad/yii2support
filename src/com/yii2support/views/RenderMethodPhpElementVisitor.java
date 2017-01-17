package com.yii2support.views;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by NVlad on 15.01.2017.
 */
public class RenderMethodPhpElementVisitor extends PhpElementVisitor {
    private ProblemsHolder myHolder;

    RenderMethodPhpElementVisitor(@NotNull ProblemsHolder problemsHolder) {
        myHolder = problemsHolder;
    }

    @Override
    public void visitPhpMethodReference(MethodReference reference) {
        final String name = reference.getName();

        if (name == null || !(name.equals("render") || name.equals("renderAjax") || name.equals("renderPartial"))) {
            return;
        }

        PsiElement[] parameters = reference.getParameters();

        if (parameters.length > 0 && parameters[0] instanceof StringLiteralExpression) {
            String renderView = ((StringLiteralExpression) parameters[0]).getContents();
            if (!renderView.equals(reference.getUserData(ViewsUtil.RENDER_VIEW))) {
                reference.putUserData(ViewsUtil.RENDER_VIEW, renderView);
                reference.putUserData(ViewsUtil.RENDER_VIEW_FILE, null);
                reference.putUserData(ViewsUtil.VIEW_VARIABLES, null);
                reference.putUserData(ViewsUtil.VIEW_FILE_MODIFIED, null);
            }

            PsiFile psiFile = reference.getUserData(ViewsUtil.RENDER_VIEW_FILE);
            if (psiFile == null || !psiFile.isValid()) {
                psiFile = ViewsUtil.getViewPsiFile(parameters[0]);
                reference.putUserData(ViewsUtil.RENDER_VIEW_FILE, psiFile);
            }
            if (psiFile == null) {
                final PsiElement str = parameters[0].findElementAt(1);
                if (str != null) {
                    final String errorViewNotFoundTemplate = "View file for %name% not found.";
                    final RenderMethodViewNotFoundLocalQuickFix quickFix = new RenderMethodViewNotFoundLocalQuickFix(str.getText());
                    final String descriptionTemplate = errorViewNotFoundTemplate.replace("%name%", parameters[0].getText());
                    myHolder.registerProblem(str, descriptionTemplate, quickFix);
                }
                return;
            }

            ArrayList<String> externalVariables = ViewsUtil.getViewVariables(psiFile);
            if (!externalVariables.isEmpty()) {
                String errorRequiredParams = "View %view% required params.";
                if (parameters.length == 1) {
                    RenderMethodRequiredParamsLocalQuickFix fix = new RenderMethodRequiredParamsLocalQuickFix(externalVariables);
                    myHolder.registerProblem(reference, errorRequiredParams.replace("%view%", parameters[0].getText()), fix);
                } else if (parameters[1] instanceof ArrayCreationExpression) {
                    for (ArrayHashElement item : ((ArrayCreationExpression) parameters[1]).getHashElements()) {
                        if (item.getKey() instanceof StringLiteralExpression) {
                            String key = ((StringLiteralExpression) item.getKey()).getContents();

                            if (externalVariables.contains(key)) {
                                externalVariables.remove(key);
                            }
                        }
                    }

                    if (!externalVariables.isEmpty()) {
                        RenderMethodRequiredParamsLocalQuickFix fix = new RenderMethodRequiredParamsLocalQuickFix(externalVariables);
                        myHolder.registerProblem(reference, errorRequiredParams.replace("%view%", parameters[0].getText()), fix);
                    }
                }
            }
        }
    }
}
