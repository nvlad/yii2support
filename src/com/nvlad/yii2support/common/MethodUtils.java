package com.nvlad.yii2support.common;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.PhpPsiElement;
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl;
import org.jetbrains.annotations.Nullable;

/**
 * Created by oleg on 2017-07-23.
 */
public class MethodUtils {
    @Nullable
    public static PsiElement getParameter(MethodReference methodRef, int index) {
        if (methodRef.getParameters().length >= index) {
            return methodRef.getParameters()[index];
        }
        return null;
    }

    public static boolean isYiiCreateObjectMethod(PsiElement psiElement) {
        if (psiElement instanceof MethodReference) {
            MethodReference referenceMethod = (MethodReference) psiElement;
            if (referenceMethod.getName() != null && referenceMethod.getName().equals("createObject")
                    && referenceMethod.getParameters().length > 0) {
                PhpExpression classReference = ((MethodReferenceImpl) psiElement).getClassReference();
                if (classReference != null && classReference.getName() != null && classReference.getName().equals("Yii")) {
                    return true;
                }
            }
        }
        return false;
    }
}
