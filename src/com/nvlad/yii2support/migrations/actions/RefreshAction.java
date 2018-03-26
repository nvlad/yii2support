package com.nvlad.yii2support.migrations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.AnActionButton;
import com.nvlad.yii2support.migrations.MigrationManager;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.util.MigrationUtil;

import javax.swing.*;
import javax.swing.tree.TreeSelectionModel;
import java.util.Collection;
import java.util.Map;

public class RefreshAction extends AnActionButton {
    private JTree tree;
    private Map<String, Collection<Migration>> migrationMap;

    public RefreshAction(JTree migrationsTree) {
        super("Refresh migrations", AllIcons.Actions.Refresh);

        tree = migrationsTree;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
            Map<String, Collection<Migration>> newMigrationsMap = MigrationManager.getInstance(anActionEvent.getProject()).getMigrations();
//            if (!newMigrationsMap.equals(migrationMap)) {
            migrationMap = newMigrationsMap;
            MigrationUtil.updateTree(tree, migrationMap);
//            }
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);


        System.out.println("RefreshAction.actionPerformed()");
    }

    @Override
    public boolean displayTextInToolbar() {
        return false;
    }
}
