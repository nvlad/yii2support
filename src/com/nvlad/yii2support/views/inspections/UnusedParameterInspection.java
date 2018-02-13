package com.nvlad.yii2support.views.inspections;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.nvlad.yii2support.views.ViewUtil;
import com.nvlad.yii2support.views.ViewsUtil;
import com.nvlad.yii2support.views.index.ViewFileIndex;
import com.nvlad.yii2support.views.index.ViewInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

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

                final PsiElement[] renderParameters = reference.getParameters();
                if (renderParameters.length < 2 || !(renderParameters[0] instanceof StringLiteralExpression)) {
                    return;
                }

                final String key = ViewUtil.getViewPrefix(renderParameters[0]);
                if (key == null) {
                    return;
                }
                Project project = reference.getProject();
                final Collection<ViewInfo> views = FileBasedIndex.getInstance()
                        .getValues(ViewFileIndex.identity, key, GlobalSearchScope.projectScope(project));
                if (views.size() == 0) {
                    return;
                }

                final Collection<String> viewParameters = new HashSet<>();
                for (ViewInfo view : views) {
                    viewParameters.addAll(view.parameters);
                }

                if (viewParameters.size() == 0) {
                    if (renderParameters[1] instanceof ArrayCreationExpression || renderParameters[1] instanceof FunctionReference) {
                        String errorUnusedParameters = "This View does not use parameters";
                        UnusedParametersLocalQuickFix fix = new UnusedParametersLocalQuickFix();
                        problemsHolder.registerProblem(renderParameters[1], errorUnusedParameters, ProblemHighlightType.LIKE_UNUSED_SYMBOL, fix);
                        if (isOnTheFly) {
                            problemsHolder.registerProblem(reference, errorUnusedParameters, ProblemHighlightType.INFORMATION, fix);
                        }
                    }
                    return;
                }

                final String errorUnusedParameter = "View " + renderParameters[0].getText() + " not use \"%parameter%\" parameter";
                final Set<String> unusedParameters = new HashSet<>();
                BiConsumer<String, PsiElement> processParameter = (String arrayKey, PsiElement element) -> {
                    if (!viewParameters.contains(arrayKey)) {
                        UnusedParameterLocalQuickFix fix = new UnusedParameterLocalQuickFix(arrayKey);
                        String description = errorUnusedParameter.replace("%parameter%", arrayKey);
                        problemsHolder.registerProblem(element, description, ProblemHighlightType.LIKE_UNUSED_SYMBOL, fix);
                        unusedParameters.add(arrayKey);
                    }
                };

                if (renderParameters[1] instanceof ArrayCreationExpression) {
                    for (ArrayHashElement element : ((ArrayCreationExpression) renderParameters[1]).getHashElements()) {
                        if (element.getKey() != null && element.getKey() instanceof StringLiteralExpression) {
                            final String arrayKey = ((StringLiteralExpression) element.getKey()).getContents();
                            processParameter.accept(arrayKey, element);
                        }
                    }
                }

                if (renderParameters[1] instanceof FunctionReference) {
                    FunctionReference function = ((FunctionReference) renderParameters[1]);
                    if (function.getName() != null && function.getName().contains("compact")) {
                        for (PsiElement element : function.getParameters()) {
                            if (element instanceof StringLiteralExpression) {
                                String arrayKey = ((StringLiteralExpression) element).getContents();
                                processParameter.accept(arrayKey, element);
                            }
                        }
                    }
                }

                if (unusedParameters.size() > 0 && isOnTheFly) {
                    if (viewParameters.containsAll(unusedParameters)) {
                        String errorUnusedParameters = "This View does not use parameters";
                        UnusedParametersLocalQuickFix fix = new UnusedParametersLocalQuickFix();
                        problemsHolder.registerProblem(renderParameters[1], errorUnusedParameters, ProblemHighlightType.LIKE_UNUSED_SYMBOL, fix);
                        problemsHolder.registerProblem(reference, errorUnusedParameters, ProblemHighlightType.INFORMATION, fix);
                    } else {
                        String errorUnusedParameters = "This View have unused parameters";
                        UnusedParametersLocalQuickFix fix = new UnusedParametersLocalQuickFix(unusedParameters);
                        problemsHolder.registerProblem(reference, errorUnusedParameters, ProblemHighlightType.INFORMATION, fix);
                    }
                }
            }
        };
    }
}
