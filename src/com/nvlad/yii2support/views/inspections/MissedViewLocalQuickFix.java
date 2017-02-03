package com.nvlad.yii2support.views.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.nvlad.yii2support.views.ViewsUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by NVlad on 15.01.2017.
 */
class MissedViewLocalQuickFix implements LocalQuickFix {
    final private String myName;

    MissedViewLocalQuickFix(String name) {
        myName = name;
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return "Create view for \"%name%\"".replace("%name%", myName);
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Create view";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        final PsiElement element = descriptor.getPsiElement().getParent();
        PsiDirectory directory;
        String path = null;

        if (element instanceof StringLiteralExpression) {
            path = ((StringLiteralExpression) element).getContents();
        }

        if (path == null) {
            return;
        }

        if (path.startsWith("/")) {
            directory = ViewsUtil.getRootDirectory(element);
            path = path.substring(1);
        } else {
            directory = ViewsUtil.getContextDirectory(element);
        }

        if (directory == null) {
            return;
        }

        while (directory != null && path.contains("/")) {
            final String subdirectory = path.substring(0, path.indexOf('/'));
            path = path.substring(path.indexOf('/') + 1);

            directory = directory.findSubdirectory(subdirectory);
        }

        if (directory != null) {
            if (!path.contains(".")) {
                path = path + ".php";
            }

            final PsiFile viewPsiFile = directory.createFile(path);
            FileEditorManager.getInstance(project).openFile(viewPsiFile.getVirtualFile(), true);

            final FileTemplate[] templates = FileTemplateManager.getDefaultInstance().getTemplates(FileTemplateManager.DEFAULT_TEMPLATES_CATEGORY);
            FileTemplate template = null;
            for (FileTemplate fileTemplate : templates) {
                if (fileTemplate.getName().equals("PHP File")) {
                    template = fileTemplate;
                    break;
                }
            }

            if (template != null && viewPsiFile.getViewProvider().getDocument() != null) {
                final Properties properties = FileTemplateManager.getDefaultInstance().getDefaultProperties();
                template.setLiveTemplateEnabled(true);
                template.setReformatCode(true);
                try {
                    viewPsiFile.getViewProvider().getDocument().insertString(0, template.getText(properties));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
