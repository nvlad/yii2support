package com.nvlad.yii2support.views.inspections;

import com.google.common.io.Files;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.smarty.SmartyFileType;
import com.jetbrains.twig.TwigFileType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Created by NVlad on 15.01.2017.
 */
class MissedViewLocalQuickFix implements LocalQuickFix {
    final private String myName;
    final private String myPath;

    MissedViewLocalQuickFix(String name, String path) {
        myName = name;
        myPath = path;
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
        final String projectUrl = project.getBaseDir().getUrl();
        final VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
        VirtualFile virtualFile = virtualFileManager.findFileByUrl(projectUrl + myPath);
        if (virtualFile != null) {
            System.out.println("File " + projectUrl + myPath + " already exist.");
            return;
        }

        PsiDirectory directory = PsiManager.getInstance(project).findDirectory(project.getBaseDir());
        if (directory == null) {
            return;
        }

        List<String> pathElements = StringUtil.split(StringUtil.trimLeading(myPath, '/'), "/");
        if (pathElements.size() == 0) {
            return;
        }

        final String fileName = pathElements.get(pathElements.size() - 1);
        pathElements.remove(pathElements.size() - 1);
        for (String pathElement : pathElements) {
            if (directory.findSubdirectory(pathElement) == null) {
                directory.createSubdirectory(pathElement);
            }
            directory = directory.findSubdirectory(pathElement);
            if (directory == null) {
                return;
            }
        }

        final PsiFile viewPsiFile = directory.createFile(fileName);
        FileType fileType = FileTypeManager.getInstance().getFileTypeByExtension(Files.getFileExtension(myPath));
        String templateName = getFileTemplateName(fileType);
        if (templateName == null) {
            System.out.println("Template for \"" + myPath + "\" not detected.");
            return;
        }
        templateName = templateName.toLowerCase();

        FileEditorManager.getInstance(project).openFile(viewPsiFile.getVirtualFile(), true);

        final FileTemplate[] templates = FileTemplateManager.getDefaultInstance().getTemplates(FileTemplateManager.DEFAULT_TEMPLATES_CATEGORY);
        FileTemplate template = null;
        for (FileTemplate fileTemplate : templates) {
            if (fileTemplate.isTemplateOfType(fileType) && fileTemplate.getName().toLowerCase().equals(templateName)) {
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

    private String getFileTemplateName(FileType fileType) {
        if (fileType == PhpFileType.INSTANCE) {
            return "PHP File";
        } else if (fileType == SmartyFileType.INSTANCE) {
            return "Smarty File";
        } else {
            try {
                Class.forName("com.jetbrains.twig.TwigFileType");
                if (fileType == TwigFileType.INSTANCE) {
                    return "Twig File";
                }
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        return null;
    }
}
