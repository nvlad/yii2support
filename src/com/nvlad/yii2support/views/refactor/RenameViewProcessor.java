package com.nvlad.yii2support.views.refactor;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.refactoring.RefactoringSettings;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenamePsiFileProcessor;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import com.nvlad.yii2support.views.index.ViewFileIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by NVlad on 02.02.2017.
 */
public class RenameViewProcessor extends RenamePsiFileProcessor {
    private final Set<PsiElement> renders = new HashSet<>();

    @Override
    public boolean canProcessElement(@NotNull PsiElement psiElement) {
        return psiElement instanceof PhpFile;
    }

    @Override
    public void prepareRenaming(PsiElement psiElement, String s, Map<PsiElement, String> map) {
        renders.clear();

        if (!RefactoringSettings.getInstance().RENAME_SEARCH_FOR_REFERENCES_FOR_FILE) {
            return;
        }

        for (PsiReference reference : findReferences(psiElement)) {
            final PsiElement element = reference.getElement();
            if (element instanceof StringLiteralExpression) {
                renders.add(element.getParent());
            }
        }
    }

    @Nullable
    @Override
    public Runnable getPostRenameCallback(PsiElement psiElement, String s, RefactoringElementListener refactoringElementListener) {
        return () -> {
            final Yii2SupportSettings settings = Yii2SupportSettings.getInstance(psiElement.getProject());

            for (PsiElement render : renders) {
                final StringLiteralExpression element = (StringLiteralExpression) ((ParameterList) render).getParameters()[0];
                String fileName = element.getContents();
                if (fileName.endsWith("." + settings.defaultViewExtension)) {
                    fileName = fileName.substring(0, fileName.length() - settings.defaultViewExtension.length() - 1);
                }

                fileName = element.isSingleQuote() ? "'" + fileName + "'" : "\"" + fileName + "\"";
                final PsiElement newValue = PhpPsiElementFactory.createFromText(psiElement.getProject(), StringLiteralExpression.class, fileName);
                if (newValue != null) {
                    element.replace(newValue);
                }
            }

            FileBasedIndex.getInstance().requestRebuild(ViewFileIndex.identity);
        };
    }
}
