package com.yii2support.views;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 16.01.2017.
 */
public class RenderMethodUnusedParamLocalQuickFix implements LocalQuickFix {
    private String myParam;

    @Nls
    @NotNull
    @Override
    public String getName() {
        return "Unused %param% view param";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Unused view param";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {

    }
}
