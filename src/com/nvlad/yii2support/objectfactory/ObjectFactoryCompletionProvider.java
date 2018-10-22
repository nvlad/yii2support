package com.nvlad.yii2support.objectfactory;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Hashtable;

/**
 * Created by oleg on 16.02.2017.
 */
public class ObjectFactoryCompletionProvider extends com.intellij.codeInsight.completion.CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        if (getArrayCreation(completionParameters) == null &&
                !(walkParents(completionParameters, 4) instanceof ArrayCreationExpression) &&
                !(walkParents(completionParameters, 3) instanceof ArrayAccessExpression)) {
            return;
        }

        ArrayCreationExpression arrayCreation = null;
        // access to key of variable declared elsewhere
        if (completionParameters.getPosition().getParent().getParent().getParent() instanceof ArrayAccessExpression) {
            ArrayAccessExpression arrayAccess = (ArrayAccessExpression) completionParameters.getPosition().getParent().getParent().getParent();
            PhpPsiElement value = arrayAccess.getValue();
            if (value instanceof Variable) {
                arrayCreation = ObjectFactoryUtils.getArrayCreationByVarRef((Variable) value);
            }

            if (value instanceof FieldReference) {
                arrayCreation = ObjectFactoryUtils.getArrayCreationByFieldRef((FieldReference) value);
            }

            if (arrayCreation == null) {
                return;
            }

            // get parent array creation from inside of array
        } else if (getArrayCreation(completionParameters) != null) {
            arrayCreation = getArrayCreation(completionParameters);
        } else if (walkParents(completionParameters, 4) instanceof ArrayCreationExpression &&
                walkParents(completionParameters, 3) instanceof ArrayHashElement &&
                !walkParents(completionParameters, 2).toString().equals("Array value")) {
            arrayCreation = (ArrayCreationExpression) walkParents(completionParameters, 4);
        } else {
            return;
        }

        final PhpFile file = (PhpFile) completionParameters.getOriginalFile();
        final PsiDirectory dir = file.getContainingDirectory();
        final PhpClass phpClass = ObjectFactoryUtils.findClassByArrayCreation(arrayCreation, dir);

        final PsiElement element = completionParameters.getPosition().getParent();
        if (!(element instanceof PhpExpression)) {
            return;
        }

        final Hashtable<String, Object> uniqTracker = new Hashtable<>();
        final PhpExpression position = (PhpExpression) element;
        if (phpClass != null) {
            for (Field field : ClassUtils.getWritableClassFields(phpClass)) {
                uniqTracker.put(field.getName(), field);
                LookupElementBuilder lookupBuilder = buildLookup(field, position);
                completionResultSet.addElement(lookupBuilder);
            }

            for (Method method : ClassUtils.getClassSetMethods(phpClass)) {
                if (uniqTracker.get(ClassUtils.getAsPropertyName(method)) == null) {
                    LookupElementBuilder lookupBuilder = buildLookup(method, position);
                    completionResultSet.addElement(lookupBuilder);
                }
            }
        }
    }

    @Nullable
    private ArrayCreationExpression getArrayCreation(@NotNull CompletionParameters completionParameters) {
        PsiElement element = completionParameters.getPosition();
        int level = 0;
        while (element != null && level < 3) {
            element = element.getParent();
            level++;
        }

        if (element instanceof ArrayCreationExpression) {
            return (ArrayCreationExpression) element;
        }

        return null;
    }

    @Nullable
    private PsiElement walkParents(CompletionParameters parameters, int level) {
        PsiElement element = parameters.getPosition();
        for (int i = 0; i < level; i++) {
            if (element == null) {
                return null;
            }

            element = element.getParent();
        }

        return element;
    }

    @NotNull
    private LookupElementBuilder buildLookup(PhpClassMember field, PhpExpression position) {
        String lookupString = field instanceof Method ? ClassUtils.getAsPropertyName((Method) field) : field.getName();
        LookupElementBuilder builder = LookupElementBuilder.create(field, lookupString).withIcon(field.getIcon())
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
