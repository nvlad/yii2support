package com.nvlad.yii2support.validation;

import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.objectfactory.ObjectFactoryUtils;
import com.nvlad.yii2support.validation.entities.Validator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

class ValidationUtil {
    @NotNull
    static List<Validator> getAllValidators(@NotNull PhpClass phpClass) {
        final PhpIndex phpIndex = PhpIndex.getInstance(phpClass.getProject());
        final List<Validator> validators = getDefaultValidators(phpIndex);
        validators.addAll(getCustomValidators(phpIndex, validators));
        validators.addAll(getMethodValidators(phpClass));

        return validators;
    }

    @NotNull
    private static List<Validator> getDefaultValidators(PhpClass phpClass) {
        return getDefaultValidators(PhpIndex.getInstance(phpClass.getProject()));
    }

    @NotNull
    private static List<Validator> getDefaultValidators(PhpIndex phpIndex) {
        final List<Validator> validators = new ArrayList<>();
        final Collection<PhpClass> classes = phpIndex.getClassesByFQN("\\yii\\validators\\Validator");
        for (PhpClass validatorClass : classes) {
            Field builtInValidators = validatorClass.findOwnFieldByName("builtInValidators", false);
            if (builtInValidators == null || !(builtInValidators.getDefaultValue() instanceof ArrayCreationExpression)) {
                continue;
            }

            ArrayCreationExpression fieldArray = (ArrayCreationExpression) builtInValidators.getDefaultValue();
            if (fieldArray == null) {
                continue;
            }

            Iterable<ArrayHashElement> hashElements = fieldArray.getHashElements();
            for (ArrayHashElement elem : hashElements) {
                if (elem.getKey() == null || elem.getValue() == null) {
                    continue;
                }

                if (elem.getValue() instanceof ArrayCreationExpression) {
                    PhpClass phpClass = ObjectFactoryUtils.findClassByArray((ArrayCreationExpression) elem.getValue());
                    validators.add(new Validator(ClassUtils.removeQuotes(elem.getKey().getText()), phpClass));
                } else {
                    String className = ClassUtils.removeQuotes(elem.getValue().getText());
                    Iterator<PhpClass> classIterator = phpIndex.getClassesByFQN(className).iterator();
                    if (classIterator.hasNext()) {
                        PhpClass phpClass = classIterator.next();
                        String validatorName = ClassUtils.removeQuotes(elem.getKey().getText());
                        validators.add(new Validator(validatorName, phpClass));
                    }
                }
            }
        }

        return validators;
    }

    @Nullable
    static Validator getDefaultValidator(PhpClass phpClass, String alias) {
        if (alias == null) {
            return null;
        }

        List<Validator> validators = ValidationUtil.getDefaultValidators(phpClass);
        for (Validator validator : validators) {
            if (alias.equals(validator.alias)) {
                return validator;
            }
        }

        return null;
    }

    @NotNull
    private static List<Validator> getCustomValidators(PhpIndex phpIndex, Collection<Validator> exclude) {
        final Collection<PhpClass> validatorClasses = phpIndex.getAllSubclasses("yii\\validators\\Validator");
        final List<Validator> validators = new ArrayList<>();
        final Set<PhpNamedElement> excludeElements = new HashSet<>(exclude.size());
        for (Validator validator : exclude) {
            excludeElements.add(validator.validator);
        }

        for (PhpClass validatorClass : validatorClasses) {
            if (!excludeElements.contains(validatorClass)) {
                validators.add(new Validator(validatorClass));
            }
        }

        return validators;
    }

    @NotNull
    private static List<Validator> getMethodValidators(PhpClass phpClass) {
        final int minMethodNameLength = 8;
        final List<Validator> validators = new ArrayList<>();
        for (Method method : phpClass.getMethods()) {
            final String methodName = method.getName();
            if (methodName.length() > minMethodNameLength && methodName.startsWith("validate")) {
                validators.add(new Validator(methodName, method));
            }
        }

        return validators;
    }
}
