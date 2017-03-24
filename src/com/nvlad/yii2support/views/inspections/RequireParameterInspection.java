package com.nvlad.yii2support.views.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.util.ArrayUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.nvlad.yii2support.common.PhpUtil;
import com.nvlad.yii2support.views.ViewsUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by NVlad on 23.01.2017.
 */
public class RequireParameterInspection extends PhpInspection {
    @NotNull
    @Override
    public String getShortName() {
        return "RequireParameterInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean isOnTheFly) {
        return new PhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(MethodReference reference) {
                final String name = reference.getName();
                if (name == null || !ArrayUtil.contains(name, ViewsUtil.renderMethods)) {
                    return;
                }

                final PsiElement[] parameters = reference.getParameters();
                if (parameters.length == 0 || !(parameters[0] instanceof StringLiteralExpression)) {
                    return;
                }

                final PsiFile file = ViewsUtil.getViewFile(parameters[0]);
                if (file == null) {
                    return;
                }

                final ArrayList<String> viewParameters = ViewsUtil.getViewVariables(file);
                if (viewParameters.size() > 0) {
                    final Collection<String> existKeys;
                    if (parameters.length > 1) {
                        if (parameters[1] instanceof ArrayCreationExpression) {
                            existKeys = PhpUtil.getArrayKeys((ArrayCreationExpression) parameters[1]);
                        } else if (parameters[1] instanceof FunctionReference) {
                            FunctionReference function = (FunctionReference) parameters[1];
                            if (function.getName() != null && function.getName().equals("compact")) {
                                existKeys = new HashSet<>();
                                for (PsiElement element : function.getParameters()) {
                                    if (element instanceof StringLiteralExpression) {
                                        existKeys.add(((StringLiteralExpression) element).getContents());
                                    }
                                }
                            } else {
                                return;
                            }
                        } else {
                            return;
                        }
                    } else {
                        existKeys = new HashSet<>();
                    }

                    if (existKeys.size() == 0 && isOnTheFly) {
                        String errorRequireParameters = "View %view% require parameters.";
                        RequireParameterLocalQuickFix fix = new RequireParameterLocalQuickFix(viewParameters);
                        problemsHolder.registerProblem(reference, errorRequireParameters.replace("%view%", parameters[0].getText()), fix);
                        return;
                    }

                    viewParameters.removeIf(existKeys::contains);
                    if (viewParameters.size() > 0) {
                        String errorRequireParameter = "View %view% require %parameter% parameter";
                        for (String parameter : viewParameters) {
                            RequireParameterLocalQuickFix fix = new RequireParameterLocalQuickFix(viewParameters);
                            String description = errorRequireParameter
                                    .replace("%view%", parameters[0].getText())
                                    .replace("%parameter%", parameter);
                            problemsHolder.registerProblem(reference, description, fix);
                        }
                    }
                }
            }
        };
    }
}
