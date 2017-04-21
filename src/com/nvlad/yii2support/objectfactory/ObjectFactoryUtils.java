package com.nvlad.yii2support.objectfactory;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by oleg on 14.03.2017.
 */
public class ObjectFactoryUtils {
    @Nullable
    static public PhpClass findClassByArray(@NotNull ArrayCreationExpression arrayCreationExpression) {
        HashMap<String, String> keys = new HashMap<>();

        for (ArrayHashElement arrayHashElement : arrayCreationExpression.getHashElements()) {
            PhpPsiElement child = arrayHashElement.getKey();
            if (child != null && ((child instanceof StringLiteralExpression))) {
                String key;
                if (child instanceof StringLiteralExpression) {
                    key = ((StringLiteralExpression) child).getContents();
                } else {
                    key = child.getText();
                }

                Project project = child.getProject();

                if (key.equals("class")) {
                    String className = "";
                    PhpPsiElement value = arrayHashElement.getValue();
                    PhpClass methodRef = ClassUtils.getPhpClassUniversal(project, value);
                    if (methodRef != null) return methodRef;
                }
            }
        }

        return null;
    }

    @Nullable
    static PhpClass getPhpClassByYiiCreateObject(ArrayCreationExpression arrayCreation) {
        PhpClass phpClass = null;
        PsiElement parent = arrayCreation.getParent().getParent();
        if (parent != null && parent instanceof MethodReference) {
            MethodReference method = (MethodReference) parent;
            if (method.getName() != null && method.getName().equals("createObject")) {
                PhpExpression methodClass = method.getClassReference();
                if (methodClass != null && methodClass.getName() != null && methodClass.getName().equals("Yii")) {
                    PsiElement[] pList = method.getParameters();
                    if (pList.length == 2 && ClassUtils.paramIndexForElement(arrayCreation) == 1) { // \Yii::createObject takes 2 paramters
                        phpClass = ClassUtils.getPhpClassUniversal(method.getProject(), (PhpPsiElement) pList[0]);
                    }
                }
            }
        }
        return phpClass;
    }

    static PhpClass getPhpClassInConfig(PsiDirectory dir, ArrayCreationExpression arrayCreation) {
        PhpClass phpClass = null;
        if (dir != null && (dir.getName().equals("config") || dir.getName().equals("src") /* for tests */)) {
            PsiElement parent = arrayCreation.getParent().getParent();
            if (parent instanceof ArrayHashElement) {
                ArrayHashElement hash = (ArrayHashElement) parent;
                PsiElement element = hash.getKey();
                if (element instanceof StringLiteralExpression) {
                    StringLiteralExpression literal = (StringLiteralExpression) element;
                    String key = literal.getContents();
                    phpClass = getStandardPhpClass(PhpIndex.getInstance(literal.getProject()), key);
                }
            }
        }
        return phpClass;
    }

    static PhpClass getPhpClassInWidget(ArrayCreationExpression arrayCreation) {
        PsiElement parent = arrayCreation.getParent().getParent();
        if (parent != null && parent instanceof MethodReference) {
            MethodReference methodRef = (MethodReference) parent;
            if (methodRef.getName() != null && (methodRef.getName().equals("widget") || methodRef.getName().equals("begin"))) {
                Method method = (Method) methodRef.resolve();

                PhpExpression ref = methodRef.getClassReference();
                if (ref != null && ref instanceof ClassReference && ClassUtils.paramIndexForElement(arrayCreation) == 0) {
                    PhpClass callingClass = (PhpClass) ((ClassReference) ref).resolve();
                    PhpClass superClass = ClassUtils.getClass(PhpIndex.getInstance(methodRef.getProject()), "\\yii\\base\\Widget");
                    if (ClassUtils.isClassInheritsOrEqual(callingClass, superClass))
                        return callingClass;
                } else if (method != null && ref != null && ref instanceof MethodReference && ClassUtils.paramIndexForElement(arrayCreation) == 1) {
                    // This code process
                    // $form->field($model, 'username')->widget(\Class::className())
                    PhpClass callingClass = method.getContainingClass();
                    PhpClass superClass = ClassUtils.getClass(PhpIndex.getInstance(methodRef.getProject()), "yii\\widgets\\ActiveField");
                    if (ClassUtils.isClassInheritsOrEqual(callingClass, superClass)
                            && method.getParameters().length == 2 &&
                            method.getParameters()[0].getName().equals("class")) {
                        PhpPsiElement element = (PhpPsiElement) methodRef.getParameters()[0];
                        PhpClass widgetClass = ClassUtils.getPhpClassUniversal(methodRef.getProject(), element);
                        if (widgetClass != null)
                            return widgetClass;

                    }

                }

            }
        }
        return null;
    }

    static PhpClass getPhpClassInGridColumns(ArrayCreationExpression arrayCreation) {
        PsiElement parent = arrayCreation.getParent().getParent();
        if (parent != null && parent instanceof ArrayCreationExpression) {
            PsiElement possibleHashElement = arrayCreation.getParent().getParent().getParent().getParent();

            if (!(possibleHashElement instanceof ArrayHashElement)) {
                return null;
            }

            PsiElement key = ((ArrayHashElement) possibleHashElement).getKey();
            if (key != null &
                    key.getText() != null &&
                    key.getText().replace("\"", "").replace("\'", "").equals("columns")) {
                PsiElement methodRef = possibleHashElement.getParent().getParent().getParent();
                if (methodRef instanceof MethodReference) {
                    MethodReference method = (MethodReference) methodRef;
                    if (method.getClassReference() != null) {
                        PhpExpression methodClass = method.getClassReference();
                        PhpClass callingClass = (PhpClass) ((ClassReference) methodClass).resolve();
                        if (callingClass != null && callingClass.getFQN().equals("\\yii\\grid\\GridView")) {
                            return ClassUtils.getClass(PhpIndex.getInstance(methodClass.getProject()), "\\yii\\grid\\DataColumn");
                        }

                    }
                }

            }

        }
        return null;
    }

    @Nullable
    static PhpClass findClassByArrayCreation(ArrayCreationExpression arrayCreation, PsiDirectory dir) {
        PhpClass phpClass;
        phpClass = findClassByArray(arrayCreation);
        if (phpClass == null) {
            phpClass = getClassByInstatiation(arrayCreation);
        }
        if (phpClass == null) {
            phpClass = getPhpClassByYiiCreateObject(arrayCreation);
        }
        if (phpClass == null) {
            phpClass = getPhpClassInWidget(arrayCreation);
        }
        if (phpClass == null) {
            phpClass = getPhpClassInGridColumns(arrayCreation);
        }
        if (phpClass == null && arrayCreation.getParent().getParent() instanceof ArrayHashElement) {
            phpClass = getPhpClassByHash((ArrayHashElement) arrayCreation.getParent().getParent(), dir);
        }
        if (phpClass == null) {
            phpClass = getPhpClassInConfig(dir, arrayCreation);
        }
        return phpClass;
    }

    private static PhpClass getPhpClassByHash(ArrayHashElement hashElement, PsiDirectory dir) {
        if (hashElement.getParent() instanceof ArrayCreationExpression) {
            PhpClass phpClass = findClassByArrayCreation((ArrayCreationExpression) hashElement.getParent(), dir);
            if (phpClass == null)
                return null;
            String fieldName = hashElement.getKey() != null ? hashElement.getKey().getText() : null;
            if (fieldName == null)
                return null;
            PhpClassMember field = ClassUtils.findWritableField(phpClass, fieldName);
            if (field == null)
                return null;
            Set<String> types = field.getType().getTypes();
            PhpClass resultClass = null;
            for (String type : types) {
                resultClass = ClassUtils.getClass(PhpIndex.getInstance(field.getProject()), type);
                if (resultClass != null && ! resultClass.getName().equals("Closure") ) {
                    return resultClass;
                }
            }
        }
        return null;
    }

    static PhpClass getClassByInstatiation(PhpExpression element) {

        PsiElement newElement = element.getParent().getParent();
        if (newElement != null && newElement instanceof NewExpression) {
            ClassReference ref = ((NewExpression) newElement).getClassReference();
            if (ref == null)
                return null;

            PsiElement possiblePhpClass = ref.resolve();
            if (!(possiblePhpClass instanceof PhpClass))
                return null;

            PhpClass phpClass = (PhpClass) possiblePhpClass;
            if (phpClass != null) {

                Method constructor = phpClass.getConstructor();

                PhpClass yiiObjectClass = ClassUtils.getClass(PhpIndex.getInstance(element.getProject()), "\\yii\\base\\Object");
                if (!ClassUtils.isClassInheritsOrEqual(phpClass, yiiObjectClass))
                    return null;

                Parameter[] parameterList = constructor.getParameters();
                if (parameterList.length > 0 && parameterList[0].getName().equals("config") && ClassUtils.paramIndexForElement(element) == 0)
                    return phpClass;

            }
        }
        return null;
    }


    static PhpClass getStandardPhpClass(PhpIndex phpIndex, String shortName) {
        switch (shortName){
            // web/Application
            case "request":  return ClassUtils.getClass(phpIndex, "\\yii\\web\\Request");
            case "response":  return ClassUtils.getClass(phpIndex, "\\yii\\web\\Response");
            case "session":  return ClassUtils.getClass(phpIndex, "\\yii\\web\\Session");
            case "user":  return ClassUtils.getClass(phpIndex, "\\yii\\web\\User");
            case "errorHandler":  return ClassUtils.getClass(phpIndex, "\\yii\\web\\ErrorHandler");
            // base/Application
            case "log":  return ClassUtils.getClass(phpIndex, "\\yii\\log\\Dispatcher");
            case "view":  return ClassUtils.getClass(phpIndex, "\\yii\\web\\View");
            case "formatter":  return ClassUtils.getClass(phpIndex, "yii\\i18n\\Formatter");
            case "i18n":  return ClassUtils.getClass(phpIndex, "yii\\i18n\\I18N");
            case "mailer":  return ClassUtils.getClass(phpIndex, "\\yii\\swiftmailer\\Mailer");
            case "urlManager":  return ClassUtils.getClass(phpIndex, "\\yii\\web\\UrlManager");
            case "assetManager":  return ClassUtils.getClass(phpIndex, "\\yii\\web\\AssetManager");
            case "security":  return ClassUtils.getClass(phpIndex, "\\yii\\base\\Security");
            // custom
            case "db": return ClassUtils.getClass(phpIndex, "\\yii\\db\\Connection");
        }
        return null;
    }
}
