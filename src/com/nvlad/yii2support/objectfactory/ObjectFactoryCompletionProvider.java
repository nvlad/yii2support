package com.nvlad.yii2support.objectfactory;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by oleg on 16.02.2017.
 */
public class ObjectFactoryCompletionProvider extends com.intellij.codeInsight.completion.CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {

        PsiElement arrayValue = (PsiElement) completionParameters.getPosition().getParent().getParent();
        ArrayCreationExpression arrayCreation = (ArrayCreationExpression) arrayValue.getParent();
        PhpClass phpClass = ObjectFactoryUtil.findClassByArray(arrayCreation);
        if (phpClass != null) {
            for (Field field : ObjectFactoryUtil.getClassFields(phpClass)) {
                completionResultSet.addElement(new ObjectFactoryFieldLookupElement(null, field));
            }

        }

        /*
        NewExpression newExpression = ObjectFactoryUtil.configForNewExpression(element);
        if (newExpression != null) {
            PhpClass phpClass = ObjectFactoryUtil.getPhpClass(newExpression);
            if (phpClass != null) {
                Method constructor = phpClass.getConstructor();
                ParameterList parameterList = newExpression.getParameterList();
                if (constructor != null && parameterList != null) {
                    int paramIndex = ObjectFactoryUtil.paramIndexForElement(element);

                    if (paramIndex != -1) {
                        Parameter[] parameters = constructor.getParameters();

                        if (paramIndex < parameters.length && parameters[paramIndex].getName().equals("config")) {
                            for (Field field : ObjectFactoryUtil.getClassFields(phpClass)) {
                                completionResultSet.addElement(new ObjectFactoryFieldLookupElement(element, field));
                            }
                        }
                    }
                }
            }
        }
        */
    }
}
