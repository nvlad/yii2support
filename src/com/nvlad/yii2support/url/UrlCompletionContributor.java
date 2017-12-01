package com.nvlad.yii2support.url;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.MethodUtils;
import com.nvlad.yii2support.common.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

                // url param
                if ( PsiUtil.getSuperParent(position, 3) instanceof MethodReference) {
                    MethodReference methodRef2 = (MethodReference) PsiUtil.getSuperParent(position, 3);
                    if (methodRef2 != null) {
                        Method method = (Method)methodRef2.resolve();
                        if (method != null) {
                            int urlParamIndex = ClassUtils.getParamIndex(method, "url");
                            if (urlParamIndex > -1) {
                                int index = MethodUtils.paramIndexByRef(position.getParent());
                                if (index == urlParamIndex) {
                                    buildUrlCompletionList(completionResultSet, position);
                                }
                            }
                        }
                    }

                }


                // Check Url::to
                boolean isInArray = position.getParent().getParent().getParent() instanceof ArrayHashElement
                        || position.getParent().getParent().getParent() instanceof ArrayCreationExpression;
                MethodReference mRef = getMethodReference(position);
                if (isInArray && mRef != null && mRef.getName() != null) {
                    boolean isUrlParam = false;
                    Method method = (Method)mRef.resolve();
                    if (method != null) {
                        int urlParamIndex = ClassUtils.getParamIndex(method, "url");
                        if (urlParamIndex > -1) {
                            int index = MethodUtils.paramIndexByRef(position.getParent().getParent().getParent());
                            if (index == -1)
                                index = MethodUtils.paramIndexByRef(position.getParent().getParent().getParent().getParent());
                            if (index == urlParamIndex) {
                                isUrlParam = true;
                            }
                        }
                    }

                     if (isUrlParam) {
                         ArrayCreationExpression arrayCreationExpression = PsiUtil.getArrayCreation(position);

                         if (arrayCreationExpression != null) {
                             int valueIndexInArray = PsiUtil.getValueIndexInArray(position.getParent().getParent(), arrayCreationExpression);
                             if (valueIndexInArray == -1)
                                 valueIndexInArray = PsiUtil.getValueIndexInArray(position.getParent().getParent().getParent(), arrayCreationExpression);
                             if (valueIndexInArray == 0) {  // First array item, url
                                 buildUrlCompletionList(completionResultSet, position);
                             } else if (valueIndexInArray > 0) {  // Next array items, params
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
                                         if (params != null) {
                                             for (Parameter param : params) {
                                                 if (!usedParams.containsKey(param.getName())) {
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
            }
        });
    }

    private void buildUrlCompletionList(@NotNull CompletionResultSet completionResultSet, PsiElement position) {
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
