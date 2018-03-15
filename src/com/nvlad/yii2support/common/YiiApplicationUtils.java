package com.nvlad.yii2support.common;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class YiiApplicationUtils {
    private static VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
    private static Map<Project, VirtualFile> yiiRootPaths = new HashMap<>();

    @Nullable
    public static String getYiiRootPath(Project project) {
        if (yiiRootPaths.containsKey(project)) {
            return yiiRootPaths.get(project).getPath();
        }

        VirtualFile yiiRoot = findYiiRoot(project.getBaseDir());
        if (yiiRoot != null) {
            yiiRootPaths.put(project, yiiRoot);
            return yiiRoot.getPath();
        }

        return null;
    }

    @Nullable
    public static String getYiiRootUrl(Project project) {
        if (yiiRootPaths.containsKey(project)) {
            return yiiRootPaths.get(project).getUrl();
        }

        VirtualFile yiiRoot = findYiiRoot(project.getBaseDir());
        if (yiiRoot != null) {
            yiiRootPaths.put(project, yiiRoot);
            return yiiRoot.getUrl();
        }

        return null;
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
    private static VirtualFile findYiiRoot(VirtualFile dir) {
        if (dir.findFileByRelativePath("/vendor/yiisoft/yii2/Yii.php") != null) {
            return dir;
        }

        for (VirtualFile file : dir.getChildren()) {
            if (file.findFileByRelativePath("/vendor/yiisoft/yii2/Yii.php") != null) {
                return file;
            }
        }

        return null;
    }
}
