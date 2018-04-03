package com.nvlad.yii2support.migrations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.AnActionButton;
import com.nvlad.yii2support.migrations.MigrationManager;
import com.nvlad.yii2support.migrations.ui.MigrationPanel;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.Set;

@SuppressWarnings("ComponentNotRegistered")
public class MigrateUpAction extends AnActionButton {
    public MigrateUpAction() {
        super("Migrate Up", AllIcons.Actions.Rerun);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        MigrationPanel panel = (MigrationPanel) getContextComponent();
        JTree tree = panel.getTree();

        if (tree.getSelectionModel().getSelectionCount() > 0) {
            TreePath leadSelectionPath =  tree.getLeadSelectionPath();
            if (leadSelectionPath == null)  {
                return;
            }

            DefaultMutableTreeNode object = (DefaultMutableTreeNode) leadSelectionPath.getLastPathComponent();
            if (object.getUserObject() instanceof String) {
                MigrationManager manager = MigrationManager.getInstance(anActionEvent.getProject());
                Set<String> migrations = manager.migrateUp((String) object.getUserObject(), 1);
            }

//
//            final Project project = anActionEvent.getProject();
//            final Yii2SupportSettings settings = Yii2SupportSettings.getInstance(project);
//            settings.newestFirst = !settings.newestFirst;
        }

    }
}
