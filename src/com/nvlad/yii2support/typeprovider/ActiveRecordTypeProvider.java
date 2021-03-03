package com.nvlad.yii2support.typeprovider;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.SignatureUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

/**
 * Created by oleg on 2017-06-24.
 */
public class ActiveRecordTypeProvider  implements PhpTypeProvider4  {
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
                int beginIndex = signature.indexOf("\\");
                int endIndex = signature.indexOf("|");
                if (endIndex < 0) {
                    endIndex = signature.length() - 1;
                }
                if (beginIndex > -1 && beginIndex < endIndex) {
                    signature = signature.substring(beginIndex, endIndex);
                    return new PhpType().add("#" + this.getKey() + signature);                    
                }
            }
        }
        return null;
    }

    @Override
    @Nullable
    public PhpType complete(String s, Project project) {
        PhpType phpType = new PhpType();

        int endIndex = s.lastIndexOf(this.getKey());
        if(endIndex == -1) {
            return null;
        }

        PhpClass classBySignature = SignatureUtils.getClassBySignature(s, project);
        boolean classInheritsFromAD = ClassUtils.isClassInherit(classBySignature, "\\yii\\db\\BaseActiveRecord", PhpIndex.getInstance(project));
        if (classInheritsFromAD) {
            if (s.endsWith(".one"))
                phpType.add(classBySignature.getFQN());
            else if (s.endsWith(".all")) {
                phpType.add(classBySignature.getFQN()+ "[]");
            }
        }
        return phpType;
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String s, Set<String> set, int i, Project project) {
        return null;
    }
}
