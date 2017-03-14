package com.nvlad.yii2support.objectfactory;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocProperty;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by NVlad on 11.01.2017.
 */
class ObjectFactoryUtil {


    @Nullable
    static public PhpClass findClassByArray(@NotNull ArrayCreationExpression arrayCreationExpression) {
        HashMap<String, String> keys = new HashMap<>();

        for(ArrayHashElement arrayHashElement: arrayCreationExpression.getHashElements()) {
            PhpPsiElement child = arrayHashElement.getKey();
            if(child != null && ((child instanceof StringLiteralExpression))) {
                String key;
                if(child instanceof StringLiteralExpression) {
                    key = ((StringLiteralExpression) child).getContents();
                } else {
                    key = child.getText();
                }

                Project project = child.getProject();

                if (key.equals("class")) {
                    String className = "";
                    PhpPsiElement value = arrayHashElement.getValue();
                    PhpClass methodRef = getPhpClassUniversal(project, value);
                    if (methodRef != null) return methodRef;
                }
            }
        }

       return null;
    }

    @Nullable
    public static PhpClass getPhpClassUniversal(Project project, PhpPsiElement value) {
        if (value instanceof MethodReference && value.getName().equals("className")) {
            MethodReference methodRef = (MethodReference) value;
            return getPhpClass(methodRef.getClassReference());

        }
        if (value instanceof ClassConstantReference) {
            ClassConstantReference classRef = (ClassConstantReference) value;
            return getPhpClass(classRef);
        }
        if (value instanceof StringLiteralExpression) {
            StringLiteralExpression str = (StringLiteralExpression)value;
            PhpIndex phpIndex = PhpIndex.getInstance(project);
            PhpClass classRef = getClass(phpIndex, str.getContents());
            return classRef;
        }
        return null;
    }

    @Nullable
    static public PhpClass getClass(PhpIndex phpIndex, String className) {
        Collection<PhpClass> classes = phpIndex.getClassesByFQN(className);
        return classes.isEmpty() ? null : classes.iterator().next();
    }

     @Nullable
    static PhpClass getPhpClass(PhpPsiElement phpPsiElement) {
        while (phpPsiElement != null) {
            if (phpPsiElement instanceof ClassConstantReference) {
                phpPsiElement = ((ClassConstantReference) phpPsiElement).getClassReference();
            }
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

    static boolean isClassInherits(PhpClass classObject, PhpClass superClass) {
        if (classObject == null || superClass == null)
            return false;
        if ( classObject.getSuperClass() != null) {
             if (classObject.getSuperClass().isEquivalentTo(superClass))
                 return true;
             else
                 return isClassInherits(classObject.getSuperClass(), superClass);
        }
        return false;
    }

    static Collection<Method> getClassSetMethods(PhpClass phpClass) {
        final HashSet<Method> result = new HashSet<>();
        final Collection<Method> methods = phpClass.getMethods();


        for (Method method : methods) {
            String methodName = method.getName();
            int pCount =  method.getParameters().length;
            if (methodName.length() > 3 && methodName.startsWith("set")  && pCount == 1 &&
                    Character.isUpperCase(methodName.charAt(3))) {
                result.add(method);
            }
        }

        return result;

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

    static PhpClass getStandardPhpClass(PhpIndex phpIndex, String shortName) {
        switch (shortName){
            // web/Application
            case "request":  return getClass(phpIndex, "\\yii\\web\\Request");
            case "response":  return getClass(phpIndex, "\\yii\\web\\Response");
            case "session":  return getClass(phpIndex, "\\yii\\web\\Session");
            case "user":  return getClass(phpIndex, "\\yii\\web\\User");
            case "errorHandler":  return getClass(phpIndex, "\\yii\\web\\ErrorHandler");
            // base/Application
            case "log":  return getClass(phpIndex, "\\yii\\log\\Dispatcher");
            case "view":  return getClass(phpIndex, "\\yii\\web\\View");
            case "formatter":  return getClass(phpIndex, "\\yii\\i18n\\I18N");
            case "mailer":  return getClass(phpIndex, "\\yii\\swiftmailer\\Mailer");
            case "urlManager":  return getClass(phpIndex, "\\yii\\web\\UrlManager");
            case "assetManager":  return getClass(phpIndex, "\\yii\\web\\AssetManager");
            case "security":  return getClass(phpIndex, "\\yii\\base\\Security");
        }
        return null;
    }
}
