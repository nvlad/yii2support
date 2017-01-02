package com.yii2framework.views;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by NVlad on 28.12.2016.
 */
public class DirectoryLookupElement extends LookupElement {
    private Icon icon;
    private String name;

    DirectoryLookupElement(PsiDirectory directory) {
        icon = directory.getIcon(0);
        name = directory.getName();
    }

    @NotNull
    @Override
    public String getLookupString() {
        return name.concat("/");
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        super.renderElement(presentation);

        presentation.setItemText(name);
        presentation.setTailText("/", true);
        presentation.setIcon(icon);
    }
}
