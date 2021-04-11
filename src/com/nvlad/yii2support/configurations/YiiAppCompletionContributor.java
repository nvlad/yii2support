package com.nvlad.yii2support.configurations;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import com.jetbrains.php.lang.psi.elements.FieldReference;

public class YiiAppCompletionContributor extends CompletionContributor {
    public YiiAppCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withParent(FieldReference.class), new YiiAppCompletionProvider());
    }

}
