package com.nvlad.yii2support.migrations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.MigrationStatus;
import com.nvlad.yii2support.migrations.util.MigrationUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.util.Collection;
import java.util.Map;

public class MigrationsToolWindowFactory implements ToolWindowFactory {
    private JTree migrationsTree;
    private JPanel mainPanel;
    private JButton refreshButton;
    private Map<String, Collection<Migration>> migrationMap;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        migrationsTree.setCellRenderer(new MigrationTreeCellRenderer());
        migrationsTree.addMouseListener(new MigrationsMouseListener());

        refreshButton.addActionListener(e -> {
            Map<String, Collection<Migration>> newMigrationsMap = MigrationUtil.getMigrations(project);
//            if (!newMigrationsMap.equals(migrationMap)) {
            migrationMap = newMigrationsMap;
            updateTree(migrationMap);
//            }
            migrationsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
        });

        migrationMap = MigrationUtil.getMigrations(project);
        updateTree(migrationMap);
    }

    private void updateTree(Map<String, Collection<Migration>> migrationMap) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) migrationsTree.getModel().getRoot();
        if (root == null) {
            return;
        }

        root.removeAllChildren();
        for (Map.Entry<String, Collection<Migration>> entry : migrationMap.entrySet()) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(entry.getKey());
            root.add(node);

            for (Migration migration : entry.getValue()) {
                migration.status = MigrationStatus.Unknown;
                node.add(new DefaultMutableTreeNode(migration));
            }
        }
        migrationsTree.updateUI();
    }
}
