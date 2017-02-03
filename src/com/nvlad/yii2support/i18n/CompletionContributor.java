package com.nvlad.yii2support.i18n;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.nvlad.yii2support.common.Patterns;
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
        MethodReference reference = PsiTreeUtil.getParentOfType(position, MethodReference.class);
        if (reference != null && reference.getName() != null && reference.getName().equals("t")) {
            ClassReference classReference = (ClassReference) reference.getClassReference();
            if (classReference == null || classReference.getName() == null || !classReference.getName().equals("Yii")) {
                return false;
            }
            if (typeChar == '\'' || typeChar == '"') {
                if (position instanceof LeafPsiElement && (position.getText().equals("$category") || position.getText().equals("$message"))) {
                    return true;
                }
                if (position.getNextSibling() instanceof ParameterList) {
                    return true;
                }
            }
        }

        return false;
    }

    private static ElementPattern<PsiElement> ElementPattern() {
        return PlatformPatterns.psiElement(PsiElement.class)
                .withSuperParent(3, Patterns.methodWithName("t"));
    }
}
