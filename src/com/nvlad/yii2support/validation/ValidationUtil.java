package com.nvlad.yii2support.validation;

import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.objectfactory.ObjectFactoryUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ValidationUtil {
    @NotNull
    public static Map<String, PhpNamedElement> getAllValidators(@NotNull PhpClass phpClass) {
        final PhpIndex phpIndex = PhpIndex.getInstance(phpClass.getProject());
        final Map<String, PhpNamedElement> validators = getDefaultValidators(phpIndex);
        validators.putAll(getCustomValidators(phpIndex, validators.values()));
        validators.putAll(getMethodValidators(phpClass));

        return validators;
    }

    public static Map<String, PhpNamedElement> getDefaultValidators(PhpClass phpClass) {
        return getDefaultValidators(PhpIndex.getInstance(phpClass.getProject()));
    }

    public static Map<String, PhpNamedElement> getDefaultValidators(PhpIndex phpIndex) {
        final Map<String, PhpNamedElement> validators = new HashMap<>();
        final Collection<PhpClass> classes = phpIndex.getClassesByFQN("\\yii\\validators\\Validator");
        for (PhpClass validatorClass : classes) {
            Field builtInValidatorsField = validatorClass.findOwnFieldByName("builtInValidators", false);
            if (!(builtInValidatorsField.getDefaultValue() instanceof ArrayCreationExpression)) {
                continue;
            }

            ArrayCreationExpression fieldArray = (ArrayCreationExpression) builtInValidatorsField.getDefaultValue();
            if (fieldArray == null) {
                continue;
            }

            Iterable<ArrayHashElement> hashElements = fieldArray.getHashElements();
            for (ArrayHashElement elem : hashElements) {
                if (elem.getValue() instanceof ArrayCreationExpression) {
                    PhpClass phpClass = ObjectFactoryUtils.findClassByArray((ArrayCreationExpression) elem.getValue());
                    validators.put(ClassUtils.removeQuotes(elem.getKey().getText()), phpClass);
                } else {
                    PhpClass phpClass = phpIndex.getClassesByFQN(ClassUtils.removeQuotes(elem.getValue().getText())).iterator().next();
                    String validatorName = ClassUtils.removeQuotes(elem.getKey().getText());
                    validators.put(validatorName, phpClass);
                }
            }
        }

        return validators;
    }

    public static Map<String, PhpNamedElement> getCustomValidators(PhpIndex phpIndex, Collection<PhpNamedElement> exclude) {
        final Collection<PhpClass> validatorClasses = phpIndex.getAllSubclasses("yii\\validators\\Validator");
        final Map<String, PhpNamedElement> validators = new HashMap<>();
        for (PhpClass validatorClass : validatorClasses) {
            if (!exclude.contains(validatorClass)) {
                validators.put(validatorClass.getName(), validatorClass);
            }
        }

        return validators;
    }

    public static HashMap<String, PhpNamedElement> getMethodValidators(PhpClass phpClass) {
        final int minMethodNameLength = 8;
        final HashMap<String, PhpNamedElement> validators = new HashMap<>();
        for (Method method : phpClass.getMethods()) {
            final String methodName = method.getName();
            if (methodName.length() > minMethodNameLength && methodName.startsWith("validate")) {
                validators.put(method.getName(), method);
            }
        }

        return validators;
    }
}
