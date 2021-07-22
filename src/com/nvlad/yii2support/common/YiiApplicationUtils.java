package com.nvlad.yii2support.common;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import com.nvlad.yii2support.views.util.ViewUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YiiApplicationUtils {
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
        return getApplicationName(FileUtil.getVirtualFile(file), file.getProject());
    }

    @NotNull
    public static String getApplicationName(@NotNull VirtualFile file, @NotNull Project project) {
        VirtualFile yiiRoot = getYiiRootVirtualFile(project);
        if (yiiRoot == null || !yiiRoot.isValid()) {
            return "";
        }

        if (yiiRoot.findChild("controllers") == null) {
            final String fileUrl = file.getUrl();
            if (!fileUrl.startsWith(yiiRoot.getUrl())) {
                return "";
            }

            int yiiRootLength = yiiRoot.getUrl().length();
            int slashIndex = fileUrl.indexOf("/", yiiRootLength + 1);
            if (slashIndex == -1) {
                return "";
            }

            String appName = fileUrl.substring(yiiRootLength + 1, slashIndex);
            String moduleName = ViewUtil.getModuleName(fileUrl.substring(yiiRootLength + 1));
            if(moduleName != null){
                return moduleName;
            }

            return appName;
        }

        return "app";
    }

    public static YiiApplicationTemplate getAppTemplate(Project project) {
        return getAppTemplate(getYiiRootVirtualFile(project));
    }

    public static YiiApplicationTemplate getAppTemplate(Project project, String path) {
        return getAppTemplate(getYiiRootVirtualFile(project, path));
    }

    public static YiiApplicationTemplate getAppTemplate(@Nullable VirtualFile yiiRoot) {
        if (yiiRoot == null || !yiiRoot.isValid()) {
            return YiiApplicationTemplate.Unknown;
        }

        if (yiiRoot.findChild("web") != null
                && yiiRoot.findChild("config") != null
                && (yiiRoot.findChild("controllers") != null || yiiRoot.findChild("modules") != null)) {
            return YiiApplicationTemplate.Basic;
        }

        VirtualFile checkFile = yiiRoot.findChild("common");
        if (checkFile != null) {
            checkFile = checkFile.findChild("migrations");
            if (checkFile != null) {
                if (checkFile.findChild("db") != null && checkFile.findChild("rbac") != null) {
                    return YiiApplicationTemplate.StarterKit;
                }
            }
        }

        return YiiApplicationTemplate.Advanced;
    }

    @Nullable
    public static VirtualFile getYiiRootVirtualFile(Project project) {
        return getYiiRootVirtualFile(project, Yii2SupportSettings.getInstance(project).yiiRootPath);

    }

    @Nullable
    public static VirtualFile getYiiRootVirtualFile(Project project, String path) {
        if (yiiRootPaths.containsKey(project)) {
            return yiiRootPaths.get(project);
        }

        VirtualFile yiiRootPath;
        if (path == null) {
            yiiRootPath = project.getBaseDir();
        } else {
            LocalFileSystem fileSystem = LocalFileSystem.getInstance();
            yiiRootPath = fileSystem.refreshAndFindFileByPath(path);
            if (yiiRootPath == null) {
                yiiRootPath = project.getBaseDir();
                path = path.replace('\\', '/');
                if (path.startsWith("./")) {
                    path = path.substring(2);
                }

                if (path.startsWith("/")) {
                    path = path.substring(1);
                }

                List<String> pathEntries = StringUtil.split(path, "/");
                for (String pathEntry : pathEntries) {
                    yiiRootPath = yiiRootPath.findChild(pathEntry);
                    if (yiiRootPath == null) {
                        break;
                    }
                }
            }
        }

        yiiRootPaths.put(project, yiiRootPath);

        return yiiRootPath;
    }
}
