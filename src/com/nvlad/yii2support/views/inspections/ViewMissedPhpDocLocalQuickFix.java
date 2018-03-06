package com.nvlad.yii2support.views.inspections;

import com.intellij.codeInspection.LocalQuickFixOnPsiElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.elements.GroupStatement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class ViewMissedPhpDocLocalQuickFix extends LocalQuickFixOnPsiElement {
    private final Map<String, String> myVariables;

    ViewMissedPhpDocLocalQuickFix(PsiElement element, Map<String, String> variables) {
        super(element);
        myVariables = variables;
    }

    @NotNull
    @Override
    public String getText() {
        return getFamilyName();
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Add missed variable declarations";
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiFile psiFile, @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (String key : myVariables.keySet()) {
            stringBuilder.append("\n/* @var $");
            stringBuilder.append(key);
            stringBuilder.append(" ");
            stringBuilder.append(myVariables.get(key));
            stringBuilder.append(" */");
        }

        int insertPosition = getInsertPosition(psiFile);
        if (insertPosition == 0) {
            stringBuilder.insert(0, "<?php\n");

            if (psiFile.getText().length() > 0) {
                stringBuilder.append("\n?>\n");
            }
        }
        if (insertPosition == 5) {
            stringBuilder.insert(0, "\n");
        }

        editor.getDocument().insertString(insertPosition, stringBuilder);
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    private int getInsertPosition(PsiFile psiFile) {
        if (!(psiFile.getFirstChild() instanceof GroupStatement)) {
            return 0;
        }

        PsiElement element = psiFile;
        if (psiFile.getChildren().length == 1) {
            element = element.getFirstChild();
        }
        if (element.getFirstChild().getText().equals("<?php")) {
            PsiElement phpDocComment = null;
            for (PsiElement psiElement : element.getChildren()) {
                if (element.getText().equals("<?php")) {
                    continue;
                }
                if (psiElement.getText().equals("?>")) {
                    break;
                }

                if (psiElement instanceof PhpDocComment) {
                    phpDocComment = psiElement;
                }
            }
            if (phpDocComment != null) {
                return phpDocComment.getTextRange().getEndOffset();
            }

            return 5;
        }

        return 0;
    }
}
