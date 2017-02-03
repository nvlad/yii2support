package com.nvlad.yii2support.views.completion;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.nvlad.yii2support.common.Patterns;
import com.nvlad.yii2support.views.ViewsUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 27.12.2016.
 */
public class CompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {
    public CompletionContributor() {
        extend(CompletionType.BASIC, ElementPattern(), new CompletionProvider());
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        MethodReference reference = PsiTreeUtil.getParentOfType(position, MethodReference.class);
        if (reference != null && ArrayUtil.contains(reference.getName(), ViewsUtil.renderMethods)) {
            if (typeChar == '\'' || typeChar == '"') {
                if (position instanceof LeafPsiElement && position.getText().equals("$view")) {
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
        return PlatformPatterns.psiElement()
                .withSuperParent(3, Patterns.methodWithName(ViewsUtil.renderMethods));
    }
}
