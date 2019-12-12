package com.nvlad.yii2support.views.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.ListCellRendererWithRightAlignedComponent;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.lang.psi.elements.Include;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.nvlad.yii2support.common.FileUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenViewCalls extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        PsiFile psiFile = e.getData(PlatformDataKeys.PSI_FILE);
        if (psiFile == null) {
            return;
        }

        Collection<PsiReference> references = ReferencesSearch.search(psiFile).findAll();
        references.removeIf(psiReference -> psiReference.getElement() instanceof Include);

        if (references.size() == 0) {
            return;
        }

        if (references.size() == 1) {
            ReferenceListPopupStep.openReference(references.iterator().next());
            return;
        }

        BaseListPopupStep popupStep = new ReferenceListPopupStep("Render this View from", references);
        ListPopup popup = new ListPopupImpl(popupStep) {
            @Override
            protected ListCellRenderer getListElementRenderer() {
                return new ListCellRendererWithRightAlignedComponent<PsiReference>() {
                    @Override
                    protected void customize(PsiReference reference) {
                        if (reference == null) {
                            setLeftText("(empty)");
                            return;
                        }

                        PsiElement psiElement = reference.getElement();
                        if (psiElement == null) {
                            setLeftText("(empty)");
                            return;
                        }

                        PsiElement methodElement = PsiTreeUtil.getParentOfType(psiElement, MethodReference.class);
                        if (methodElement == null) {
                            setLeftText("(empty)");
                            return;
                        }

                        Project project = methodElement.getProject();
                        VirtualFile virtualFile = FileUtil.getVirtualFile(methodElement.getContainingFile());
                        String fileName = virtualFile.getUrl().replace(project.getBaseDir().getUrl(), "");

                        Document document = PsiDocumentManager.getInstance(project).getDocument(methodElement.getContainingFile());
                        if (document != null) {
                            fileName += ":" + (document.getLineNumber(psiElement.getTextOffset()) + 1);
                        }

                        Pattern pattern = Pattern.compile("([^,]+),");
                        Matcher matcher = pattern.matcher(methodElement.getText());
                        if (matcher.find()) {
                            setLeftText(matcher.group(1) + ", ...) ");
                        } else {
                            setLeftText(methodElement.getText() + " ");
                        }
                        setRightText("..." + fileName + " ");
                        setRightForeground(JBColor.GRAY);
                        setIcon(PhpIcons.METHOD);
                    }

//                    @Override
//                    public Component getListCellRendererComponent(JList jList, Object o, int i, boolean b, boolean b1) {
//                        jList.setFont(EditorUtil.getEditorFont());
//                        return super.getListCellRendererComponent(jList, o, i, b, b1);
//                    }
                };
            }
        };

        Project project = e.getProject();
        if (project == null) {
            popup.showInFocusCenter();
        } else {
            popup.showCenteredInCurrentWindow(project);
        }
    }
}
