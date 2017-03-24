package com.nvlad.yii2support.phpdoc;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceService;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by NVlad on 24.03.2017.
 */
public class PhpDocReferenceProvider extends com.intellij.psi.PsiReferenceProvider {
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        context.toString();

        return new PsiReference[0];
    }

    @Override
    public boolean acceptsHints(@NotNull PsiElement element, @NotNull PsiReferenceService.Hints hints) {
        if (hints.target != null) {
            String text = element.getText();
            boolean accept = text.contains("[[");

            if (accept) {
                Pattern pattern = Pattern.compile("(\\[\\[[\\w]+]])", Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    System.out.println("accepted: " + matcher.group(1));
                }

            }

            return accept;
        }

        return super.acceptsHints(element, hints);
    }
}
