package com.yii2support.views;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 15.01.2017.
 */
final public class RenderMethodInspection extends PhpInspection {
    @NotNull
    @Override
    public String getShortName() {
        return "RenderMethodInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean isOnTheFly) {
        return new RenderMethodPhpElementVisitor(problemsHolder);
    }
}
