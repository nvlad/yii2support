package com.nvlad.yii2support.views.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;

import java.util.Collection;

public class OpenViewCalls extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        PsiFile psiFile = e.getData(PlatformDataKeys.PSI_FILE);
        if (psiFile == null) {
            return;
        }

        Collection<PsiReference> references = ReferencesSearch.search(psiFile).findAll();
        if (references.size() == 0) {
            return;
        }

        if (references.size() == 1) {
            ReferenceListPopupStep.openReference(references.iterator().next());
            return;
        }

        JBPopupFactory popupFactory = JBPopupFactory.getInstance();
        BaseListPopupStep popupStep = new ReferenceListPopupStep("Render this View from", references);
        ListPopup popup = popupFactory.createListPopup(popupStep);
        Project project = e.getProject();
        if (project == null) {
            popup.showInFocusCenter();
        } else {
            popup.showCenteredInCurrentWindow(project);
        }
    }
}
