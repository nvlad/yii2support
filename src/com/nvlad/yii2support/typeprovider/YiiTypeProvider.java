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
                if (classMethod.getName().equals("createObject") && classMethod.getContainingClass() != null &&
                        (classMethod.getContainingClass().getName().equals("BaseYii")
                                || classMethod.getContainingClass().getName().equals("Yii"))
                        && referenceMethod.getParameters().length > 0) {
                    //System.out.print("getType" + (System.currentTimeMillis() % 1000) + "\n");
                    PhpPsiElement firstParam = (PhpPsiElement)referenceMethod.getParameters()[0];
                    if (firstParam instanceof ArrayCreationExpression) {
                        for (ArrayHashElement elem : ((ArrayCreationExpression) firstParam).getHashElements()  ) {
                            if (elem.getKey() != null && elem.getKey().getText() != null &&
                                    ClassUtils.removeQuotes(elem.getKey().getText()).equals("class")) {
                                PhpClass phpClass = getClass(elem.getValue());
                                //System.out.print("getType" + (System.currentTimeMillis() % 1000) + "\n");
                                return new PhpType().add(phpClass);
                            }
                        }
                    } else {
                        PhpClass phpClass = getClass(firstParam);
                        //System.out.print("getType" + (System.currentTimeMillis() % 1000) + "\n");
                        return new PhpType().add(phpClass);
                    }

                }
            }
        }
        return null;
    }

    private PhpClass getClass(PhpPsiElement elem) {
        if (elem != null)
            return  ClassUtils.getPhpClassUniversal(elem.getProject(), elem);
        return null;
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String s, Set<String> set, int i, Project project) {
        return null;
    }
}
