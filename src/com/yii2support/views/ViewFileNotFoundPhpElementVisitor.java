package com.yii2support.views;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 15.01.2017.
 */
public class ViewFileNotFoundPhpElementVisitor extends PhpElementVisitor {
    private ProblemsHolder myHolder;
    private boolean myIsOnTheFly;

    static private String errorMessageTemplate = "View file for %name% not found.";

    ViewFileNotFoundPhpElementVisitor(@NotNull ProblemsHolder problemsHolder, boolean isOnTheFly) {
        myHolder = problemsHolder;
        myIsOnTheFly = isOnTheFly;
    }

    @Override
    public void visitPhpMethodReference(MethodReference reference) {
        final String name = reference.getName();

        if (name == null || !(name.equals("render") || name.equals("renderAjax") || name.equals("renderPartial"))) {
            return;
        }

        PsiElement[] parameters = reference.getParameters();

        if (parameters.length > 0 && parameters[0] instanceof StringLiteralExpression) {
            PsiFile psiFile = ViewsUtil.getViewPsiFile(parameters[0]);
            if (psiFile == null) {
                PsiElement str = parameters[0].findElementAt(1);
                if (str != null) {
                    myHolder.registerProblem(str, errorMessageTemplate.replace("%name%", parameters[0].getText()), ProblemHighlightType.ERROR);
                }
            }
        }
    }


}
