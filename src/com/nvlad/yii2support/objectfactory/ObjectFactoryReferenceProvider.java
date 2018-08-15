package com.nvlad.yii2support.objectfactory;

import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oleg on 14.03.2017.
 */
public class ObjectFactoryReferenceProvider extends com.intellij.psi.PsiReferenceProvider {
    @NotNull
    @Override
    public ObjectFactoryReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext) {
        List<ObjectFactoryReference> references = new ArrayList<>();

        ObjectFactoryReference reference = new ObjectFactoryReference(psiElement);
        references.add(reference);


        return references.toArray(new ObjectFactoryReference[0]);
    }
}
