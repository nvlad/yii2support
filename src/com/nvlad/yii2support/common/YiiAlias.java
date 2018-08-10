package com.nvlad.yii2support.common;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class YiiAlias {
    private static Map<Project, YiiAlias> yiiProjectAliasMap = new HashMap<>();

    public static YiiAlias getInstance(Project project) {
        if (!yiiProjectAliasMap.containsKey(project)) {
            yiiProjectAliasMap.put(project, new YiiAlias(project));
        }

        return yiiProjectAliasMap.get(project);
    }

    private final Project myProject;
    private final Map<String, String> myAliasMap;
    private final Map<String, String> myResolvedAliasCache;

    protected YiiAlias(Project project) {
        myProject = project;
        myAliasMap = new HashMap<>(new WeakHashMap<>(Yii2SupportSettings.getInstance(project).aliasMap));
        myResolvedAliasCache = new HashMap<>();
    }

    @Nullable
    public String getAlias(@NotNull String alias) {
        return aliasFromMap(myAliasMap, alias);
    }

    @Nullable
    public String resolveAlias(@NotNull String alias) {
        String path = myResolvedAliasCache.get(alias);
        if (path != null) {
            return path;
        }

        path = getAlias(alias);
        if (path == null) {
            return null;
        }

        myResolvedAliasCache.put(alias, path);

        return myResolvedAliasCache.get(alias);
    }

    public VirtualFile resolveVirtualFile(@NotNull String alias) {
        String path = resolveAlias(alias);
        if (path == null) {
            return null;
        }

        path = YiiApplicationUtils.getYiiRootPath(myProject) + "/" + path;

        return myProject.getBaseDir().getFileSystem().findFileByPath(path);
    }

    @Nullable
    private String aliasFromMap(@NotNull Map<String, String> aliasMap, @NotNull String alias) {
        if (!alias.startsWith("@")) {
            return alias;
        }

        String value = aliasMap.get(alias);
        if (value != null) {
            return aliasFromMap(aliasMap, alias);
        }

        String foundAlias = null;
        for (String aliasKey : aliasMap.keySet()) {
            if (alias.startsWith(aliasKey)) {
                if (foundAlias == null || aliasKey.length() > foundAlias.length()) {
                    foundAlias = aliasKey;
                }
            }
        }

        if (foundAlias == null) {
            return null;
        }

        value = alias.replace(foundAlias, aliasMap.get(foundAlias));
        return aliasFromMap(aliasMap, value);
    }
}
