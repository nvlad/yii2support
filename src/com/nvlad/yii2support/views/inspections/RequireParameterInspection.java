package com.nvlad.yii2support.views.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.nvlad.yii2support.common.ApplicationUtils;
import com.nvlad.yii2support.common.PhpUtil;
import com.nvlad.yii2support.views.ViewUtil;
import com.nvlad.yii2support.views.ViewsUtil;
import com.nvlad.yii2support.views.index.ViewFileIndex;
import com.nvlad.yii2support.views.index.ViewInfo;
import org.jetbrains.annotations.NotNull;

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
                if (!ViewsUtil.isValidRenderMethod(reference)) {
                    return;
                }

                final String name = reference.getName();
                if (name == null || !ArrayUtil.contains(name, ViewsUtil.renderMethods)) {
                    return;
                }

                final PsiElement[] renderParameters = reference.getParameters();
                if (renderParameters.length == 0 || !(renderParameters[0] instanceof StringLiteralExpression)) {
                    return;
                }

                final String key = ViewUtil.getViewPrefix(renderParameters[0]);
                if (key == null) {
                    return;
                }
                Project project = reference.getProject();
                final Collection<ViewInfo> views = FileBasedIndex.getInstance()
                        .getValues(ViewFileIndex.identity, key, GlobalSearchScope.projectScope(project));
                final String application = ApplicationUtils.getApplicationName(reference.getContainingFile());
                views.removeIf(viewInfo -> !application.equals(viewInfo.application));
                if (views.size() == 0) {
                    return;
                }

                final Collection<String> viewParameters = new HashSet<>();
                for (ViewInfo view : views) {
                    viewParameters.addAll(view.parameters);
                }
                if (viewParameters.size() == 0) {
                    return;
                }


                final Collection<String> existKeys;
                if (renderParameters.length > 1) {
                    if (renderParameters[1] instanceof ArrayCreationExpression) {
                        existKeys = PhpUtil.getArrayKeys((ArrayCreationExpression) renderParameters[1]);
                    } else if (renderParameters[1] instanceof FunctionReference) {
                        FunctionReference function = (FunctionReference) renderParameters[1];
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
                    problemsHolder.registerProblem(reference, errorRequireParameters.replace("%view%", renderParameters[0].getText()), fix);
                    return;
                }

                viewParameters.removeIf(existKeys::contains);
                if (viewParameters.size() > 0) {
                    String errorRequireParameter = "View %view% require %parameter% parameter";
                    for (String parameter : viewParameters) {
                        RequireParameterLocalQuickFix fix = new RequireParameterLocalQuickFix(viewParameters);
                        String description = errorRequireParameter
                                .replace("%view%", renderParameters[0].getText())
                                .replace("%parameter%", parameter);
                        problemsHolder.registerProblem(reference, description, fix);
                    }
                }
            }
        };
    }
}
