package com.yii2support.views;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import org.jetbrains.annotations.NotNull;

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
            final PsiFile psiFile = ViewsUtil.getViewPsiFile(parameters[0]);
            if (psiFile == null) {
                final PsiElement str = parameters[0].findElementAt(1);
                if (str != null) {
                    final String errorMessageTemplate = "View file for %name% not found.";
                    final ViewFileNotFoundLocalQuickFix quickFix = new ViewFileNotFoundLocalQuickFix(str.getText());
                    final String descriptionTemplate = errorMessageTemplate.replace("%name%", parameters[0].getText());
                    myHolder.registerProblem(str, descriptionTemplate, ProblemHighlightType.ERROR, quickFix);
                }
            }
        }
    }


}
