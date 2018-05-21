package com.nvlad.yii2support.views;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CreateFromTemplateHandler implements com.intellij.ide.fileTemplates.CreateFromTemplateHandler {
    @Override
    public boolean handlesTemplate(FileTemplate fileTemplate) {
        String templateName = fileTemplate.getName() + '.' + fileTemplate.getExtension();
        boolean result = templateName.equals("Yii2 PHP View File.php");
        return result;

    }

    @NotNull
    @Override
    public PsiElement createFromTemplate(Project project, PsiDirectory psiDirectory, String s, FileTemplate fileTemplate, String s1, @NotNull Map<String, Object> map) throws IncorrectOperationException {
        return null;
    }

    @Override
    public boolean canCreate(PsiDirectory[] psiDirectories) {
        return true;
    }

    @Override
    public boolean isNameRequired() {
        return true;
    }

    @Override
    public String getErrorMessage() {
        return null;
    }

    @Override
    public void prepareProperties(Map<String, Object> map) {
        map.put("YII2_VIEW_PARAMETERS", "this \\yii\\web\\View");
    }
}
