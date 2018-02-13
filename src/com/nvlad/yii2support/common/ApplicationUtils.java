package com.nvlad.yii2support.common;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;

public class ApplicationUtils {
    public static String getApplicationName(PsiFile file) {
        String projectUrl = file.getProject().getBaseDir().getUrl();
        VirtualFileManager manager = VirtualFileManager.getInstance();
        if (manager.findFileByUrl(projectUrl + "/web") == null) {
            VirtualFile virtualFile = file.getVirtualFile();
            if (virtualFile == null) {
                virtualFile = file.getOriginalFile().getVirtualFile();
            }
            final String fileUrl = virtualFile.getUrl();
            return fileUrl.substring(projectUrl.length() + 1, fileUrl.indexOf("/", projectUrl.length() + 1));
        }
        return "basic";
    }
}
