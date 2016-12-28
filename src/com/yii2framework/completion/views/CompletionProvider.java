package com.yii2framework.completion.views;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.template.macro.SplitWordsMacro;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 27.12.2016.
 */
public class CompletionProvider extends com.intellij.codeInsight.completion.CompletionProvider {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        PsiElement psiElement = completionParameters.getPosition();
        PhpPsiElement phpPsiElement = (PhpPsiElement) psiElement.getParent().getParent().getParent();
        String psiElementName = phpPsiElement.getName();

        if (psiElementName != null && psiElementName.startsWith("render")) {
            PsiDirectory viewsPath = getViewsPsiDirectory(completionParameters.getOriginalFile(), psiElement);

            if (viewsPath != null) {
                String enteredText = psiElement.getText().replaceAll("IntellijIdeaRulezzz ", "");
                for (PsiDirectory psiDirectory : viewsPath.getSubdirectories()) {
                    completionResultSet.addElement(new DirectoryLookupElement(psiDirectory, enteredText));
                }

                for (PsiFile psiFile : viewsPath.getFiles()) {
                    completionResultSet.addElement(new ViewLookupElement(psiFile, enteredText));
                }
            }
        }
    }

    private PsiDirectory getViewsPsiDirectory(PsiFile psiFile, PsiElement psiElement) {
        String fileName = psiFile.getName().substring(0, psiFile.getName().lastIndexOf("."));
        PsiDirectory psiDirectory = psiFile.getContainingDirectory();

        if (fileName.endsWith("Controller")) {
            psiDirectory = psiFile.getContainingDirectory().getParentDirectory();
            if (psiDirectory != null) {
                psiDirectory = psiDirectory.findSubdirectory("views");

                if (psiDirectory != null) {
                    String container = fileName.substring(0, fileName.length() - 10);
                    container = new SplitWordsMacro.LowercaseAndDash().convertString(container);

                    psiDirectory = psiDirectory.findSubdirectory(container);
                }
            }
        }

        String enteredText = psiElement.getText().replaceAll("IntellijIdeaRulezzz ", "");
        String enteredPath = enteredText;
        if (enteredText.startsWith("/")) {
            while (psiDirectory != null && !psiDirectory.getName().equals("views")) {
                psiDirectory = psiDirectory.getParentDirectory();
            }
            enteredPath = enteredPath.substring(1);
        }

        String directory;
        while (!enteredPath.equals("")) {
            if (enteredPath.contains("/")) {
                directory = enteredPath.substring(0, enteredPath.indexOf("/"));
                enteredPath = enteredPath.substring(directory.length() + 1);
                psiDirectory = psiDirectory.findSubdirectory(directory);
            } else {
                psiDirectory = psiDirectory.findSubdirectory(enteredPath);
                enteredPath = "";
            }
        }

        System.out.println(enteredPath);

        return psiDirectory;
    }
}
