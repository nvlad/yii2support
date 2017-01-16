package com.yii2support.views;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.yii2support.common.PsiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by NVlad on 16.01.2017.
 */
public class RenderMethodNotRequireParamsLocalQuickFix implements LocalQuickFix {
    private ArrayList<String> myUnusedParams;

    RenderMethodNotRequireParamsLocalQuickFix() {
    }

    RenderMethodNotRequireParamsLocalQuickFix(ArrayList<String> unusedParams) {
        myUnusedParams = unusedParams;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Remove all unused parameters";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        if (myUnusedParams != null) {
            ArrayCreationExpression array = (ArrayCreationExpression) descriptor.getPsiElement();
            ArrayList<ArrayHashElement> unused = new ArrayList<>();
            for (ArrayHashElement element : array.getHashElements()) {
                if (element.getKey() != null) {
                    String key = ((StringLiteralExpression) element.getKey()).getContents();
                    if (myUnusedParams.contains(key)) {
                        unused.add(element);
                    }
                }
            }
            for (ArrayHashElement element : unused) {
                PsiUtil.deleteArrayElement(element);
            }
        } else {
            PsiElement item = descriptor.getPsiElement();
            if (item.getPrevSibling() instanceof PsiWhiteSpace) {
                item.getPrevSibling().delete();
            }
            item.getPrevSibling().delete();
            item.delete();
        }
    }
}
