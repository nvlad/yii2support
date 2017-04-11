package com.nvlad.yii2support.common;

import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by NVlad on 23.01.2017.
 */
public class PhpUtil {
    @NotNull
    public static Collection<String> getArrayKeys(ArrayCreationExpression array) {
        final HashSet<String> result = new HashSet<>();

        Iterable<ArrayHashElement> items = array.getHashElements();

        for (ArrayHashElement item : items) {
            if (item.getKey() != null && item.getKey() instanceof StringLiteralExpression) {
                result.add(((StringLiteralExpression) item.getKey()).getContents());
            }
        }

        return result;
    }

    @Nullable
    public static PhpClass getPhpClass(PhpPsiElement phpPsiElement) {
        while (phpPsiElement != null) {
            if (phpPsiElement instanceof ClassReference) {
                return (PhpClass) ((ClassReference) phpPsiElement).resolve();
            }
            if (phpPsiElement instanceof NewExpression) {
                ClassReference classReference = ((NewExpression) phpPsiElement).getClassReference();
                if (classReference != null) {
                    PhpPsiElement resolve = (PhpPsiElement) classReference.resolve();
                    if (resolve instanceof PhpClass) {
                        return (PhpClass) resolve;
                    }
                }
            }
            if (phpPsiElement instanceof MethodReference && phpPsiElement.getFirstPsiChild() instanceof ParenthesizedExpression) {
                final ClassReference classReference = ((NewExpression) ((ParenthesizedExpression) phpPsiElement.getFirstChild()).unparenthesize()).getClassReference();
                if (classReference != null) {
                    return (PhpClass) classReference.resolve();
                }
            }

            phpPsiElement = (PhpPsiElement) phpPsiElement.getParent();
        }

        return null;
    }
}
