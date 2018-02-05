package com.nvlad.yii2support.url;

import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssFileType;
import com.intellij.util.ProcessingContext;

import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oleg on 14.03.2017.
 */
public class UrlReferenceProvider extends com.intellij.psi.PsiReferenceProvider {
    @NotNull
    @Override
    public UrlReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
        List<UrlReference> references = new ArrayList<>();

        if (psiElement instanceof StringLiteralExpression) {
            UrlReference reference = new UrlReference(psiElement);
            references.add(reference);
        }


        return references.toArray(new UrlReference[references.size()]);
    }
}
