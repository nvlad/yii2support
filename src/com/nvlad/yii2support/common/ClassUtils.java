package com.nvlad.yii2support.common;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocProperty;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by NVlad on 11.01.2017.
 */
public class ClassUtils {


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
            StringLiteralExpression str = (StringLiteralExpression) value;
            PhpIndex phpIndex = PhpIndex.getInstance(project);
            PhpClass classRef = getClass(phpIndex, str.getContents());
            return classRef;
        }
        return null;
    }

    @Nullable
    public static PhpClass getClass(PhpIndex phpIndex, String className) {
        Collection<PhpClass> classes = phpIndex.getAnyByFQN(className);
        return classes.isEmpty() ? null : classes.iterator().next();
    }

    @Nullable
    public static PhpClass getPhpClass(PhpPsiElement phpPsiElement) {
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

    @Nullable
    public static PhpClass getPhpClassByCallChain(MethodReference methodRef) {

        while (methodRef != null) {
            PhpExpression expr = methodRef.getClassReference();
            if (expr instanceof ClassReference) {
                return (PhpClass) ((ClassReference)expr).resolve();
            }
            else if (expr instanceof MethodReference) {
                methodRef = (MethodReference) expr;
            } else {
                return null;
            }
        }

        return null;
    }

    public static boolean isClassInheritsOrEqual(PhpClass classObject, PhpClass superClass) {
        if (classObject == null || superClass == null)
            return false;
        if (classObject != null) {
            if (classObject.isEquivalentTo(superClass))
                return true;
            else
                return isClassInheritsOrEqual(classObject.getSuperClass(), superClass);
        }
        return false;
    }

    public static String getAsPropertyName(Method method) {
        String methodName = method.getName();
        String propertyName = methodName.substring(3);
        propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
        return propertyName;
    }

    public static Collection<Method> getClassSetMethods(PhpClass phpClass) {
        final HashSet<Method> result = new HashSet<>();
        final Collection<Method> methods = phpClass.getMethods();


        for (Method method : methods) {
            String methodName = method.getName();
            int pCount = method.getParameters().length;
            if (methodName.length() > 3 && methodName.startsWith("set") && pCount == 1 &&
                    Character.isUpperCase(methodName.charAt(3))) {
                result.add(method);
            }
        }

        return result;

    }

    @Nullable
    public static MethodReference getMethodRef(PsiElement el, int recursionLimit) {
        if (el == null)
            return null;
        else if (el.getParent() instanceof MethodReference)
            return (MethodReference)el.getParent();
        else if (recursionLimit <= 0)
            return null;
        else
            return getMethodRef(el.getParent(), recursionLimit - 1);
    }

    public static String removeQuotes(String str) {
        return str.replace("\"", "").replace("\'", "");
    }

    public static PhpClassMember findField(PhpClass phpClass, String fieldName) {

        if (phpClass == null || fieldName == null)
            return null;
        fieldName = ClassUtils.removeQuotes(fieldName);


        final Collection<Field> fields = phpClass.getFields();
        final Collection<Method> methods = phpClass.getMethods();

        if (fields != null) {
            for (Field field : fields) {
                if (!field.getName().equals(fieldName))
                    continue;

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

                return field;
            }
        }


        if (methods != null) {
            for (Method method : methods) {
                String methodName = method.getName();
                int pCount = method.getParameters().length;
                if (methodName.length() > 3 && methodName.startsWith("set") && pCount == 1 &&
                        Character.isUpperCase(methodName.charAt(3))) {
                    String propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                    if (propertyName.equals(fieldName))
                        return method;

                }
            }
        }


        return null;
    }

    public static int paramIndexForElement(PsiElement psiElement) {
        PsiElement parent = psiElement.getParent();
        if (parent == null) {
            return -1;
        }

        if (parent instanceof ParameterList) {
            return ArrayUtil.indexOf(((ParameterList) parent).getParameters(), psiElement);
        }

        return paramIndexForElement(parent);
    }


    public static Collection<Field> getClassFields(PhpClass phpClass) {
        if (phpClass == null)
            return null;
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
