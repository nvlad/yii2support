package com.yii2support.i18n;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.yii2support.common.Patterns;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 06.01.2017.
 */
public class CompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {
    public CompletionContributor() {
        extend(CompletionType.BASIC, ElementPattern(), new CompletionProvider());
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        if (typeChar == '\'' || typeChar == '"') {
            if (position instanceof LeafPsiElement && (position.getText().equals("$category") || position.getText().equals("$message"))) {
                return true;
            }
            if (position.getNextSibling() instanceof ParameterList || position.getParent() instanceof MethodReference) {
                return true;
            }
        }

        return false;
    }

    private static ElementPattern<PsiElement> ElementPattern() {
        return PlatformPatterns.psiElement(PsiElement.class)
                .withSuperParent(3, Patterns.methodWithName("t"));
    }
}
