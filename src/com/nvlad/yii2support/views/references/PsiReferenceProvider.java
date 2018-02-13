package com.nvlad.yii2support.views.references;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.nvlad.yii2support.common.ApplicationUtils;
import com.nvlad.yii2support.views.ViewUtil;
import com.nvlad.yii2support.views.index.ViewFileIndex;
import com.nvlad.yii2support.views.index.ViewInfo;
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

        final String key = ViewUtil.getViewPrefix(psiElement);
        if (key != null) {
            Project project = psiElement.getProject();
            final Collection<ViewInfo> views = FileBasedIndex.getInstance()
                    .getValues(ViewFileIndex.identity, key, GlobalSearchScope.projectScope(project));

            final String application = ApplicationUtils.getApplicationName(psiElement.getContainingFile());
            if (views.size() > 0) {
                for (ViewInfo view : views) {
                    if (!application.equals(view.application)) {
                        continue;
                    }

                    PsiFile file = PsiManager.getInstance(project).findFile(view.getVirtualFile());
                    if (file != null) {
                        PsiReference reference = new PsiReference(psiElement, file);
                        references.add(reference);
                    }
                }
            }
        }

        return references.toArray(new PsiReference[0]);
    }
}
