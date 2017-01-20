package com.yii2support.views.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.inspections.PhpInspection;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 15.01.2017.
 */
final public class RenderMethodUnusedParamInspection extends PhpInspection {
    @NotNull
    @Override
    public String getShortName() {
        return "RenderMethodUnusedParamInspection";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean isOnTheFly) {
        return new RenderMethodUnusedParamPhpElementVisitor(problemsHolder);
    }
}
