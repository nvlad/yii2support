package com.nvlad.yii2support.typeprovider;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.impl.VariableImpl;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider3;
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
        if (psiElement instanceof VariableImpl &&
                ((VariableImpl) psiElement).getType().isEmpty()) {

            if (((VariableImpl)psiElement).getName().equals("test1")) {
                psiElement = psiElement;
                return new PhpType().add("yii\\grid\\DataColumn");
            }
        }
        return null;
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String s, Set<String> set, int i, Project project) {
        return null;
    }
}
