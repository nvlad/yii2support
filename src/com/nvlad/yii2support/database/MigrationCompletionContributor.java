package com.nvlad.yii2support.database;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.ClassUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Created by oleg on 24.03.2017.
 */
public class MigrationCompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {
    public MigrationCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new MigrationCompletionProvider());
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        //((MethodReferenceImpl) position.getParent()).resolve().getParent()
        if ((typeChar == '\'' || typeChar == '"')  ) {
            if (position.getParent() instanceof MethodReference) {
                Method method = (Method)((MethodReference) position.getParent()).resolve();
                Object possibleClass = method.getParent();
                if (possibleClass instanceof PhpClass) {
                    if (((PhpClass)possibleClass).getName().equals("Migration")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static ElementPattern<PsiElement> ElementPattern() {
        return  PlatformPatterns.psiElement().withSuperParent(3, MethodReference.class).withParent(StringLiteralExpression.class);
    }
}
