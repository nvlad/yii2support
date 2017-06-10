package com.nvlad.yii2support.typeprovider;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.VariableImpl;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider3;
import com.nvlad.yii2support.common.ClassUtils;
import org.jetbrains.annotations.Nullable;


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
            PsiElement resolved = ((MethodReference)psiElement).resolve();
            if (resolved != null && resolved instanceof Method) {
                MethodReference referenceMethod = (MethodReference) psiElement;
                Method classMethod = (Method)resolved;
                classMethod = classMethod;
                if (classMethod.getFQN().equals("\\yii\\BaseYii.createObject") && referenceMethod.getParameters().length > 0) {
                    PhpPsiElement firstParam = (PhpPsiElement)referenceMethod.getParameters()[0];
                    if (firstParam instanceof ArrayCreationExpression) {
                        for (ArrayHashElement elem : ((ArrayCreationExpression) firstParam).getHashElements()  ) {
                            if (elem.getKey() != null && elem.getKey().getText() != null &&
                                    ClassUtils.removeQuotes(elem.getKey().getText()).equals("class")) {
                                PhpClass phpClass = ClassUtils.getPhpClassUniversal(psiElement.getProject(), elem.getValue());
                                return new PhpType().add(phpClass);
                            }
                        }
                    } else {
                        PhpClass phpClass = ClassUtils.getPhpClassUniversal(psiElement.getProject(), firstParam);
                        return new PhpType().add(phpClass);
                    }

                }
            }
           psiElement = psiElement;
        }
        return null;
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String s, Set<String> set, int i, Project project) {
        return null;
    }
}
