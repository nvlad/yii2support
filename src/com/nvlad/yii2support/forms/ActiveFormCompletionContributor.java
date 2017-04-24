package com.nvlad.yii2support.forms;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.DatabaseUtils;
import org.jetbrains.annotations.NotNull;
import org.mozilla.javascript.ast.VariableDeclaration;

import java.util.Hashtable;

/**
 * Created by oleg on 24.04.2017.
 */
public class ActiveFormCompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor  {
    public ActiveFormCompletionContributor() {
        extend(CompletionType.BASIC, ElementPattern(), new CompletionProvider<CompletionParameters>(){

            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                PhpExpression position = (PhpExpression) completionParameters.getPosition().getParent();
                MethodReference mRef = ClassUtils.getMethodRef(position, 3);
                if (mRef == null)
                    return;
                int paramIndex = ClassUtils.indexForElementInParameterList(position);
                if (paramIndex < 1)
                    return;
                Method method = (Method) mRef.resolve();
                if (method == null)
                    return;
                if (method.getParameters().length - 1 < paramIndex )
                    return;
                if (! method.getParameters()[paramIndex].getName().equals("attribute"))
                    return;
                PsiElement possibleVariable = mRef.getParameters()[paramIndex - 1];
                if (! (possibleVariable instanceof Variable) )
                    return;
                Variable modelVar = (Variable)possibleVariable;

                PhpClass modelClass = ClassUtils.getElementType(modelVar);
                if (modelClass == null)
                    return;

                if ( ClassUtils.isClassInherit(modelClass, "\\yii\\base\\Model", PhpIndex.getInstance(position.getProject()))) {
                    for (Field field : ClassUtils.getClassFields(modelClass)) {
                        LookupElementBuilder lookupBuilder = buildLookup(field, position);
                        completionResultSet.addElement(lookupBuilder);
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
        });
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        if ((typeChar == '\'' || typeChar == '"') && position.getParent() instanceof MethodReference) {
            return true;
        }

        return false;
    }

    private static ElementPattern<PsiElement> ElementPattern() {

        return
                PlatformPatterns.psiElement().withSuperParent(2, ParameterList.class);


    }
}
