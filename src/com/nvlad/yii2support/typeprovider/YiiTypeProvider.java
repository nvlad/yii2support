package com.nvlad.yii2support.typeprovider;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.VariableImpl;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider3;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.MethodUtils;
import com.nvlad.yii2support.common.SignatureUtils;
import com.nvlad.yii2support.objectfactory.ObjectFactoryUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by oleg on 2017-06-08.
 */
public class YiiTypeProvider extends CompletionContributor implements PhpTypeProvider3 {
    final static char TRIM_KEY = '\u0197';

    @Override
    public char getKey() {
        return '\u0857';
    }

    @Nullable
    @Override
    public PhpType getType(PsiElement psiElement) {
        if (MethodUtils.isYiiCreateObjectMethod(psiElement)) {
                    PhpPsiElement firstParam = (PhpPsiElement)MethodUtils.getParameter((MethodReference)psiElement, 0);
                    //  Case: Yii::createObject($obj->array_var)
                    if (firstParam instanceof FieldReference ) {
                        String signature = ((MethodReference)psiElement).getSignature();
                        return new PhpType().add("#" + this.getKey() + signature + TRIM_KEY + ((FieldReference) firstParam).getSignature());
                    }
                    // Case: Yii::createObject($array_var)
                    if (firstParam instanceof Variable  && ((VariableImpl) firstParam).getDeclaredType().getTypes().contains("\\array") ) {
                        firstParam = getArrayCreationByVariableRef(firstParam);
                    }
                    if (firstParam instanceof ArrayCreationExpression) {
                        PhpType elem = getClassByArrayCreationOptimized((ArrayCreationExpression) firstParam);
                        if (elem != null) return elem;
                    }
                    else {
                        return getClass(firstParam);
                    }
        }
        return null;
    }

    @NotNull
    private PhpPsiElement getArrayCreationByVariableRef(PhpPsiElement firstParam) {
        Collection<? extends PhpNamedElement> localResolvedVariables = ((VariableImpl) firstParam).resolveLocal();
        PhpNamedElement firstElem = localResolvedVariables.iterator().next();
        if (firstElem instanceof Variable) {
            Variable variableDecl = (Variable)firstElem;
            if (variableDecl.getParent() != null) {
                PsiElement array = variableDecl.getParent().getLastChild();
                if (array instanceof ArrayCreationExpression) {
                    firstParam = (ArrayCreationExpression) array;
                }
            }
        }
        return firstParam;
    }

    @Nullable
    private PhpType getClassByArrayCreationOptimized(ArrayCreationExpression arrayCreationExpr) {
        for (ArrayHashElement elem : arrayCreationExpr.getHashElements()) {
            if (elem.getKey() != null && elem.getKey().getText() != null &&
                    ClassUtils.removeQuotes(elem.getKey().getText()).equals("class")) {
                return getClass(elem.getValue());
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
        Collection<PhpNamedElement> elements = new HashSet<>();
        int trimIndex = s.indexOf(TRIM_KEY);
        if (trimIndex > -1 && s.length() + 1 > trimIndex) {
            String origSignature = s.substring(0, trimIndex);
            if (origSignature != null && origSignature.endsWith("\\Yii.createObject")) {
                String variableRef = s.substring(trimIndex + 1);
                final Collection<? extends PhpNamedElement> indexedVariabled = PhpIndex.getInstance(project).getBySignature(variableRef);
                for (PhpNamedElement elem : indexedVariabled) {
                    if (elem instanceof Field) {
                        Field field = (Field) elem;
                        if (field.getLastChild() instanceof ArrayCreationExpression) {
                            PhpClass classByArray = ObjectFactoryUtils.findClassByArray((ArrayCreationExpression) field.getLastChild());
                            elements.add(classByArray);
                        }
                    }
                }
            }
        }

        return elements;
    }
}
