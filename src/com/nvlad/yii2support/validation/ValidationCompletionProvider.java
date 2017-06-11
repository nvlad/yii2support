package com.nvlad.yii2support.validation;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import com.jetbrains.php.lang.psi.elements.impl.PhpReturnImpl;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.DatabaseUtils;
import com.nvlad.yii2support.common.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Created by oleg on 20.04.2017.
 */
public class ValidationCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        PsiElement position = completionParameters.getPosition();
        PhpClass phpClass = getClassIfInRulesMethod(position);
        if (phpClass != null) {
            RulePositionEnum getPosition = getPosition(position);
            if (getPosition.equals(RulePositionEnum.FIELD))
            {
                ArrayList<LookupElementBuilder> items = DatabaseUtils.getLookupItemsByProperties(phpClass, (PhpExpression) completionParameters.getPosition().getParent());
                completionResultSet.addAllElements(items);
            }
        }
    }

    @Nullable
    private PhpClass getClassIfInRulesMethod(PsiElement position) {
        PsiElement elem = position.getParent();
        Method currentMethod = null;
        PhpClass phpClass = null;
        while ( true ) {
            if (elem instanceof Method)
                currentMethod = (Method) elem;
            else if (elem instanceof PhpClass) {
                phpClass = (PhpClass)elem;
                break;
            } else if  (elem instanceof PhpFile)
                break;
            else if (elem == null) {
                break;
            }
            elem = elem.getParent();
        }
        if (currentMethod != null && phpClass != null) {
            if (ClassUtils.isClassInherit(phpClass, "\\yii\\base\\Model", PhpIndex.getInstance(position.getProject())) &&
                    currentMethod.getName().equals("rules")) {
                return phpClass;
            }
            else
                return null;

        } else {
            return null;
        }
    }

    private RulePositionEnum getPosition(PsiElement position) {

        PsiElement validationParameter = null;

        PsiElement possibleReturn = PsiUtil.getSuperParent(position, 8);
        if (possibleReturn != null && possibleReturn instanceof PhpReturnImpl)
            validationParameter = position.getParent().getParent();
        else {
            possibleReturn = PsiUtil.getSuperParent(position, 6);
            if (possibleReturn != null && possibleReturn instanceof PhpReturnImpl) {
                validationParameter = position.getParent().getParent();
            } else {
                return RulePositionEnum.UNKNOWN;
            }
        }

        if (validationParameter.toString().equals("Array value") && validationParameter.getParent() instanceof ArrayCreationExpression) {
            int index = PsiUtil.getValueIndexInArray(validationParameter, (ArrayCreationExpression) validationParameter.getParent());
            if (index == 0)
                return RulePositionEnum.FIELD;
            else if (index == 1)
                return RulePositionEnum.TYPE;
            else if (index > 1)
                return RulePositionEnum.OPTIONS;
            else
                return RulePositionEnum.UNKNOWN;
        }
        return  RulePositionEnum.UNKNOWN;
    }
}
