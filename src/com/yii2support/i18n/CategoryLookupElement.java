package com.yii2support.i18n;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 06.01.2017.
 */
class CategoryLookupElement extends LookupElement {
    final private PsiElement myCategory;

    CategoryLookupElement(PsiElement category) {
        myCategory = category;
    }

    @NotNull
    @Override
    public String getLookupString() {
        if (myCategory instanceof PsiFile) {
            String filename = ((PsiFile) myCategory).getName();
            return filename.substring(0, filename.lastIndexOf("."));
        }

        return myCategory.getText();
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        super.renderElement(presentation);

        if (myCategory instanceof PsiFile) {
            PsiFile file = (PsiFile) myCategory;
            String filename = file.getName();
            presentation.setIcon(file.getIcon(0));
            presentation.setItemText(filename.substring(0, filename.lastIndexOf(".")));
        }
    }
}
