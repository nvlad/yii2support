package com.nvlad.yii2support.objectfactory;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.impl.ArrayCreationExpressionImpl;
import com.nvlad.yii2support.common.Patterns;
import org.jetbrains.annotations.NotNull;

/**
 * Created by oleg on 16.02.2017.
 */
public class ObjectFactoryCompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {
    public ObjectFactoryCompletionContributor() {
        extend(CompletionType.BASIC, ElementPattern(), new ObjectFactoryCompletionProvider());
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
       // Object reference = PsiTreeUtil.getParentOfType(position, MethodReference.class);
        Object parent = position.getParent();
        if (parent instanceof ArrayCreationExpressionImpl) {
            ArrayCreationExpressionImpl arrayExpr = (ArrayCreationExpressionImpl)parent;
        }
        return false;
    }

    private static ElementPattern<PsiElement> ElementPattern() {
        return PlatformPatterns.psiElement()
                .withSuperParent(3, Patterns.arrayCreation());
    }
}
