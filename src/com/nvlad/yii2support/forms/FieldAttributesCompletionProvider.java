package com.nvlad.yii2support.forms;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.DatabaseUtils;
import com.nvlad.yii2support.objectfactory.ObjectFactoryUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by oleg on 2017-12-18.
 */
public class FieldAttributesCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override

    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        MethodReference methodRef = ClassUtils.getMethodRef(completionParameters.getPosition(), 10);
        PsiElement position = completionParameters.getPosition();
        if (methodRef != null) {
            Method method = (Method) methodRef.resolve();
            int paramPosition = ClassUtils.indexForElementInParameterList(completionParameters.getPosition());
            // attribute plus model parameters
            if (method != null && paramPosition > 0 && method.getParameters().length > paramPosition) {
                if (method.getParameters()[paramPosition].getName().equals("attribute") &&
                        method.getParameters()[paramPosition - 1].getName().equals("model")) {
                    PsiElement element = methodRef.getParameters()[paramPosition - 1];
                    if (element instanceof PhpClass && ClassUtils.isClassInherit((PhpClass)element, "yii\\base\\Model", PhpIndex.getInstance(position.getProject()) )) {
                        Collection<Field> classFields = ClassUtils.getClassFields((PhpClass) element);
                        PhpExpression position2 = (PhpExpression) completionParameters.getPosition().getParent();
                        for (Field field : classFields) {
                            LookupElementBuilder lookupBuilder = buildLookup(field, position2);
                            completionResultSet.addElement(lookupBuilder);
                        }
                    }

                }
            }
        }
    }

    @NotNull
    private LookupElementBuilder buildLookup(PhpClassMember field, PhpExpression position) {
        String lookupString = field instanceof Method ? ClassUtils.getAsPropertyName((Method) field) : field.getName();
        LookupElementBuilder builder =  LookupElementBuilder.create(field, lookupString).withIcon(field.getIcon())
                .withInsertHandler((insertionContext, lookupElement) -> {

                    Document document = insertionContext.getDocument();
                    int insertPosition = insertionContext.getSelectionEndOffset();

                    if (position.getParent().getParent() instanceof ArrayCreationExpression) {
                        document.insertString(insertPosition + 1, " => ");
                        insertPosition += 5;
                        insertionContext.getEditor().getCaretModel().getCurrentCaret().moveToOffset(insertPosition);
                    }
                });
        if (field instanceof Field) {
            builder = builder.withTypeText(field.getType().toString());
        }
        return builder;
    }
}
