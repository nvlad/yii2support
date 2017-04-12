package com.nvlad.yii2support.views.inspections;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.util.ArrayUtil;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.nvlad.yii2support.views.ViewsUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by NVlad on 15.01.2017.
 */
final public class UnusedParameterInspection extends PhpInspection {
    @NotNull
    @Override
    public String getShortName() {
        return "UnusedParameterInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean isOnTheFly) {
        return new PhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(MethodReference reference) {
                if (!ViewsUtil.isValidRenderMethod(reference)) {
                    return;
                }

                final String name = reference.getName();
                if (name == null || !ArrayUtil.contains(name, ViewsUtil.renderMethods)) {
                    return;
                }

                final PsiElement[] parameters = reference.getParameters();
                if (parameters.length < 2 || !(parameters[0] instanceof StringLiteralExpression)) {
                    return;
                }

                final PsiFile file = ViewsUtil.getViewFile(parameters[0]);
                if (file == null) {
                    return;
                }

                if (!(file instanceof PhpFile)) {
                    return;
                }

                final ArrayList<String> viewParameters = ViewsUtil.getViewVariables(file);
                if (viewParameters.size() > 0) {
                    final HashSet<String> unusedParameters = new HashSet<>();
                    final String errorUnusedParameter = "View %view% not use \"%parameter%\" parameter";
                    if (parameters[1] instanceof ArrayCreationExpression) {
                        for (ArrayHashElement element : ((ArrayCreationExpression) parameters[1]).getHashElements()) {
                            if (element.getKey() != null && element.getKey() instanceof StringLiteralExpression) {
                                final String key = ((StringLiteralExpression) element.getKey()).getContents();
                                if (!viewParameters.contains(key)) {
                                    UnusedParameterLocalQuickFix fix = new UnusedParameterLocalQuickFix(key);
                                    String description = errorUnusedParameter
                                            .replace("%view%", parameters[0].getText())
                                            .replace("%parameter%", key);
                                    problemsHolder.registerProblem(element, description, ProblemHighlightType.LIKE_UNUSED_SYMBOL, fix);
                                    unusedParameters.add(key);
                                }
                            }
                        }
                    }
                    if (parameters[1] instanceof FunctionReference) {
                        FunctionReference function = ((FunctionReference) parameters[1]);
                        if (function.getName() != null && function.getName().contains("compact")) {
                            for (PsiElement element : function.getParameters()) {
                                if (element instanceof StringLiteralExpression) {
                                    String key = ((StringLiteralExpression) element).getContents();
                                    if (!viewParameters.contains(key)) {
                                        UnusedParameterLocalQuickFix fix = new UnusedParameterLocalQuickFix(key);
                                        String description = errorUnusedParameter
                                                .replace("%view%", parameters[0].getText())
                                                .replace("%parameter%", key);
                                        problemsHolder.registerProblem(element, description, ProblemHighlightType.LIKE_UNUSED_SYMBOL, fix);
                                        unusedParameters.add(key);
                                    }
                                }
                            }
                        }
                    }

                    if (unusedParameters.size() > 0 && isOnTheFly) {
                        if (viewParameters.containsAll(unusedParameters)) {
                            String errorUnusedParameters = "This View does not use parameters";
                            UnusedParametersLocalQuickFix fix = new UnusedParametersLocalQuickFix();
                            problemsHolder.registerProblem(parameters[1], errorUnusedParameters, ProblemHighlightType.LIKE_UNUSED_SYMBOL, fix);
                            problemsHolder.registerProblem(reference, errorUnusedParameters, ProblemHighlightType.INFORMATION, fix);
                        } else {
                            String errorUnusedParameters = "This View have unused parameters";
                            UnusedParametersLocalQuickFix fix = new UnusedParametersLocalQuickFix(unusedParameters);
                            problemsHolder.registerProblem(reference, errorUnusedParameters, ProblemHighlightType.INFORMATION, fix);
                        }

                    }
                } else {
                    if (parameters.length > 1) {
                        if (parameters[1] instanceof ArrayCreationExpression || parameters[1] instanceof FunctionReference) {
                            String errorUnusedParameters = "This View does not use parameters";
                            UnusedParametersLocalQuickFix fix = new UnusedParametersLocalQuickFix();
                            problemsHolder.registerProblem(parameters[1], errorUnusedParameters, ProblemHighlightType.LIKE_UNUSED_SYMBOL, fix);
                            if (isOnTheFly) {
                                problemsHolder.registerProblem(reference, errorUnusedParameters, ProblemHighlightType.INFORMATION, fix);
                            }
                        }
                    }
                }
            }
        };
    }
}
