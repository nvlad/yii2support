package com.yii2framework.completion.views;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 28.12.2016.
 */
public class ViewLookupElement extends LookupElement {
    private String name;
    private String tail;
    private String prefix;

    ViewLookupElement(PsiFile psiFile, String searchPrefix) {
        String filename = psiFile.getName().substring(0, psiFile.getName().lastIndexOf("."));
        if (filename.contains(".")) {
            name = psiFile.getName();
        } else {
            name = filename;
            tail = psiFile.getName().substring(filename.length());
        }
        prefix = searchPrefix;
        if (searchPrefix.startsWith("/")) {
            prefix = prefix.substring(1);
        }
        if (prefix.length() > 0 && !prefix.endsWith("/")) {
            prefix = prefix.concat("/");
        }
    }

    @NotNull
    @Override
    public String getLookupString() {
        return prefix.concat(name);
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        super.renderElement(presentation);

        presentation.setIcon(IconLoader.getIcon("/icons/view.png"));
        presentation.setItemText(name);
        presentation.setItemTextBold(true);
        if (tail != null) {
            presentation.setTailText(tail, true);
        }
        presentation.setTypeText("View");
        presentation.setTypeGrayed(true);
    }
}
