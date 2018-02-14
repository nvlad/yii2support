package com.nvlad.yii2support.common;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class YiiModuleUtils {
    @Nullable
    public static String getModuleName(@NotNull PsiFile file) {
        VirtualFile virtualFile = file.getVirtualFile();
        if (virtualFile == null) {
            virtualFile = file.getOriginalFile().getVirtualFile();
        }
        return getModuleName(virtualFile);
    }

    @Nullable
    public static String getModuleName(@NotNull VirtualFile file) {
        String path = getModuleUrl(file);
        if (path == null) {
            return null;
        }
        return path.substring(path.lastIndexOf('/') + 1);
    }

    @Nullable
    public static String getModuleUrl(@NotNull PsiFile file) {
        VirtualFile virtualFile = file.getVirtualFile();
        if (virtualFile == null) {
            virtualFile = file.getOriginalFile().getVirtualFile();
        }
        return getModuleUrl(virtualFile);
    }

    @Nullable
    public static String getModuleUrl(@NotNull VirtualFile file) {
        String path = file.getUrl();
        int modulesPosition = path.lastIndexOf("/modules/");
        if (modulesPosition == -1) {
            return null;
        }
        int pathEnd = path.indexOf('/', modulesPosition + 9);
        if (pathEnd != -1) {
            return path.substring(0, pathEnd);
        }
        return null;
    }
}
