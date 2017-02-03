package com.nvlad.yii2support.components;

import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocProperty;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by NVlad on 11.01.2017.
 */
class ComponentUtil {
    @Nullable
    static NewExpression configForNewExpression(PsiElement psiElement) {
        if (psiElement instanceof NewExpression) {
            return (NewExpression) psiElement;
        }

        PsiElement parent = psiElement.getParent();
        if (parent != null) {
            return configForNewExpression(parent);
        }

        return null;
    }

    static int paramIndexForElement(PsiElement psiElement) {
        PsiElement parent = psiElement.getParent();
        if (parent == null) {
            return -1;
        }

        if (parent instanceof ParameterList) {
            return ArrayUtil.indexOf(((ParameterList) parent).getParameters(), psiElement);
        }

        return paramIndexForElement(parent);
    }


    @Nullable
    static PhpClass getPhpClass(PhpPsiElement phpPsiElement) {
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

            phpPsiElement = (PhpPsiElement) phpPsiElement.getParent();
        }

        return null;
    }

    static Collection<Field> getClassFields(PhpClass phpClass) {
        final HashSet<Field> result = new HashSet<>();

        final Collection<Field> fields = phpClass.getFields();
        final Collection<Method> methods = phpClass.getMethods();
        for (Field field : fields) {
            if (field.isConstant()) {
                continue;
            }

            final PhpModifier modifier = field.getModifier();
            if (!modifier.isPublic() || modifier.isStatic()) {
                continue;
            }

            if (field instanceof PhpDocProperty) {
                final String setter = "set" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
                Boolean setterExist = false;
                for (Method method : methods) {
                    if (method.getName().equals(setter)) {
                        setterExist = true;
                        break;
                    }
                }
                if (!setterExist) {
                    String getter = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
                    Boolean getterExist = false;
                    for (Method method : methods) {
                        if (method.getName().equals(getter)) {
                            getterExist = true;
                            break;
                        }
                    }
                    if (getterExist) {
                        continue;
                    }
                }
            }

            result.add(field);
        }

        return result;
    }
}
