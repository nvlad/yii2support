package com.nvlad.yii2support.objectfactory;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.nvlad.yii2support.common.ClassUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Created by oleg on 15.03.2017.
 */
public class ObjectFactoryMissedFieldInspection extends PhpInspection {
    @NotNull
    @Override
    public String getShortName() {
        return "MissedFieldInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean isOnTheFly) {
        return new PhpElementVisitor() {
            @Override
            public void visitPhpArrayCreationExpression(ArrayCreationExpression expression) {
                PsiDirectory dir = expression.getContainingFile().getContainingDirectory();
                PhpClass phpClass = ObjectFactoryUtils.findClassByArrayCreation(expression, dir);
                if (phpClass != null) {
                    for (ArrayHashElement elem: expression.getHashElements()) {
                        String keyName = elem.getKey() != null ? elem.getKey().getText() : null;
                        keyName = ClassUtils.removeQuotes(keyName);
                        if ( keyName != null &&  ! keyName.equals("class") &&  ClassUtils.findField(phpClass,  keyName) == null) {
                            final String descriptionTemplate = "Field '"+keyName+"' not exists in referenced class "+phpClass.getFQN();
                            problemsHolder.registerProblem(elem, descriptionTemplate);
                        }
                    }


                }
                super.visitPhpArrayCreationExpression(expression);
            }
        };
    }
}
