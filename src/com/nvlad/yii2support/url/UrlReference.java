package com.nvlad.yii2support.url;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.UrlUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Created by oleg on 14.03.2017.
 */
public class UrlReference extends PsiReferenceBase<PsiElement> {
    UrlReference(@NotNull PsiElement element)
    {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {

        if (myElement instanceof StringLiteralExpression) {
            HashMap<String, Method> routes = UrlUtils.getRoutes(myElement.getProject());
            Method method = routes.get(((StringLiteralExpression) myElement).getContents());
            return method;

            /*
            PhpClass phpClass = ObjectFactoryUtils.findClassByArrayCreation(arrayCreation, dir);

            if (phpClass != null) {
                PsiElement field = ClassUtils.findWritableField(phpClass, myElement.getText());
                return field;
            }
            */
        }
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
