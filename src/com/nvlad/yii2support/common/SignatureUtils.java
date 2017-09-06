package com.nvlad.yii2support.common;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Created by oleg on 2017-07-22.
 */
public class SignatureUtils {
    @Nullable
    public static PhpClass getClassBySignature(String signature, Project project) {
        int beginClassIndex = signature.indexOf("\\");
        int endClassIndex = signature.indexOf(".");
        if (endClassIndex < 0) {
            endClassIndex = signature.length() - 1;
        }
        if (beginClassIndex > -1 && beginClassIndex < endClassIndex) {
            String className = signature.substring(beginClassIndex, endClassIndex);
            Collection<PhpClass> classesByFQN = PhpIndex.getInstance(project).getClassesByFQN(className);
            if (! classesByFQN.isEmpty()) {
                return classesByFQN.iterator().next();
            }
        }
        return  null;
    }

}
