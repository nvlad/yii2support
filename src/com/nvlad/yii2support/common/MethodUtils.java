package com.nvlad.yii2support.common;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
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

    public static int paramIndexByRef(PsiElement paramRef) {
        if (paramRef.getParent().getParent() instanceof MethodReference) {
            MethodReference ref = (MethodReference) paramRef.getParent().getParent();
            PsiElement[] parameters = ref.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                PsiElement parameter = parameters[i];
                if (parameter == paramRef)
                    return i;
            }
        }
        return -1;
    }

    @Nullable
    public static PsiElement findParamRefByElement(PsiElement element) {
        int limit = 10;
        PsiElement prevElement = element;
        PsiElement currElement = element.getParent();
        while (limit > 0) {
            if (currElement instanceof ParameterList)
                return prevElement;
            else {
                prevElement = currElement;
                currElement = currElement.getParent();

                limit--;
            }
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
