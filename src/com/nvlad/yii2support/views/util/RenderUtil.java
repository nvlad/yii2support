package com.nvlad.yii2support.views.util;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class RenderUtil {
    @NotNull
    public static Map<String, String> getViewParameters(MethodReference reference) {
        final Map<String, String> result = new LinkedHashMap<>();
        result.put("this", Yii2SupportSettings.getInstance(reference.getProject()).defaultViewClass);

        ParameterList parameterList = reference.getParameterList();
        if (parameterList == null) {
            return result;
        }

        if (parameterList.getParameters().length == 1) {
            return result;
        }

        final PsiElement parameter = parameterList.getParameters()[1];
        if (parameter instanceof ArrayCreationExpression) {
            final ArrayCreationExpression array = (ArrayCreationExpression) parameter;
            for (ArrayHashElement item : array.getHashElements()) {
                PhpPsiElement keyElement = item.getKey();

                String key;
                if (keyElement instanceof StringLiteralExpression) {
                    key = ((StringLiteralExpression) keyElement).getContents();
                } else {
                    return result;
                }

                String valueType;
                final PhpPsiElement valueElement = item.getValue();
                if (valueElement instanceof PhpExpression) {
                    valueType = ((PhpExpression) valueElement).getType().toString();
                } else {
                    return result;
                }

                result.put(key, valueType);
            }

            return result;
        }

        return result;
    }
}
