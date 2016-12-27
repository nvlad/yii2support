package com.yii2framework.completion.views;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NVlad on 27.12.2016.
 */
public class CompletionProvider extends com.intellij.codeInsight.completion.CompletionProvider {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        PhpPsiElement psiElement = (PhpPsiElement) completionParameters.getPosition().getParent().getParent().getParent();
        String psiElementName = psiElement.getName();

        if (psiElementName != null && psiElementName.startsWith("render")) {
            PsiDirectory viewsPath = getViewsPsiDirectory(completionParameters.getOriginalFile());

            for (Object view : getFileListForPath(viewsPath)) {
                completionResultSet.addElement(LookupElementBuilder.create(view));
            }
        }
    }

    private PsiDirectory getViewsPsiDirectory(PsiFile psiFile) {
        String fileName = psiFile.getName().substring(0, psiFile.getName().lastIndexOf("."));
        PsiDirectory psiDirectory = psiFile.getContainingDirectory();

        if (fileName.endsWith("Controller")) {
            psiDirectory = psiFile.getContainingDirectory().getParentDirectory();
            if (psiDirectory != null) {
                psiDirectory = psiDirectory.findSubdirectory("views");

                if (psiDirectory != null) {
                    String container = fileName.substring(0, fileName.length() - 10).toLowerCase();

                    psiDirectory = psiDirectory.findSubdirectory(container);
                }
            }
        }

        return psiDirectory;
    }

    private Object[] getFileListForPath(PsiDirectory psiDirectory) {
        List<String> files = new ArrayList<>();
        String filename;

        for (PsiFile psiFile : psiDirectory.getFiles()) {
            filename = psiFile.getName().substring(0, psiFile.getName().lastIndexOf("."));
            if (filename.contains(".")) {
                files.add(psiFile.getName());
            } else {
                files.add(filename);
            }
        }

        return files.toArray();
    }
}
