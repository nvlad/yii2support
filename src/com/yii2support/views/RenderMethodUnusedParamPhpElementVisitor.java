package com.yii2support.views;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by NVlad on 16.01.2017.
 */
public class RenderMethodUnusedParamPhpElementVisitor extends PhpElementVisitor {
    private ProblemsHolder myHolder;

    RenderMethodUnusedParamPhpElementVisitor(@NotNull ProblemsHolder problemsHolder) {
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
                    final String errorViewNotFoundTemplate = "View file for %name% not found.";
                    final RenderMethodViewNotFoundLocalQuickFix quickFix = new RenderMethodViewNotFoundLocalQuickFix(str.getText());
                    final String descriptionTemplate = errorViewNotFoundTemplate.replace("%name%", parameters[0].getText());
                    myHolder.registerProblem(str, descriptionTemplate, quickFix);
                }
                return;
            }

            final Collection<Variable> viewVariables = PsiTreeUtil.findChildrenOfType(psiFile, Variable.class);
            ArrayList<String> externalVariables = new ArrayList<>();
            if (viewVariables.size() > 0) {
                ArrayList<String> allVariables = new ArrayList<>();
                ArrayList<String> declaredVariables = new ArrayList<>();

                for (Variable variable : viewVariables) {
                    String variableName = variable.getName();
                    if (variable.isDeclaration()) {
                        if (!declaredVariables.contains(variableName)) {
                            declaredVariables.add(variableName);
                        }
                    } else {
                        if (!(variableName.equals("this") || variableName.equals("_file_") || variableName.equals("_params_"))) {
                            if (!allVariables.contains(variableName) && psiFile.getUseScope().equals(variable.getUseScope())) {
                                allVariables.add(variableName);
                            }
                        }
                    }
                }

                for (String variable : allVariables) {
                    if (!declaredVariables.contains(variable)) {
                        externalVariables.add(variable);
                    }
                }
            }

            if (externalVariables.isEmpty()) {
                if (parameters.length > 1 && parameters[1] instanceof ArrayCreationExpression) {
                    String hintNotRequireParams = "This view not require params.";
                    RenderMethodNotRequireParamsLocalQuickFix fix = new RenderMethodNotRequireParamsLocalQuickFix();
                    myHolder.registerProblem(parameters[1], hintNotRequireParams, ProblemHighlightType.LIKE_UNUSED_SYMBOL, fix);
                }
            } else {
                String hintUnusedParams = "View %view% not used params.";
                if (parameters.length > 1 && parameters[1] instanceof ArrayCreationExpression) {
                    for (ArrayHashElement item : ((ArrayCreationExpression) parameters[1]).getHashElements()) {
                        if (item.getKey() instanceof StringLiteralExpression) {
                            String key = ((StringLiteralExpression) item.getKey()).getContents();

                            if (!externalVariables.contains(key)) {
                                RenderMethodUnusedParamLocalQuickFix fix = new RenderMethodUnusedParamLocalQuickFix(key);
                                myHolder.registerProblem(item, hintUnusedParams.replace("%view%", parameters[0].getText()), ProblemHighlightType.LIKE_UNUSED_SYMBOL, fix);
                            }
                        }
                    }
                }
            }
        }
    }
}
