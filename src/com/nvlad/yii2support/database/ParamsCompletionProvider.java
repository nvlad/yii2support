package com.nvlad.yii2support.database;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.DatabaseUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by oleg on 28.03.2017.
 */
public class ParamsCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        MethodReference methodRef = ClassUtils.getMethodRef(completionParameters.getPosition(), 10);
        PsiElement position = completionParameters.getPosition();
        if (methodRef != null) {
            Method method = (Method) methodRef.resolve();
            int paramPosition = ClassUtils.paramIndexForElement(completionParameters.getPosition());
            if (paramPosition > 0 && method.getParameters().length > paramPosition) {
                if (method.getParameters()[paramPosition].getName().equals("params") &&
                        ( method.getParameters()[paramPosition - 1].getName().equals("condition") ||
                                method.getParameters()[paramPosition - 1].getName().equals("sql") ||
                                method.getParameters()[paramPosition - 1].getName().equals("expression") )) {
                    PsiElement element = methodRef.getParameters()[paramPosition - 1];
                    String condition = element.getText();
                    String[] result = DatabaseUtils.extractParamsFromCondition(condition);
                    ArrayList<String> usedItems = new ArrayList<>();
                    if (position.getParent().getParent().getParent() instanceof ArrayCreationExpression) {
                        ArrayCreationExpression array = (ArrayCreationExpression) position.getParent().getParent().getParent();
                        for (ArrayHashElement elem : array.getHashElements()) {
                            usedItems.add(ClassUtils.removeQuotes(elem.getKey().getText()));
                        }
                    }

                    for (String resultItem : result) {
                        if (!usedItems.contains(resultItem)) {
                            LookupElementBuilder builder = LookupElementBuilder.create(resultItem).withInsertHandler((insertionContext, lookupElement) -> {

                                Document document = insertionContext.getDocument();
                                int insertPosition = insertionContext.getSelectionEndOffset();

                                if (position.getParent().getParent().getParent() instanceof ArrayCreationExpression) {
                                    document.insertString(insertPosition + 1, " => ");
                                    insertPosition += 5;
                                    insertionContext.getEditor().getCaretModel().getCurrentCaret().moveToOffset(insertPosition);
                                }
                            });
                            completionResultSet.addElement(builder);
                        }
                    }


                }
            }
        }
    }
}
