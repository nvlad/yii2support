package com.nvlad.yii2support.url;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

/**
 * Created by NVlad on 02.02.2017.
 */
public class UrlRenameProcessor extends RenamePsiElementProcessor {

    PsiElement element;

    @Override
    public boolean canProcessElement(@NotNull PsiElement psiElement) {

        return psiElement instanceof StringLiteralExpression;
    }

    @Override
    public void prepareRenaming(PsiElement psiElement, String s, Map<PsiElement, String> map) {
        element = psiElement;
    }

    @Nullable
    @Override
    public Runnable getPostRenameCallback(PsiElement psiElement, String s, RefactoringElementListener refactoringElementListener) {
        return () -> {
            element = element;
        };
    }
}
