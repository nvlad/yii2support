package com.nvlad.yii2support.views.util;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class RenderUtil {
    @NotNull
    public static Map<String, PhpType> getViewArguments(MethodReference reference) {
        final Map<String, PhpType> result = new LinkedHashMap<>();
        result.put("this", new PhpType.PhpTypeBuilder().add(Yii2SupportSettings.getInstance(reference.getProject()).defaultViewClass).build());

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
                    continue;
                }

                PhpType valueType;
                final PhpPsiElement valueElement = item.getValue();
                if (valueElement instanceof PhpExpression) {
                    valueType = ((PhpTypedElement) valueElement).getType().global(valueElement.getProject());
                } else {
                    continue;
                }

                result.put(key, valueType);
            }

            return result;
        }

        return result;
    }
}
