package com.yii2support.components;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
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

        NewExpression newExpression = ComponentUtil.configForNewExpression(element);
        if (newExpression != null) {
            PhpClass phpClass = ComponentUtil.getPhpClass(newExpression);
            if (phpClass != null) {
                Method constructor = phpClass.getConstructor();
                ParameterList parameterList = newExpression.getParameterList();
                if (constructor != null && parameterList != null) {
                    int paramIndex = ComponentUtil.paramIndexForElement(element);

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
}
