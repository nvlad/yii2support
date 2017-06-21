package com.nvlad.yii2support.validation;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.DatabaseUtils;
import com.nvlad.yii2support.common.PsiUtil;
import com.nvlad.yii2support.objectfactory.ObjectFactoryUtils;
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
                    // Put class validators
                    HashMap<String, PhpPsiElement> validators = getDefaultValidators(phpClass.getProject());
                    for (Map.Entry<String, PhpPsiElement> entry : validators.entrySet()) {
                        completionResultSet.addElement(buildLookup(entry.getKey(), (PhpClass) entry.getValue(), phpExpression));
                    }
                    HashMap<String, PhpPsiElement> customValidators = getCustomValidators(phpClass);
                    for (Map.Entry<String, PhpPsiElement> entry : customValidators.entrySet()) {
                        completionResultSet.addElement(buildLookup((PhpClass) entry.getValue(), phpExpression));
                    }

                    // Put method validators
                    HashMap<String, PhpPsiElement> methodValidators = getMethodValidators(phpClass);
                    for (Map.Entry<String, PhpPsiElement> entry : methodValidators.entrySet()) {
                        completionResultSet.addElement(buildLookup((Method) entry.getValue(), phpExpression));
                    }
                } else if (getPosition.equals(RulePositionEnum.OPTIONS)) {
                    ArrayCreationExpression arrayCreation = (ArrayCreationExpression) PsiUtil.getSuperParent(position, ArrayCreationExpression.class, 4);
                    if (arrayCreation != null) {

                        if (arrayCreation.getChildren().length > 2) {
                            PsiElement elem = arrayCreation.getChildren()[1];
                            if (elem.getChildren().length == 0)
                                return;
                            PsiElement validatorIdentifier = elem.getChildren()[0];
                            PhpClass validator = null;

                            if (validatorIdentifier instanceof StringLiteralExpression) {
                                HashMap<String, PhpPsiElement> validators = getDefaultValidators(phpClass.getProject());
                                String value = validatorIdentifier.getText();
                                if (value != null) {
                                    PhpPsiElement validatorElement = validators.get(ClassUtils.removeQuotes(value));
                                    if (validatorElement != null && validatorElement instanceof PhpClass ) {
                                        validator = (PhpClass)validatorElement;
                                    }
                                }
                            }

                            if (validator == null && validatorIdentifier instanceof PhpPsiElement) {
                                validator = ClassUtils.getPhpClassUniversal(phpClass.getProject(), (PhpPsiElement)validatorIdentifier);
                            }

                            if (validator != null) {
                                for (Field field : ClassUtils.getClassFields((PhpClass) validator)) {
                                    completionResultSet.addElement(buildLookup(field, phpExpression, true));
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    private static HashMap<String, PhpPsiElement> getCustomValidators(PhpClass phpClass) {
        Collection<PhpClass> validatorClasses = PhpIndex.getInstance(phpClass.getProject()).getAllSubclasses("yii\\validators\\Validator");
        HashMap<String, PhpPsiElement> validators = new HashMap<>();

        for (PhpClass validatorClass : validatorClasses) {
            if (validatorClass.getSuperFQN() == null || !validatorClass.getFQN().startsWith("\\yii")) {
                validators.put(validatorClass.getName(), validatorClass);
            }
        }
        return validators;
    }

    private static HashMap<String, PhpPsiElement> getMethodValidators(PhpClass phpClass) {
        HashMap<String, PhpPsiElement> validators = new HashMap<>();
        Collection<Method> methods = phpClass.getMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("validate") && method.getName().length() > "validate".length()) {
                validators.put(method.getName().replace("validate", "").toLowerCase(), method);
            }
        }
        return validators;
    }

    private static HashMap<String, PhpPsiElement> getDefaultValidators(Project project) {
        HashMap<String, PhpPsiElement> validators = new LinkedHashMap<>();
        PhpIndex phpIndex = PhpIndex.getInstance(project);
        Collection<PhpClass> classesByFQN = phpIndex.getClassesByFQN("\\yii\\validators\\Validator");
        if (!classesByFQN.isEmpty()) {
            PhpClass validatorClass = classesByFQN.iterator().next();
            Field builtInValidatorsField = validatorClass.findOwnFieldByName("builtInValidators", false);
            ArrayCreationExpression fieldArray = (ArrayCreationExpression) builtInValidatorsField.getDefaultValue();
            Iterable<ArrayHashElement> hashElements = fieldArray.getHashElements();
            for (ArrayHashElement elem : hashElements) {
                if (elem.getValue() instanceof ArrayCreationExpression) {
                    PhpClass phpClass = ObjectFactoryUtils.findClassByArray((ArrayCreationExpression) elem.getValue());
                    validators.put(ClassUtils.removeQuotes(elem.getKey().getText()), phpClass);
                } else {

                    PhpClass phpClass = phpIndex.getClassesByFQN(ClassUtils.removeQuotes(elem.getValue().getText())).iterator().next();
                    String validatorName = ClassUtils.removeQuotes(elem.getKey().getText());
                    validators.put(validatorName, phpClass);
                }
            }
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
    private LookupElementBuilder buildLookup(Method method, PhpExpression position) {
        String lookupString = method.getName().replace("validate", "").toLowerCase();
        LookupElementBuilder builder = LookupElementBuilder.create(method, lookupString).withIcon(method.getIcon())
                .withInsertHandler((insertionContext, lookupElement) -> {
                });

        builder = builder.withTypeText(method.getFQN());


        return builder;
    }

    @NotNull
    private LookupElementBuilder buildLookup(PhpClass phpClass, PhpExpression position) {
        String lookupString = phpClass.getFQN();
        lookupString = lookupString.replaceAll("^\\\\", "");
        LookupElementBuilder builder = LookupElementBuilder.create(phpClass, lookupString).withIcon(phpClass.getIcon())
                .withInsertHandler((insertionContext, lookupElement) -> {
                })
                .withTypeText(phpClass.getNamespaceName().replaceAll("^\\\\", "").replaceAll("\\\\$", ""), true)
                .withPresentableText(phpClass.getName());

        return builder;
    }

    @NotNull
    private LookupElementBuilder buildLookup(String lookupString, PhpClass phpClass, PhpExpression position) {
        LookupElementBuilder builder = LookupElementBuilder.create(phpClass, lookupString).withIcon(phpClass.getIcon())
                .withInsertHandler((insertionContext, lookupElement) -> {
                });

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

        PsiElement validationParameter = null;


        ArrayCreationExpression arrayCreationExpression = null;
        List<ArrayCreationExpression> arrayCreationExpressionList = new ArrayList<>();
        PsiElement currentElement = position.getParent();
        int limit = 15;
        while (limit > 0 && ! (currentElement instanceof Method )) {
            if (currentElement instanceof ArrayCreationExpression)
                arrayCreationExpressionList.add ((ArrayCreationExpression)currentElement);
            currentElement = currentElement.getParent();
            limit--;
        }
        if (arrayCreationExpressionList.size() < 2)
            return RulePositionEnum.UNKNOWN;
        else
            arrayCreationExpression = arrayCreationExpressionList.get(arrayCreationExpressionList.size() - 2);


        Boolean innerArray = false;

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
            else if (index > 1 && ! innerArray)
                return RulePositionEnum.OPTIONS;
            else
                return RulePositionEnum.UNKNOWN;
        }
        return RulePositionEnum.UNKNOWN;
    }
}
