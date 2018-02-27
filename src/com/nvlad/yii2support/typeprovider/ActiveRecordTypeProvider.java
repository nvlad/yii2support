package com.nvlad.yii2support.typeprovider;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider3;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.SignatureUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by oleg on 2017-06-24.
 */
public class ActiveRecordTypeProvider  implements PhpTypeProvider3  {
    final static char TRIM_KEY = '\u0197';

    @Override
    public char getKey() {
        return '\u0856';
    }

    @Nullable
    @Override
    public PhpType getType(PsiElement psiElement) {
        if (psiElement instanceof MethodReference) {
            MethodReference methodReference = (MethodReference)psiElement;
            if (methodReference.getName() == null)
                return null;
            if (methodReference.getName().equals("one") || methodReference.getName().equals("all")) {
                String signature = methodReference.getSignature();
                return new PhpType().add("#" + this.getKey() + signature );
            }
        }
        return null;

    }

//    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String s, Project project) {
        Collection<PhpNamedElement> elements = new HashSet<>();
        PhpClass classBySignature = SignatureUtils.getClassBySignature(s, project);
        boolean classInheritsFromAD = ClassUtils.isClassInherit(classBySignature, "\\yii\\db\\BaseActiveRecord", PhpIndex.getInstance(project));
        if (classInheritsFromAD) {
            if (s.endsWith(".one"))
                elements.add(classBySignature);
            else if (s.endsWith(".all")) {
                Collection<? extends PhpNamedElement> bySignature = PhpIndex.getInstance(project).getBySignature(s);
                if (! bySignature.isEmpty()) {
                    PhpNamedElement firstItem = bySignature.iterator().next();
                    if (firstItem instanceof Method) {
                        ((Method)firstItem).getType().add(classBySignature.getFQN()+ "[]");
                    }
                }
            }
        }
        return elements;
    }

//    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String s, Set<String> set, int i, Project project) {
        return getBySignature(s, project);
    }
}
