package com.nvlad.yii2support.errorreport;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import org.jetbrains.annotations.Nullable;

public class SentryErrorReporter {
    @Nullable
    public static String getPluginVersion(String pluginKey) {
        final PluginId pluginId = PluginId.getId(pluginKey);
        final IdeaPluginDescriptor plugin = PluginManager.getPlugin(pluginId);
        if (plugin == null) {
            return null;
        }

        return plugin.getVersion();
    }
}
