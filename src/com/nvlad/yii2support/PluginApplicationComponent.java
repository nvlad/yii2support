package com.nvlad.yii2support;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PluginApplicationComponent implements ApplicationComponent {
    public static final PluginId PLUGIN_ID = PluginId.getId("com.yii2support");

    @Override
    public void initComponent() {
        final IdeaPluginDescriptor plugin = PluginManager.getPlugin(PLUGIN_ID);
        if (plugin == null) {
            return;
        }

        final PluginGlobalSettings settings = PluginGlobalSettings.getInstance();
        if (StringUtil.isEmpty(settings.uuid)) {
            settings.uuid = UUID.randomUUID().toString();
        }

        if (StringUtil.isEmpty(settings.username)) {
            settings.username = settings.uuid;
        }

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

    public static PluginApplicationComponent getInstance() {
        return ApplicationManager.getApplication().getComponent(PluginApplicationComponent.class);
    }
}
