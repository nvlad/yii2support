package com.nvlad.yii2support.objectfactory;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Created by oleg on 14.03.2017.
 */
public class ObjectFactoryUtils {
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
                    if (pList.length == 2) { // \Yii::createObject takes 2 paramters
                        phpClass = ClassUtils.getPhpClassUniversal(method.getProject(), (PhpPsiElement) pList[0]);
                    }
                }
            }
        }
        return phpClass;
    }

    static PhpClass getPhpClassInConfig(PsiDirectory dir, ArrayCreationExpression arrayCreation) {
        PhpClass phpClass = null;
        if (dir != null && dir.getName().equals("config")) {
            PsiElement parent = arrayCreation.getParent().getParent();
            if (parent instanceof ArrayHashElement) {
                ArrayHashElement hash = (ArrayHashElement) parent;
                PsiElement element = hash.getKey();
                if (element instanceof StringLiteralExpression) {
                    StringLiteralExpression literal = (StringLiteralExpression) element;
                    String key = literal.getContents();
                    phpClass = ClassUtils.getStandardPhpClass(PhpIndex.getInstance(literal.getProject()), key);
                }
            }
        }
        return phpClass;
    }

    static PhpClass getPhpClassInWidget(ArrayCreationExpression arrayCreation) {
        PsiElement parent = arrayCreation.getParent().getParent();
        if (parent != null && parent instanceof MethodReference) {
            MethodReference method = (MethodReference) parent;
            if (method.getName() != null && (method.getName().equals("widget") || method.getName().equals("begin"))) {
                PhpExpression methodClass = method.getClassReference();
                PhpClass callingClass = (PhpClass) ((ClassReference) methodClass).resolve();
                PhpClass superClass = ClassUtils.getClass(PhpIndex.getInstance(methodClass.getProject()), "\\yii\\base\\Widget");
                if (ClassUtils.isClassInherits(callingClass, superClass))
                    return callingClass;

            }
        }
        return null;
    }

    static PhpClass getPhpClassInGridColumns(ArrayCreationExpression arrayCreation) {
        PsiElement parent = arrayCreation.getParent().getParent();
        if (parent != null && parent instanceof ArrayCreationExpression) {
            PsiElement possibleHashElement = arrayCreation.getParent().getParent().getParent().getParent();

            if (possibleHashElement instanceof ArrayHashElement &&
                    ((ArrayHashElement) possibleHashElement).getKey().getText() != null &&
                    ((ArrayHashElement) possibleHashElement).getKey().getText().replace("\"", "").replace("\'", "").equals("columns")) {
                PsiElement methodRef = possibleHashElement.getParent().getParent().getParent();
                if (methodRef instanceof MethodReference) {
                    MethodReference method = (MethodReference) methodRef;
                    if (method.getClassReference() != null) {
                        PhpExpression methodClass = method.getClassReference();
                        PhpClass callingClass = (PhpClass) ((ClassReference) methodClass).resolve();
                        if (callingClass.getFQN().equals("\\yii\\grid\\GridView")) {
                            return ClassUtils.getClass(PhpIndex.getInstance(methodClass.getProject()), "\\yii\\grid\\DataColumn");
                        }

                    }
                }

            }

        }
        return null;
    }

    static PhpClass findClassByArrayCreation(ArrayCreationExpression arrayCreation, PsiDirectory dir) {
        PhpClass phpClass;
        phpClass = findClassByArray(arrayCreation);
        if (phpClass == null) {
            phpClass = getPhpClassByYiiCreateObject(arrayCreation);
        }
        if (phpClass == null) {

            phpClass = getPhpClassInConfig(dir, arrayCreation);
        }

        if (phpClass == null) {
            phpClass = getPhpClassInWidget(arrayCreation);
        }
        if (phpClass == null) {
            phpClass = getPhpClassInGridColumns(arrayCreation);
        }
        return phpClass;
    }
}
