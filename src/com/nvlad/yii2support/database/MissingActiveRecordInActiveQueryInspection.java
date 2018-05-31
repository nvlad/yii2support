package com.nvlad.yii2support.database;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.nvlad.yii2support.common.ClassUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Created by oleg on 08.04.2017.
 */
public class MissingActiveRecordInActiveQueryInspection extends PhpInspection {
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean isOnTheFly) {
        return new PhpElementVisitor() {
            @Override
            public void visitPhpClass(PhpClass clazz) {
                if (clazz.getSuperClass() != null && clazz.getSuperClass().getFQN().equals("\\yii\\db\\ActiveQuery")) {
                    PhpIndex index = PhpIndex.getInstance(clazz.getProject());

                    PhpClass activeRecordClass = ClassUtils.findClassInSeeTags(index, clazz, "\\yii\\db\\BaseActiveRecord");
                    if (activeRecordClass == null) {
                        problemsHolder.registerProblem(clazz.getFirstChild(),
                                "Can not find connected ActiveRecord class.\nYou should add @see tag with linked ActiveRecord",
                                ProblemHighlightType.WEAK_WARNING);
                    }
                }
                super.visitPhpClass(clazz);

            }
        };
    }
}
