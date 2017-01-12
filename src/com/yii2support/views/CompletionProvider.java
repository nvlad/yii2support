package com.yii2support.views;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.template.macro.SplitWordsMacro;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 27.12.2016.
 */
public class CompletionProvider extends com.intellij.codeInsight.completion.CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        PsiElement psiElement = completionParameters.getPosition();
        MethodReference methodReference = (MethodReference) psiElement.getParent().getParent().getParent();
        String methodName = methodReference.getName();

        if (ArrayUtil.indexOf(methodReference.getParameters(), psiElement.getParent()) == 0) {
            if (methodName != null) {
                if (completionResultSet.getPrefixMatcher().getPrefix().contains("/")) {
                    String prefix = completionResultSet.getPrefixMatcher().getPrefix();
                    prefix = prefix.substring(prefix.lastIndexOf("/") + 1);
                    completionResultSet = completionResultSet.withPrefixMatcher(prefix);
                }
                PsiDirectory viewsPath = getViewsPsiDirectory(completionParameters.getOriginalFile(), psiElement);

                if (viewsPath != null) {
                    for (PsiDirectory psiDirectory : viewsPath.getSubdirectories()) {
                        completionResultSet.addElement(new DirectoryLookupElement(psiDirectory));
                    }

                    for (PsiFile psiFile : viewsPath.getFiles()) {
                        completionResultSet.addElement(new ViewLookupElement(psiFile));
                    }
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

        String enteredText = psiElement.getText();
        enteredText = enteredText.substring(0, enteredText.indexOf("IntellijIdeaRulezzz "));
        String enteredPath = enteredText;
        if (enteredText.startsWith("/")) {
            while (psiDirectory != null && !psiDirectory.getName().equals("views")) {
                psiDirectory = psiDirectory.getParentDirectory();
            }
            enteredPath = enteredPath.substring(1);
        }

        if (!enteredPath.endsWith("/") && enteredPath.contains("/")) {
            enteredPath = enteredPath.substring(0, enteredPath.lastIndexOf("/") + 1);
            if (enteredPath.length() == 1) {
                enteredPath = "";
            }
        }

        if (enteredPath.endsWith("/")) {
            String directory;
            while (!enteredPath.equals("")) {
                directory = enteredPath.substring(0, enteredPath.indexOf("/"));
                enteredPath = enteredPath.substring(directory.length() + 1);
                if (psiDirectory != null) {
                    psiDirectory = psiDirectory.findSubdirectory(directory);
                }
            }
        }

        return psiDirectory;
    }
}
