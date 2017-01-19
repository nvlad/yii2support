package com.yii2support.views;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by NVlad on 16.01.2017.
 */
class RenderMethodUnusedParamPhpElementVisitor extends PhpElementVisitor {
    final private ProblemsHolder myHolder;

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
                return;
            }

            ArrayList<String> externalVariables = ViewsUtil.getViewVariables(psiFile);
            ArrayList<String> renderParams = new ArrayList<>();
            ArrayList<String> unusedParams = new ArrayList<>();
            String hintUnusedParams = "View %view% not use \"%key%\" parameter";
            if (parameters.length > 1) {
                if (parameters[1] instanceof ArrayCreationExpression) {
                    for (ArrayHashElement item : ((ArrayCreationExpression) parameters[1]).getHashElements()) {
                        if (item.getKey() instanceof StringLiteralExpression) {
                            String key = ((StringLiteralExpression) item.getKey()).getContents();

                            if (!externalVariables.contains(key)) {
                                RenderMethodUnusedParamLocalQuickFix fix = new RenderMethodUnusedParamLocalQuickFix(key);
                                String description = hintUnusedParams.replace("%view%", parameters[0].getText()).replace("%key%", key);
                                myHolder.registerProblem(item, description, ProblemHighlightType.LIKE_UNUSED_SYMBOL, fix);
                                unusedParams.add(key);
                            }

                            renderParams.add(key);
                        }
                    }
                }
                if (parameters[1].getNode().getElementType() == PhpElementTypes.FUNCTION_CALL) {
                    FunctionReference functionReference = (FunctionReference) parameters[1];
                    if (functionReference.getName() != null && functionReference.getName().equals("compact")) {
                        for (PsiElement element : functionReference.getParameters()) {
                            if (element instanceof StringLiteralExpression && !externalVariables.contains(((StringLiteralExpression) element).getContents())) {
                                String key = ((StringLiteralExpression) element).getContents();
                                RenderMethodUnusedParamLocalQuickFix fix = new RenderMethodUnusedParamLocalQuickFix(key);
                                String description = hintUnusedParams.replace("%view%", parameters[0].getText()).replace("%key%", key);
                                myHolder.registerProblem(element, description, ProblemHighlightType.LIKE_UNUSED_SYMBOL, fix);
                                unusedParams.add(key);
                            }
                        }
                    }
                }
            }

            if (unusedParams.size() > 0) {
                if (parameters.length > 1) {
                    String hintNotRequireParams = "This View does not use parameters";
                    if (parameters[1] instanceof ArrayCreationExpression) {
                        if (unusedParams.size() != renderParams.size()) {
                            RenderMethodNotRequireParamsLocalQuickFix fix = new RenderMethodNotRequireParamsLocalQuickFix(unusedParams);
                            myHolder.registerProblem(parameters[1], hintNotRequireParams, ProblemHighlightType.INFORMATION, fix);
                        } else {
                            RenderMethodNotRequireParamsLocalQuickFix fix = new RenderMethodNotRequireParamsLocalQuickFix();
                            myHolder.registerProblem(parameters[1], hintNotRequireParams, ProblemHighlightType.LIKE_UNUSED_SYMBOL, fix);
                        }
                    }
                }
            }
        }
    }
}
