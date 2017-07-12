package com.nvlad.yii2support.typeprovider;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider3;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

/**
 * Created by oleg on 2017-06-24.
 */
public class ActiveRecordTypeProvider extends CompletionContributor implements PhpTypeProvider3  {
    @Override
    public char getKey() {
        return 0;
    }

    @Nullable
    @Override
    public PhpType getType(PsiElement psiElement) {
        if (psiElement instanceof MethodReference) {
            MethodReference methodReference = (MethodReference)psiElement;
            if (methodReference.getName() == null)
                return null;
            if (methodReference.getName().equals("one") || methodReference.getName().equals("all")) {
                String activeClass = findClassByMethodReference(methodReference);
                if (activeClass != null) {
                    if (methodReference.getName().equals("one")) {
                        return new PhpType().add(activeClass);
                    } else if (methodReference.getName().equals("all")) {
                        return new PhpType().add(activeClass + "[]");
                    }
                }
            }
        }
        return null;

    }

    @Nullable
    public String findClassByMethodReference(MethodReference methodReference) {

        int limit = 40;
        while (limit > 0) {
            PhpExpression expr = methodReference.getClassReference();
            if (expr == null)
                return null;
            if (expr instanceof MethodReference) {
                methodReference = (MethodReference)expr;
                limit--;
            } else if (expr instanceof ClassReference) {
                PsiElement elem = ((ClassReference)expr).resolve();
                if (elem instanceof PhpClass)
                    return ((PhpClass)elem).getFQN();
                else
                    return null;
            } else {
                return null;
            }
        }
        return null;
    }


    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String s, Set<String> set, int i, Project project) {
        return null;
    }
}
