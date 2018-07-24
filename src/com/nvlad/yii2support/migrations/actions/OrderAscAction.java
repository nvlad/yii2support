package com.nvlad.yii2support.migrations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.AnActionButton;
import com.nvlad.yii2support.migrations.ui.toolWindow.MigrationPanel;
import com.nvlad.yii2support.utils.Yii2SupportSettings;

@SuppressWarnings("ComponentNotRegistered")
public class OrderAscAction extends AnActionButton implements Toggleable {
    public OrderAscAction() {
        super("Newest migrations first", AllIcons.RunConfigurations.SortbyDuration);

//        if (SystemInfo.isMac) {
//            setShortcut(CustomShortcutSet.fromString("meta S"));
//        } else {
//            setShortcut(CustomShortcutSet.fromString("ctrl S"));
//        }
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        final Yii2SupportSettings settings = Yii2SupportSettings.getInstance(anActionEvent.getProject());
        settings.newestFirst = !settings.newestFirst;
        anActionEvent.getPresentation().putClientProperty(SELECTED_PROPERTY, settings.newestFirst);

        MigrationPanel panel = (MigrationPanel) getContextComponent();
        ApplicationManager.getApplication().invokeLater(() -> {
            panel.updateTree();
            panel.updateUI();
        });
    }

    @Override
    public void updateButton(AnActionEvent e) {
        final Yii2SupportSettings settings = Yii2SupportSettings.getInstance(e.getProject());
        e.getPresentation().putClientProperty(SELECTED_PROPERTY, settings.newestFirst);

        super.updateButton(e);
    }
}
