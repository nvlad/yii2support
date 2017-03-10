package com.nvlad.yii2support.objectfactory;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpReadWriteAccessDetector;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;

import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

/**
 * Created by oleg on 16.02.2017.
 */
public class ObjectFactoryCompletionProvider extends com.intellij.codeInsight.completion.CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {

        Hashtable<String, Object> uniqTracker = new Hashtable<>();

        PhpPsiElement arrayValue = (PhpPsiElement) completionParameters.getPosition().getParent().getParent();

        ArrayCreationExpression arrayCreation = (ArrayCreationExpression) arrayValue.getParent();
        PhpClass phpClass = ObjectFactoryUtil.findClassByArray(arrayCreation);
        if (phpClass == null) {
            PsiElement parent = arrayCreation.getParent().getParent();
            if (parent != null && parent instanceof MethodReference) {
                MethodReference method = (MethodReference) parent;
                if (method.getName() != null && method.getName().equals("createObject")) {
                    PhpExpression methodClass = method.getClassReference();
                    if (methodClass != null && methodClass.getName() != null && methodClass.getName().equals("Yii")) {
                        PsiElement[] pList = method.getParameters();
                        if (pList.length == 2) { // \Yii::createObject takes 2 paramters
                           phpClass =  ObjectFactoryUtil.getPhpClassUniversal(method.getProject(), (PhpPsiElement) pList[0]);
                        }
                    }
                }
            }
        }
        if (phpClass != null) {
            for (Field field : ObjectFactoryUtil.getClassFields(phpClass)) {
                uniqTracker.put(field.getName(), field);
                completionResultSet.addElement(new ObjectFactoryFieldLookupElement(arrayCreation, field));
            }


            for (Method method : ObjectFactoryUtil.getClassSetMethods(phpClass)) {
                ObjectFactoryMethodLookupElement lookupElem = new ObjectFactoryMethodLookupElement(arrayCreation, method);
                if (uniqTracker.get(lookupElem.getAsPropertyName()) == null) {
                    completionResultSet.addElement(lookupElem);
                }
            }

        }

    }
}
