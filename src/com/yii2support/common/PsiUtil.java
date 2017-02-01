package com.yii2support.common;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;

/**
 * Created by NVlad on 17.01.2017.
 */
public class PsiUtil {
    public static void deleteArrayElement(PsiElement element) {
        PsiElement next = element.getNextSibling();
        String endArray = ((ArrayCreationExpression) element.getParent()).isShortSyntax() ? "]" : ")";

        if (next instanceof PsiWhiteSpace && next.getNextSibling().getText() != null) {
            if (next.getNextSibling().getText().equals(endArray)) {
                next = next.getNextSibling();
            }
        }
        if (next.getText().equals(endArray)) {
            Boolean deleteComma = false;
            if (element.getPrevSibling() instanceof PsiWhiteSpace) {
                deleteComma = !element.getPrevSibling().getText().contains("\n");
                element.getPrevSibling().delete();
            }
            if (deleteComma && element.getPrevSibling().getText().equals(",")) {
                element.getPrevSibling().delete();
            }
        }
        if (next.getText().equals(",")) {
            if (next.getNextSibling() instanceof PsiWhiteSpace) {
                next.getNextSibling().delete();
            }
            next.delete();
        }
        element.delete();
    }

    public static void deleteFunctionParam(PsiElement element) {
        PsiElement next = element.getNextSibling();
        if (next != null && next.getText().equals(",")) {
            next.delete();
        } else {
            PsiElement prev = element.getPrevSibling();
            if (prev != null && prev instanceof PsiWhiteSpace) {
                prev.delete();
                prev = element.getPrevSibling();
            }
            if (prev != null && prev.getText().equals(",")) {
                prev.delete();
            }
        }
        element.delete();
    }
}
