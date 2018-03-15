package com.nvlad.yii2support.common;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class YiiApplicationUtils {
    private static VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
    private static Map<Project, VirtualFile> yiiRootPaths = new HashMap<>();

    @Nullable
    public static String getYiiRootPath(Project project) {
        VirtualFile yiiRoot = getYiiRootVirtualFile(project);

        return yiiRoot == null ? null : yiiRoot.getPath();
    }

    @Nullable
    public static String getYiiRootUrl(Project project) {
        VirtualFile yiiRoot = getYiiRootVirtualFile(project);

        return yiiRoot == null ? null : yiiRoot.getUrl();
    }

    public static void resetYiiRootPath(Project project) {
        yiiRootPaths.remove(project);
    }

    @NotNull
    public static String getApplicationName(@NotNull PsiFile file) {
        String projectUrl = getYiiRootUrl(file.getProject());
        if (projectUrl == null) {
            return "";
        }

        if (virtualFileManager.findFileByUrl(projectUrl + "/web") == null) {
            final String fileUrl = FileUtil.getVirtualFile(file).getUrl();
            return fileUrl.substring(projectUrl.length() + 1, fileUrl.indexOf("/", projectUrl.length() + 1));
        }
        return "app";
    }

    @NotNull
    public static String getApplicationName(@NotNull VirtualFile file, @NotNull Project project) {
        String projectUrl = getYiiRootUrl(project);
        if (projectUrl == null) {
            return "";
        }

        if (virtualFileManager.findFileByUrl(projectUrl + "/web") == null) {
            final String fileUrl = file.getUrl();
            final int slashPosition = fileUrl.indexOf("/", projectUrl.length() + 1);
            if (slashPosition == -1) {
                return "";
            }
            return fileUrl.substring(projectUrl.length() + 1, slashPosition);
        }
        return "app";
    }

    @Nullable
    private static VirtualFile getYiiRootVirtualFile(Project project) {
        if (yiiRootPaths.containsKey(project)) {
            return yiiRootPaths.get(project);
        }

        final String path = Yii2SupportSettings.getInstance(project).yiiRootPath;
        final VirtualFile yiiRootPath;
        if (path == null) {
            yiiRootPath = project.getBaseDir();
        } else {
            yiiRootPath = LocalFileSystem.getInstance().findFileByPath(path);
        }

        yiiRootPaths.put(project, yiiRootPath);
        return yiiRootPath;
    }
}
