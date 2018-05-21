package com.nvlad.yii2support.views.actions;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.nvlad.yii2support.common.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.LinkedList;

class ReferenceListPopupStep extends BaseListPopupStep<PsiReference> {
    ReferenceListPopupStep(@Nullable String title, Collection<PsiReference> values) {
        super(title, new LinkedList<>(values));
    }

    @Override
    public PopupStep onChosen(PsiReference reference, boolean finalChoice) {
        openReference(reference);
        return FINAL_CHOICE;
    }

    @NotNull
    @Override
    public String getTextFor(PsiReference reference) {
        if (reference == null) {
            return "(empty)";
        }

        PsiElement psiElement = reference.getElement();
        if (psiElement == null) {
            return "(empty)";
        }

        PsiElement methodElement = PsiTreeUtil.getParentOfType(psiElement, MethodReference.class);
        if (methodElement == null) {
            return "(empty)";
        }

        Project project = methodElement.getProject();
        VirtualFile virtualFile = FileUtil.getVirtualFile(methodElement.getContainingFile());
        String fileName = virtualFile.getUrl().replace(project.getBaseDir().getUrl(), "");

        Document document = PsiDocumentManager.getInstance(project).getDocument(methodElement.getContainingFile());
        if (document != null) {
            fileName += ":" + (document.getLineNumber(psiElement.getTextOffset()) + 1);
        }

        return methodElement.getText() + " [..." + fileName + "]";
    }

    @Override
    public Icon getIconFor(PsiReference reference) {
        return PhpIcons.METHOD_ICON;
    }


    static void openReference(PsiReference reference) {
        PsiElement psiElement = reference.getElement();

        if (psiElement.getFirstChild() != null && psiElement.getFirstChild().getNextSibling() != null) {
            psiElement = psiElement.getFirstChild().getNextSibling();
        }

        if (psiElement instanceof Navigatable && ((Navigatable) psiElement).canNavigate()) {
            ((Navigatable) psiElement).navigate(true);
        }
    }
}
