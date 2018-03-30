package com.nvlad.yii2support.migrations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AnActionButton;
import com.nvlad.yii2support.migrations.MigrationManager;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.util.MigrationUtil;
import com.nvlad.yii2support.utils.Yii2SupportSettings;

import javax.swing.*;
import java.util.Collection;
import java.util.Map;

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
        final Project project = anActionEvent.getProject();
        final Yii2SupportSettings settings = Yii2SupportSettings.getInstance(project);
        settings.newestFirst = !settings.newestFirst;
        anActionEvent.getPresentation().putClientProperty(SELECTED_PROPERTY, settings.newestFirst);

        Map<String, Collection<Migration>> migrationMap = MigrationManager.getInstance(project).getMigrations();
        MigrationUtil.updateTree((JTree) getContextComponent(), migrationMap, false, settings.newestFirst);
    }

    @Override
    public void updateButton(AnActionEvent e) {
        final Yii2SupportSettings settings = Yii2SupportSettings.getInstance(e.getProject());
        e.getPresentation().putClientProperty(SELECTED_PROPERTY, settings.newestFirst);

        super.updateButton(e);
    }
}
