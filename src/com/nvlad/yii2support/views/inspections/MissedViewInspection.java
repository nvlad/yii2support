package com.nvlad.yii2support.views.inspections;

import com.google.common.io.Files;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.PhpUtil;
import com.nvlad.yii2support.common.YiiApplicationUtils;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import com.nvlad.yii2support.views.entities.ViewInfo;
import com.nvlad.yii2support.views.entities.ViewResolve;
import com.nvlad.yii2support.views.entities.ViewResolveFrom;
import com.nvlad.yii2support.views.index.ViewFileIndex;
import com.nvlad.yii2support.views.util.ViewUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Created by NVlad on 15.01.2017.
 */
final public class MissedViewInspection extends PhpInspection {
    @NotNull
    @Override
    public String getShortName() {
        return "MissedViewInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean isOnTheFly) {
        return new PhpElementVisitor() {
            @Override
            public void visitPhpMethodReference(MethodReference reference) {
                if (!ViewUtil.isValidRenderMethod(reference)) {
                    return;
                }

                if (ArrayUtil.contains(reference.getName(), ViewUtil.renderMethods)) {
                    if (reference.getParameters().length > 0) {
                        final PsiElement pathParameter = reference.getParameters()[0];
                        final ViewResolve resolve = ViewUtil.resolveView(pathParameter);
                        if (resolve == null) {
                            return;
                        }

                        String key = resolve.key;
                        if (Files.getFileExtension(key).isEmpty()) {
                            key = key + '.' + Yii2SupportSettings.getInstance(reference.getProject()).defaultViewExtension;
                        }

                        Project project = reference.getProject();
                        final Collection<ViewInfo> views = FileBasedIndex.getInstance()
                                .getValues(ViewFileIndex.identity, key, GlobalSearchScope.projectScope(project));

                        final String application = YiiApplicationUtils.getApplicationName(reference.getContainingFile());
                        final boolean localViewSearch;
                        if (resolve.from == ViewResolveFrom.View) {
                            final String value = PhpUtil.getValue(pathParameter);
                            localViewSearch = !value.startsWith("@") && !value.startsWith("//");
                        } else {
                            localViewSearch = false;
                        }

                        views.removeIf(view -> {
                            if (!application.equals(view.application)) {
                                return true;
                            }

                            return localViewSearch && !resolve.theme.equals(view.theme);
                        });
                        if (views.size() != 0) {
                            return;
                        }

                        PhpClass clazz = ClassUtils.getPhpClassByCallChain(reference);
                        if (clazz != null) {
                            Method method = clazz.findMethodByName("getViewPath");
                            PhpIndex phpIndex = PhpIndex.getInstance(reference.getProject());
                            if (method != null) {
                                PhpClass containingClass = method.getContainingClass();
                                PhpClass controllerBaseClass = ClassUtils.getClass(phpIndex, "yii\\base\\Controller");
                                PhpClass widgetBaseClass = ClassUtils.getClass(phpIndex, "yii\\base\\Widget");
                                if (containingClass != controllerBaseClass && containingClass != widgetBaseClass) {
                                    return;
                                }
                            }
                        }

                        if (pathParameter instanceof StringLiteralExpression) {
                            String path = ((StringLiteralExpression) pathParameter).getContents();
                            final String errorViewNotFoundTemplate = "View file for \"%name%\" not found.";
                            final MissedViewLocalQuickFix quickFix = new MissedViewLocalQuickFix(path);
                            final String descriptionTemplate = errorViewNotFoundTemplate.replace("%name%", path);
                            final PsiElement stringPart = pathParameter.findElementAt(1);
                            if (stringPart != null) {
                                problemsHolder.registerProblem(stringPart, descriptionTemplate, quickFix);
                            }
                        }
                    }
                }
            }
        };
    }
}
