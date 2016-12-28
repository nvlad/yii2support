package com.yii2framework.completion.views;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 28.12.2016.
 */
public class ExistLookupElement extends LookupElement {
    private String name;
    private String tail;

    ExistLookupElement(PsiFile psiFile) {
        String filename = psiFile.getName().substring(0, psiFile.getName().lastIndexOf("."));
        if (filename.contains(".")) {
            name = psiFile.getName();
        } else {
            name = filename;
            tail = psiFile.getName().substring(filename.length());
        }
    }

    @NotNull
    @Override
    public String getLookupString() {
        return name;
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        presentation.setIcon(IconLoader.getIcon("/icons/view.png"));
        presentation.setItemText(name);
        presentation.setItemTextBold(true);
        if (tail != null) {
            presentation.setTailText(tail, true);
        }
        presentation.setTypeText("View");
        presentation.setTypeGrayed(true);

        super.renderElement(presentation);
    }
}
