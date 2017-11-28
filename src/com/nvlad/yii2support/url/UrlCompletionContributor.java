package com.nvlad.yii2support.url;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.parser.parsing.expressions.Expression;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.PsiUtil;
import com.nvlad.yii2support.common.StringUtils;
import com.nvlad.yii2support.database.ParamsCompletionProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.net.util.URLUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by oleg on 25.04.2017.
 */
public class UrlCompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor  {
    public UrlCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
                PsiElement position = completionParameters.getPosition();
                boolean isInArray = position.getParent().getParent().getParent() instanceof ArrayHashElement
                        || position.getParent().getParent().getParent() instanceof ArrayCreationExpression;
                MethodReference mRef = getMethodReference(position);
                if (isInArray && mRef != null) {
                     if (mRef.getName().equals("to") && mRef.getClassReference() != null && mRef.getClassReference().getName().equals("Url")) {
                         ArrayCreationExpression arrayCreationExpression = PsiUtil.getArrayCreation(position);

                         if (arrayCreationExpression != null) {
                             int valueIndexInArray = PsiUtil.getValueIndexInArray(position.getParent().getParent(), arrayCreationExpression);
                             if (valueIndexInArray == -1)
                                 valueIndexInArray = PsiUtil.getValueIndexInArray(position.getParent().getParent().getParent(), arrayCreationExpression);
                             if (valueIndexInArray == 0) {  // First param
                                 HashMap<String, Method> routes = UrlUtils.getRoutes(position.getProject());
                                 Iterator it = routes.entrySet().iterator();
                                 while (it.hasNext()) {
                                     Map.Entry pair = (Map.Entry) it.next();
                                     Method method = (Method) pair.getValue();

                                     LookupElementBuilder builder = LookupElementBuilder.create(pair.getValue(), pair.getKey().toString());
                                     builder = builder.withTypeText(method.getContainingClass().getFQN(), true);
                                     completionResultSet.addElement(builder);
                                     it.remove();
                                 }
                             } else if (valueIndexInArray > 0) {
                                 HashMap<String, String> usedParams = new HashMap<>();
                                 for ( ArrayHashElement elem :arrayCreationExpression.getHashElements()) {
                                     if (elem.getKey() != null)
                                        usedParams.put(ClassUtils.removeQuotes(elem.getKey().getText()), elem.getKey().getText());
                                 }

                                 PsiElement firstElement = arrayCreationExpression.getChildren()[0];
                                 if (firstElement.getChildren().length > 0) {
                                     PsiElement psiElement = firstElement.getChildren()[0];
                                     if (psiElement instanceof StringLiteralExpression) {
                                         String url = ((StringLiteralExpression)psiElement).getContents();
                                         Parameter[] params = UrlUtils.getParamsByUrl(url, psiElement.getProject());

                                         for (Parameter param: params ) {
                                             if (! usedParams.containsKey(param.getName())) {
                                                 LookupElementBuilder builder = LookupElementBuilder.create(param, param.getName());
                                                 if (param.getType().isComplete())
                                                     builder = builder.withTypeText(param.getType().toString(), true);
                                                 completionResultSet.addElement(builder);
                                             }
                                         }


                                     }
                                 }
                             }
                         }

                     }
                }
            }
        });
    }

    @Nullable
    private MethodReference getMethodReference(PsiElement position) {
        MethodReference mRef = null;
        try {
            if (position.getParent().getParent().getParent().getParent().getParent() instanceof MethodReference)
                mRef = (MethodReference) position.getParent().getParent().getParent().getParent().getParent();
            if (position.getParent().getParent().getParent().getParent().getParent().getParent() instanceof MethodReference)
                mRef = (MethodReference) position.getParent().getParent().getParent().getParent().getParent().getParent();
            return mRef;
        } catch (NullPointerException ex) {
            return null;
        }
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        if ((typeChar == '\'' || typeChar == '"') && position.getParent() instanceof ArrayCreationExpression) {
            return true;
        }

        return false;
    }

    private static ElementPattern<PsiElement> ElementPattern() {
        return
                PlatformPatterns.or(
                        PlatformPatterns.psiElement().withSuperParent(3, ArrayCreationExpression.class),
                        PlatformPatterns.psiElement().withSuperParent(4, ArrayCreationExpression.class));
    }
}
