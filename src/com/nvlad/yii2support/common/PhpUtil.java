package com.nvlad.yii2support.common;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

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

    @NotNull
    public static String getValue(PsiElement expression) {
        if (expression instanceof StringLiteralExpression) {
            String value = ((StringLiteralExpression) expression).getContents();
            if (value.contains("IntellijIdeaRulezzz ")) {
                return value.substring(0, value.indexOf("IntellijIdeaRulezzz "));
            }
            return value;
        }

        return "";
    }
}
