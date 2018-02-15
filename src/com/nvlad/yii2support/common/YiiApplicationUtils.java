package com.nvlad.yii2support.common;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class YiiApplicationUtils {
    private static VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();

    @NotNull
    public static String getApplicationName(@NotNull PsiFile file) {
        String projectUrl = file.getProject().getBaseDir().getUrl();
        if (virtualFileManager.findFileByUrl(projectUrl + "/web") == null) {
            VirtualFile virtualFile = file.getVirtualFile();
            if (virtualFile == null) {
                virtualFile = file.getOriginalFile().getVirtualFile();
            }
            final String fileUrl = virtualFile.getUrl();
            return fileUrl.substring(projectUrl.length() + 1, fileUrl.indexOf("/", projectUrl.length() + 1));
        }
        return "app";
    }

    public static String getApplicationName(@NotNull VirtualFile file, @NotNull Project project) {
        String projectUrl = project.getBaseDir().getUrl();
        if (virtualFileManager.findFileByUrl(projectUrl + "/web") == null) {
            final String fileUrl = file.getUrl();
            return fileUrl.substring(projectUrl.length() + 1, fileUrl.indexOf("/", projectUrl.length() + 1));
        }
        return "app";
    }
}
