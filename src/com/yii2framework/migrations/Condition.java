package com.yii2framework.migrations;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;

/**
 * Created by NVlad on 27.12.2016.
 *
 */
public class Condition implements com.intellij.openapi.util.Condition {

    @Override
    public boolean value(Object o) {
        Notifications.Bus.notify(new Notification("Yii2Framework", "Yii2 Framework :: Migrations", "Condition not realized.", NotificationType.ERROR));

        return false;
    }
}
