package com.yii2framework.completion.views;

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
    private String prefix;
    private String lookup;

    DirectoryLookupElement(PsiDirectory directory, String searchPrefix) {
        icon = directory.getIcon(0);
        name = directory.getName();
        prefix = searchPrefix;
        if (searchPrefix.startsWith("/")) {
            prefix = prefix.substring(1);
        }
        if (prefix.length() > 0 && !prefix.endsWith("/")) {
            prefix = prefix.concat("/");
        }
        lookup = directory.getName().concat("/");
    }

    @NotNull
    @Override
    public String getLookupString() {
        return prefix.concat(lookup);
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        super.renderElement(presentation);

        presentation.setItemText(name);
        presentation.setTailText("/", true);
        presentation.setIcon(icon);
    }
}
