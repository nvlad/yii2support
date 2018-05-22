package com.nvlad.yii2support.views.references;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.nvlad.yii2support.common.PhpUtil;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import com.nvlad.yii2support.views.entities.ViewInfo;
import com.nvlad.yii2support.views.entities.ViewResolve;
import com.nvlad.yii2support.views.entities.ViewResolveFrom;
import com.nvlad.yii2support.views.index.ViewFileIndex;
import com.nvlad.yii2support.views.util.ViewUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by NVlad on 02.01.2017.
 */
class PsiReferenceProvider extends com.intellij.psi.PsiReferenceProvider {
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
        Set<PsiReference> references = new HashSet<>();

        final ViewResolve resolve = ViewUtil.resolveView(psiElement);
        if (resolve != null) {
            Project project = psiElement.getProject();

            String key = resolve.key;
            if (FileUtilRt.getExtension(key).isEmpty()) {
                key = key + '.' + Yii2SupportSettings.getInstance(psiElement.getProject()).defaultViewExtension;
            }

            final Collection<ViewInfo> views = FileBasedIndex.getInstance()
                    .getValues(ViewFileIndex.identity, key, GlobalSearchScope.projectScope(project));

            if (views.size() > 0) {
                boolean localViewSearch = false;
                if (resolve.from == ViewResolveFrom.View) {
                    final String value = PhpUtil.getValue(psiElement);
                    localViewSearch = !value.startsWith("@") && !value.startsWith("//");
                }
                for (ViewInfo view : views) {
                    if (!resolve.application.equals(view.application)) {
                        continue;
                    }

                    if (localViewSearch && !resolve.theme.equals(view.theme)) {
                        continue;
                    }

                    PsiFile file = PsiManager.getInstance(project).findFile(view.getVirtualFile());
                    if (file != null) {
                        references.add(new PsiReference(psiElement, file));
                    }
                }
            }
        }

        return references.toArray(new PsiReference[0]);
    }
}
