package com.nvlad.yii2support.typeprovider;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl;
import com.jetbrains.php.lang.psi.elements.impl.VariableImpl;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider3;
import com.nvlad.yii2support.common.ClassUtils;
import org.jetbrains.annotations.Nullable;
import org.mozilla.javascript.ast.VariableDeclaration;


import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by oleg on 2017-06-08.
 */
public class YiiTypeProvider extends CompletionContributor implements PhpTypeProvider3 {
    @Override
    public char getKey() {
        return 'S';
    }

    @Nullable
    @Override
    public PhpType getType(PsiElement psiElement) {
        if (psiElement instanceof MethodReference) {
            MethodReference referenceMethod = (MethodReference)psiElement;
            if (referenceMethod.getName() != null && referenceMethod.getName().equals("createObject")
                    && referenceMethod.getParameters().length > 0) {
                PhpExpression classReference = ((MethodReferenceImpl) psiElement).getClassReference();
                if (classReference != null && classReference.getName() != null && classReference.getName().equals("Yii")) {
                    PhpPsiElement firstParam = (PhpPsiElement) referenceMethod.getParameters()[0];
                    if (firstParam instanceof Variable  && ((VariableImpl) firstParam).getDeclaredType().getTypes().contains("\\array") ) {
                        PsiElement variableDecl = ((VariableImpl) firstParam).resolve();
                        if (variableDecl != null && variableDecl.getParent() != null && variableDecl.getParent().getChildren().length > 1) {
                            PsiElement array = variableDecl.getParent().getChildren()[1];
                            if (array instanceof ArrayCreationExpression) {
                                firstParam = (ArrayCreationExpression)array;
                            }
                        }

                    }
                    if (firstParam instanceof ArrayCreationExpression) {
                        for (ArrayHashElement elem : ((ArrayCreationExpression) firstParam).getHashElements()) {
                            if (elem.getKey() != null && elem.getKey().getText() != null &&
                                    ClassUtils.removeQuotes(elem.getKey().getText()).equals("class")) {
                                return getClass(elem.getValue());
                            }
                        }
                    }
                    else {
                        return getClass(firstParam);
                    }

                }
            }
        }
        return null;
    }

    private PhpType getClass(PhpPsiElement elem) {
        if (elem instanceof ClassConstantReference) {
            if (elem.getName() != null && elem.getName().equals("class")
                    && ((ClassConstantReference) elem).getClassReference() != null)
                return ((ClassConstantReference) elem).getClassReference().getType();
        }
        if (elem instanceof MethodReference) {
            if (elem.getName() != null && elem.getName().equals("className")
                    && ((MethodReference) elem).getClassReference() != null)
                return ((MethodReference) elem).getClassReference().getType();
        }

        return null;
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String s, Set<String> set, int i, Project project) {
        return null;
    }
}
