package com.yii2support.components;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 11.01.2017.
 */
public class ComponentConfigCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        PhpExpression element = (PhpExpression) completionParameters.getPosition().getParent();
        PhpExpression parameter = (PhpExpression) element.getParent().getParent();
        ParameterList parameterList = (ParameterList) parameter.getParent();
        PhpClass phpClass = ComponentUtil.getPhpClass(parameterList);
        if (phpClass != null) {
            Method constructor = phpClass.getConstructor();
            if (constructor != null) {
                int paramIndex = ArrayUtil.indexOf(parameterList.getParameters(), parameter);

                if (paramIndex != -1) {
                    Parameter[] parameters = constructor.getParameters();

                    if (paramIndex < parameters.length && parameters[paramIndex].getName().equals("config")) {
                        for (Field field : ComponentUtil.getClassFields(phpClass)) {
                            completionResultSet.addElement(new ComponentFieldLookupElement(element, field));
                        }
                    }
                }
            }
        }
    }
}
