package com.yii2support.views;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by NVlad on 28.12.2016.
 */
public class ViewLookupElement extends LookupElement {
    private String name;
    private String tail;
    private Icon icon;

    ViewLookupElement(PsiFile psiFile) {
        String filename = psiFile.getName().substring(0, psiFile.getName().lastIndexOf("."));
        if (filename.contains(".")) {
            name = psiFile.getName();
        } else {
            name = filename;
            tail = psiFile.getName().substring(filename.length());
        }
        icon = psiFile.getIcon(0);
    }

    @NotNull
    @Override
    public String getLookupString() {
        return name;
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        super.renderElement(presentation);

        presentation.setIcon(icon);
        presentation.setItemText(name);
        presentation.setItemTextBold(true);
        if (tail != null) {
            presentation.setTailText(tail, true);
        }
        presentation.setTypeText("View");
        presentation.setTypeGrayed(true);
    }
}
