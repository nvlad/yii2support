package com.nvlad.yii2support.views.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.nvlad.yii2support.views.ViewsUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 27.12.2016.
 */
class CompletionProvider extends com.intellij.codeInsight.completion.CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        final PsiElement psiElement = completionParameters.getPosition();
        final MethodReference method = PsiTreeUtil.getParentOfType(psiElement, MethodReference.class);

        if (method == null || method.getParameters().length == 0) {
            return;
        }

        if (!ViewsUtil.isValidRenderMethod(method)) {
            return;
        }

        PsiElement parameter = psiElement;
        while (parameter != null && !(parameter.getParent() instanceof ParameterList)) {
            parameter = parameter.getParent();
        }
        if (parameter == null || !parameter.equals(method.getParameters()[0])) {
            return;
        }

        String path = getValue(method.getParameters()[0]);
        PsiDirectory directory;
        if (path.startsWith("/")) {
            path = path.substring(1);
            directory = ViewsUtil.getRootDirectory(psiElement);
        } else {
            directory = ViewsUtil.getContextDirectory(psiElement);
        }
        if (path.contains("/")) {
            path = path.substring(0, path.lastIndexOf('/') + 1);
        }

        while (path.contains("/") && directory != null) {
            String subdirectory = path.substring(0, path.indexOf('/'));
            path = path.substring(path.indexOf('/') + 1);
            directory = subdirectory.equals("..") ? directory.getParent() : directory.findSubdirectory(subdirectory);
        }
        if (directory != null) {
            if (completionResultSet.getPrefixMatcher().getPrefix().contains("/")) {
                String prefix = completionResultSet.getPrefixMatcher().getPrefix();
                prefix = prefix.substring(prefix.lastIndexOf("/") + 1);
                completionResultSet = completionResultSet.withPrefixMatcher(prefix);
            }

            for (PsiDirectory psiDirectory : directory.getSubdirectories()) {
                completionResultSet.addElement(new DirectoryLookupElement(psiDirectory));
            }

            for (PsiFile psiFile : directory.getFiles()) {
                completionResultSet.addElement(new ViewLookupElement(psiFile));
            }
        }
    }

    @NotNull
    private String getValue(PsiElement expression) {
        if (expression instanceof StringLiteralExpression) {
            String value = ((StringLiteralExpression) expression).getContents();
            return value.substring(0, value.indexOf("IntellijIdeaRulezzz "));
        }

        return "";
    }
}
