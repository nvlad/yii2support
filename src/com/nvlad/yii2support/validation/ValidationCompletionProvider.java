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
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.DatabaseUtils;
import com.nvlad.yii2support.common.PsiUtil;
import com.nvlad.yii2support.validation.entities.Validator;
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
                    ArrayList<LookupElementBuilder> items = DatabaseUtils.getLookupItemsByAnnotations(phpClass, (PhpExpression) completionParameters.getPosition().getParent());
                    for (Field field : ClassUtils.getWritableClassFields(phpClass)) {
                        LookupElementBuilder lookupBuilder = buildLookup(field, phpExpression, false);
                        completionResultSet.addElement(lookupBuilder);
                    }

                    completionResultSet.addAllElements(items);
                } else if (getPosition.equals(RulePositionEnum.TYPE)) {
                    List<Validator> validators = ValidationUtil.getAllValidators(phpClass);
                    for (Validator validator : validators) {
                        if (validator.validator instanceof PhpClass) {
                            if (validator.alias == null) {
                                completionResultSet.addElement(buildLookup((PhpClass) validator.validator));
                            } else {
                                completionResultSet.addElement(buildLookup(validator.alias, (PhpClass) validator.validator));
                            }
                        } else if (validator.validator instanceof Method) {
                            completionResultSet.addElement(buildLookup((Method) validator.validator));
                        }
                    }
//                    for (Map.Entry<String, PhpNamedElement> entry : validators.entrySet()) {
//                        if (entry.getValue() instanceof PhpClass) {
//                            completionResultSet.addElement(buildLookup(entry.getKey(), (PhpClass) entry.getValue(), phpExpression));
//                        } else if (entry.getValue() instanceof Method) {
//                            completionResultSet.addElement(buildLookup((Method) entry.getValue(), phpExpression));
//                        }
//                    }
                } else if (getPosition.equals(RulePositionEnum.OPTIONS)) {
                    ArrayCreationExpression arrayCreation = (ArrayCreationExpression) PsiUtil.getSuperParent(position, ArrayCreationExpression.class, 4);
                    if (arrayCreation != null) {
                        if (arrayCreation.getChildren().length > 2) {
                            PsiElement elem = arrayCreation.getChildren()[1];
                            if (elem.getChildren().length == 0) {
                                return;
                            }

                            PsiElement validatorIdentifier = elem.getChildren()[0];
                            PhpClass validator = null;
                            if (validatorIdentifier instanceof StringLiteralExpression) {
                                String value = validatorIdentifier.getText();
                                if (value != null) {
                                    Validator validatorElement = ValidationUtil.getDefaultValidator(phpClass, ClassUtils.removeQuotes(value));
                                    if (validatorElement != null && validatorElement.validator instanceof PhpClass) {
                                        validator = (PhpClass) validatorElement.validator;
                                    }
                                }
                            }

                            if (validator == null && validatorIdentifier instanceof PhpPsiElement) {
                                validator = ClassUtils.getPhpClassUniversal(phpClass.getProject(), (PhpPsiElement) validatorIdentifier);
                            }

                            if (validator != null) {
                                for (Field field : ClassUtils.getClassFields(validator)) {
                                    completionResultSet.addElement(buildLookup(field, phpExpression, true));
                                }
                            }
                        }
                    }
                }
            }
        }
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
    private LookupElementBuilder buildLookup(Method method) {
        String lookupString = method.getName();
        LookupElementBuilder builder = LookupElementBuilder.create(method, lookupString).withIcon(method.getIcon())
                .withInsertHandler((insertionContext, lookupElement) -> {
                });

        return builder.withTypeText(method.getFQN());
    }

    @NotNull
    private LookupElementBuilder buildLookup(PhpClass phpClass) {
        String lookupString = phpClass.getFQN();
        lookupString = lookupString.replaceAll("^\\\\", "");
        LookupElementBuilder builder = LookupElementBuilder.create(phpClass, lookupString).withIcon(phpClass.getIcon())
                .withInsertHandler((insertionContext, lookupElement) -> {
                })
                .withTypeText(phpClass.getNamespaceName().replaceAll("\\\\$", ""), true)
                .withPresentableText(phpClass.getName());

        return builder;
    }

    @NotNull
    private LookupElementBuilder buildLookup(String lookupString, PhpClass phpClass) {
        LookupElementBuilder builder = LookupElementBuilder
                .create(phpClass, lookupString)
                .withIcon(phpClass.getIcon())
                .withInsertHandler((insertionContext, lookupElement) -> {});

        builder = builder.withTypeText(phpClass.getFQN(), true);

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
        PsiElement validationParameter;
        ArrayCreationExpression arrayCreationExpression;
        List<ArrayCreationExpression> arrayCreationExpressionList = new ArrayList<>();
        PsiElement currentElement = position.getParent();
        int limit = 15;
        while (limit > 0 && !(currentElement instanceof Method)) {
            if (currentElement instanceof ArrayCreationExpression)
                arrayCreationExpressionList.add((ArrayCreationExpression) currentElement);
            currentElement = currentElement.getParent();
            limit--;
        }
        if (arrayCreationExpressionList.size() < 2) {
            return RulePositionEnum.UNKNOWN;
        } else {
            arrayCreationExpression = arrayCreationExpressionList.get(arrayCreationExpressionList.size() - 2);
        }

        boolean innerArray = false;
        if (position.getParent().getParent().getParent() == arrayCreationExpression) {
            validationParameter = position.getParent().getParent();
        } else if (position.getParent().getParent().getParent().getParent().getParent() == arrayCreationExpression) {
            validationParameter = position.getParent().getParent().getParent().getParent();
            innerArray = true;
        } else {
            return RulePositionEnum.UNKNOWN;
        }

        if (validationParameter.toString().equals("Array value") && validationParameter.getParent() instanceof ArrayCreationExpression) {
            int index = PsiUtil.getValueIndexInArray(validationParameter, (ArrayCreationExpression) validationParameter.getParent());
            if (index == 0)
                return RulePositionEnum.FIELD;
            else if (index == 1 && !innerArray)
                return RulePositionEnum.TYPE;
            else if (index > 1 && !innerArray)
                return RulePositionEnum.OPTIONS;
            else
                return RulePositionEnum.UNKNOWN;
        }
        return RulePositionEnum.UNKNOWN;
    }
}
