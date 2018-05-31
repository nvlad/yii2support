package com.nvlad.yii2support.views.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.nvlad.yii2support.common.PhpUtil;
import com.nvlad.yii2support.common.YiiApplicationUtils;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import com.nvlad.yii2support.views.entities.ViewInfo;
import com.nvlad.yii2support.views.entities.ViewResolve;
import com.nvlad.yii2support.views.entities.ViewResolveFrom;
import com.nvlad.yii2support.views.index.ViewFileIndex;
import com.nvlad.yii2support.views.util.RenderUtil;
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
                        if (FileUtilRt.getExtension(key).isEmpty()) {
                            key = key + '.' + Yii2SupportSettings.getInstance(reference.getProject()).defaultViewExtension;
                        }

                        final Project project = reference.getProject();
                        final Collection<ViewInfo> views = FileBasedIndex.getInstance()
                                .getValues(ViewFileIndex.identity, key, GlobalSearchScope.projectScope(project));

                        final String application = YiiApplicationUtils.getApplicationName(reference.getContainingFile());
                        final boolean localViewSearch;
                        final String value = PhpUtil.getValue(pathParameter);
                        if (resolve.from == ViewResolveFrom.View) {
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

                        if (pathParameter instanceof StringLiteralExpression) {
                            Collection<String> paths = ViewUtil.viewResolveToPaths(resolve, project);
                            if (!paths.iterator().hasNext()) {
                                return;
                            }

                            VirtualFile yiiRoot = YiiApplicationUtils.getYiiRootVirtualFile(project);
                            if (yiiRoot == null) {
                                return;
                            }

                            int projectUrlLength = project.getBaseDir().getUrl().length();
                            String yiiRootUrl = yiiRoot.getUrl();
                            String path;
                            if (projectUrlLength > yiiRootUrl.length()) {
                                path = paths.iterator().next();
                            } else {
                                path = yiiRootUrl.substring(projectUrlLength) + paths.iterator().next();
                            }
                            final String viewNotFoundMessage = "View file for \"" + value + "\" not found in \"" + path + "\".";
                            final MissedViewLocalQuickFix quickFix = new MissedViewLocalQuickFix(value, path, RenderUtil.getViewArguments(reference));
                            final PsiElement stringPart = pathParameter.findElementAt(1);
                            if (stringPart != null) {
                                problemsHolder.registerProblem(stringPart, viewNotFoundMessage, quickFix);
                            }
                        }
                    }
                }
            }
        };
    }
}
