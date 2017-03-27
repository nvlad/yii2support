package com.nvlad.yii2support.database;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;

public class ActiveQueryCompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {
    public ActiveQueryCompletionContributor() {
        extend(CompletionType.BASIC, ElementPattern(), new ActiveQueryCompletionProvider());
    }



    private static ElementPattern<PsiElement> ElementPattern() {

         return
                 PlatformPatterns.or(
                         // ["<caret>
                     PlatformPatterns.psiElement().withSuperParent(3, ArrayCreationExpression.class),
                         // string
                     PlatformPatterns.psiElement().withSuperParent(3, MethodReference.class).withParent(StringLiteralExpression.class),
                         // ["<caret>" => ""]
                         PlatformPatterns.psiElement().withSuperParent(4, ArrayCreationExpression.class) );


    }
}
