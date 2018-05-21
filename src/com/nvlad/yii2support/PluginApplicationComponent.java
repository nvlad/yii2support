package com.nvlad.yii2support;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.*;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.extensions.PluginId;
import org.jetbrains.annotations.NotNull;

public class PluginApplicationComponent implements ApplicationComponent {

    @Override
    public void initComponent() {
        final IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId("com.nvlad.yii2support"));
        if (plugin == null) {
            return;
        }

        final PluginGlobalSettings settings = PluginGlobalSettings.getInstance();
        if (!plugin.getVersion().equals(settings.version)) {
            settings.version = plugin.getVersion();

            String popupTitle = plugin.getName() + " v" + plugin.getVersion();
            NotificationGroup group = new NotificationGroup(plugin.getName(), NotificationDisplayType.STICKY_BALLOON, true);
            Notification notification = group.createNotification(
                    popupTitle, plugin.getChangeNotes(), NotificationType.INFORMATION, NotificationListener.URL_OPENING_LISTENER
            );
            Notifications.Bus.notify(notification);
        }
    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "PluginApplicationComponent";
    }
}
