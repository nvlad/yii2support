package com.nvlad.yii2support.database;

import com.intellij.codeInsight.completion.*;
import com.intellij.openapi.util.Pair;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.MultiMap;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.ClassUtils;
import org.jetbrains.annotations.NotNull;

public class QueryCompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {
    private final MultiMap<CompletionType, Pair<ElementPattern<? extends PsiElement>, CompletionProvider<CompletionParameters>>> myMap = new MultiMap();

    public QueryCompletionContributor() {
        extend(CompletionType.BASIC, ElementPattern(), new QueryCompletionProvider());
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        if ((typeChar == '\'' || typeChar == '"' || typeChar == '.')  ) {
            MethodReference methodRef = ClassUtils.getMethodRef(position, 2);
            if (methodRef != null) {
                Method method = (Method)methodRef.resolve();
                if (method != null) {
                    Object possibleClass = method.getParent();
                    if (possibleClass instanceof PhpClass) {
                        PhpIndex index = PhpIndex.getInstance(method.getProject());
                        if (ClassUtils.isClassInheritsOrEqual((PhpClass)possibleClass, ClassUtils.getClass(index, "\\yii\\db\\Query")) ||
                                ClassUtils.isClassInheritsOrEqual((PhpClass)possibleClass, ClassUtils.getClass(index, "\\yii\\db\\Command")) ||
                                ClassUtils.isClassInheritsOrEqual((PhpClass)possibleClass, ClassUtils.getClass(index, "\\yii\\db\\Query"))
                                ) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
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
