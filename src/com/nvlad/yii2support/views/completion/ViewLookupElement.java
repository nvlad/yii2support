package com.nvlad.yii2support.views.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 28.12.2016.
 */
class ViewLookupElement extends LookupElement {
    final private PsiFile myFile;
    final private String myName;
    final private String myTail;

    ViewLookupElement(PsiFile psiFile, String insertText) {
        myFile = psiFile;
        VirtualFile file = psiFile.getVirtualFile();

        myName = insertText;
        final String ext = file.getExtension();
        if (ext != null && ext.equals("php") && !insertText.endsWith(".php")) {
            myTail = "." + file.getExtension();
        } else {
            myTail = null;
        }
    }

    @NotNull
    @Override
    public String getLookupString() {
        return myName;
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        presentation.setIcon(myFile.getIcon(0));
        presentation.setItemText(myName);
        presentation.setItemTextBold(true);
        if (myTail != null) {
            presentation.setTailText(myTail, true);
        }
        presentation.setTypeText("View");
        presentation.setTypeGrayed(true);
    }
}
