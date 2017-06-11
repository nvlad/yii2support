package com.nvlad.yii2support.validation;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.PhpReturnImpl;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.DatabaseUtils;
import com.nvlad.yii2support.common.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by oleg on 20.04.2017.
 */
public class ValidationCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        PsiElement position = completionParameters.getPosition();
        PhpClass phpClass = getClassIfInRulesMethod(position);
        if (position.getParent() instanceof PhpExpression) {
            PhpExpression phpExpression = (PhpExpression) position.getParent();
            if (phpClass != null) {
                RulePositionEnum getPosition = getPosition(position);
                if (getPosition.equals(RulePositionEnum.FIELD)) {
                    Hashtable<String, Object> uniqTracker = new Hashtable<>();

                    ArrayList<LookupElementBuilder> items = DatabaseUtils.getLookupItemsByAnnotations(phpClass, (PhpExpression) completionParameters.getPosition().getParent());


                    for (Field field : ClassUtils.getWritableClassFields(phpClass)) {
                        uniqTracker.put(field.getName(), field);
                        LookupElementBuilder lookupBuilder = buildLookup(field, phpExpression, false);
                        completionResultSet.addElement(lookupBuilder);
                    }


                    completionResultSet.addAllElements(items);
                } else if (getPosition.equals(RulePositionEnum.TYPE)) {
                    HashMap<String, PhpPsiElement> validators = getValidators(phpClass);
                    for (Map.Entry<String, PhpPsiElement> entry: validators.entrySet()) {
                        if (entry.getValue() instanceof PhpClass)
                            completionResultSet.addElement(buildLookup((PhpClass)entry.getValue() , phpExpression));
                    }
                } else if (getPosition.equals(RulePositionEnum.OPTIONS)) {
                    ArrayCreationExpression arrayCreation = (ArrayCreationExpression)PsiUtil.getSuperParent(position, ArrayCreationExpression.class, 4);
                    if (arrayCreation != null) {
                        arrayCreation = arrayCreation;

                        if (arrayCreation.getChildren().length > 2) {
                            PsiElement elem = arrayCreation.getChildren()[1];
                            String value = elem.getText();
                            if (value != null) {
                                HashMap<String, PhpPsiElement> validators = getValidators(phpClass);
                                PsiElement validator = validators.get(ClassUtils.removeQuotes(value));
                                if (validator != null) {
                                    if (validator instanceof PhpClass) {
                                        for (Field field: ClassUtils.getClassFields((PhpClass)validator)) {
                                            completionResultSet.addElement(buildLookup(field , phpExpression, true));
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    private static HashMap<String, PhpPsiElement> getValidators(PhpClass phpClass) {
        Collection<PhpClass> validatorClasses = PhpIndex.getInstance(phpClass.getProject()).getAllSubclasses("yii\\validators\\Validator");
        HashMap<String, PhpPsiElement> validators = new HashMap<>();
        for (PhpClass validatorClass : validatorClasses) {
            validators.put(validatorClass.getName().replace("Validator", "").toLowerCase(), validatorClass);
        }
        return validators;

    }

    @NotNull
    private LookupElementBuilder buildLookup(PhpClassMember field, PhpExpression position, boolean autoValue) {
        String lookupString = field instanceof Method ? ClassUtils.getAsPropertyName((Method) field) : field.getName();
        LookupElementBuilder builder = LookupElementBuilder.create(field, lookupString).withIcon(field.getIcon());
        if (autoValue) {
            builder = builder.withInsertHandler((insertionContext, lookupElement) -> {

                Document document = insertionContext.getDocument();
                int insertPosition = insertionContext.getSelectionEndOffset();

                if (position.getParent().getParent() instanceof ArrayCreationExpression) {
                    document.insertString(insertPosition + 1, " => ");
                    insertPosition += 5;
                    insertionContext.getEditor().getCaretModel().getCurrentCaret().moveToOffset(insertPosition);
                }
            });
        }
        if (field instanceof Field) {
            builder = builder.withTypeText(field.getType().toString());
        }
        return builder;
    }

    @NotNull
    private LookupElementBuilder buildLookup(PhpClass phpClass, PhpExpression position) {
        String lookupString = phpClass.getName().replace("Validator", "").toLowerCase();
        LookupElementBuilder builder = LookupElementBuilder.create(phpClass, lookupString).withIcon(phpClass.getIcon())
                .withInsertHandler((insertionContext, lookupElement) -> {
                });

            builder = builder.withTypeText(phpClass.getFQN());

        return builder;
    }

    @Nullable
    private PhpClass getClassIfInRulesMethod(PsiElement position) {
        PsiElement elem = position.getParent();
        Method currentMethod = null;
        PhpClass phpClass = null;
        while (true) {
            if (elem instanceof Method)
                currentMethod = (Method) elem;
            else if (elem instanceof PhpClass) {
                phpClass = (PhpClass) elem;
                break;
            } else if (elem instanceof PhpFile)
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
            } else
                return null;

        } else {
            return null;
        }
    }

    private RulePositionEnum getPosition(PsiElement position) {

        PsiElement validationParameter = null;

        PsiElement possibleReturn = PsiUtil.getSuperParent(position, 6);
        if (possibleReturn != null && possibleReturn instanceof PhpReturnImpl)
            validationParameter = position.getParent().getParent();
        else {
            possibleReturn = PsiUtil.getSuperParent(position, 8);
            if (possibleReturn != null && possibleReturn instanceof PhpReturnImpl) {
                validationParameter = position.getParent().getParent().getParent().getParent();
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
        return RulePositionEnum.UNKNOWN;
    }
}
