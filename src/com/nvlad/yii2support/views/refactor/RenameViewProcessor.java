package com.nvlad.yii2support.views.refactor;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.nvlad.yii2support.views.index.ViewFileIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

/**
 * Created by NVlad on 02.02.2017.
 */
public class RenameViewProcessor extends RenamePsiElementProcessor {
    private final static Key<Boolean> WITH_EXT = Key.create("RenameViewProcessor.withExt");
    private final static Key<String> OLD_EXT = Key.create("RenameViewProcessor.oldExt");
    private final static Key<String> RELATIVE_PATH = Key.create("RenameViewProcessor.relativePath");

    private final HashSet<PsiElement> renders = new HashSet<>();

    @Override
    public boolean canProcessElement(@NotNull PsiElement psiElement) {
        return psiElement instanceof PhpFile;
    }

    @Override
    public void prepareRenaming(PsiElement psiElement, String s, Map<PsiElement, String> map) {
        renders.clear();
        for (PsiReference reference : findReferences(psiElement)) {
            final PsiElement element = reference.getElement();
            if (element instanceof StringLiteralExpression) {
                String fileName = ((StringLiteralExpression) element).getContents();
                if (fileName.contains("/")) {
                    element.getParent().putUserData(RELATIVE_PATH, fileName.substring(0, fileName.lastIndexOf('/') + 1));
                    fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
                }
                element.getParent().putUserData(WITH_EXT, fileName.contains("."));

                String realFile = ((PsiFile) psiElement).getName();
                if (realFile.contains(".")) {
                    element.getParent().putUserData(OLD_EXT, realFile.substring(realFile.lastIndexOf('.')));
                }

                renders.add(element.getParent());
            }
        }
    }

    @Nullable
    @Override
    public Runnable getPostRenameCallback(PsiElement psiElement, String s, RefactoringElementListener refactoringElementListener) {
        return () -> {
//            final Project project = psiElement.getProject();
            FileBasedIndex.getInstance().requestRebuild(ViewFileIndex.identity);

            for (PsiElement render : renders) {
                StringLiteralExpression element = (StringLiteralExpression) ((ParameterList) render).getParameters()[0];
                final PsiElement parent = element.getParent();
                String fileName = element.getContents();
                if (Objects.equals(parent.getUserData(WITH_EXT), false)) {
                    if (Objects.equals(fileName.substring(fileName.lastIndexOf('.')), parent.getUserData(OLD_EXT))) {
                        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                    }
                }
                parent.putUserData(WITH_EXT, null);
                parent.putUserData(OLD_EXT, null);

                if (parent.getUserData(RELATIVE_PATH) != null) {
                    fileName = parent.getUserData(RELATIVE_PATH) + fileName;
                    parent.putUserData(RELATIVE_PATH, null);
                }

                fileName = element.isSingleQuote() ? "'" + fileName + "'" : "\"" + fileName + "\"";
                PsiElement newValue = PhpPsiElementFactory.createFromText(psiElement.getProject(), StringLiteralExpression.class, fileName);
                if (newValue != null) {
                    element.replace(newValue);
                }
            }
        };
    }
}
