package com.nvlad.yii2support.migrations;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.JBColor;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import com.nvlad.yii2support.migrations.actions.RefreshAction;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.util.MigrationUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Map;

public class MigrationsToolWindowFactory implements ToolWindowFactory {
    private JPanel mainPanel;
//    private JButton refreshButton;
    private JTree migrationsTree;
    private JPanel toolbarPanel;
    //    private JPanel forToolbars;
    private ActionToolbar toolbar;
    private Map<String, Collection<Migration>> migrationMap;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
//        initToolbar();

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);

        migrationsTree.setCellRenderer(new MigrationTreeCellRenderer());
        migrationsTree.addMouseListener(new MigrationsMouseListener());

//        refreshButton.addActionListener(e -> {
//            Map<String, Collection<Migration>> newMigrationsMap = MigrationUtil.getMigrations(project);
////            if (!newMigrationsMap.equals(migrationMap)) {
//            migrationMap = newMigrationsMap;
//            updateTree(migrationMap);
////            }
//            migrationsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
//
//            migrationPanel.updateUI();
//        });

        migrationMap = MigrationManager.getInstance(project).getMigrations();
        MigrationUtil.updateTree(migrationsTree, migrationMap);

        initToolbar();
    }

//    private void updateTree(Map<String, Collection<Migration>> migrationMap) {
//        DefaultMutableTreeNode root = (DefaultMutableTreeNode) migrationsTree.getModel().getRoot();
//        if (root == null) {
//            return;
//        }
//
//        root.removeAllChildren();
//        for (Map.Entry<String, Collection<Migration>> entry : migrationMap.entrySet()) {
//            DefaultMutableTreeNode node = new DefaultMutableTreeNode(entry.getKey());
//            root.add(node);
//
//            for (Migration migration : entry.getValue()) {
//                migration.status = MigrationStatus.Unknown;
//                node.add(new DefaultMutableTreeNode(migration));
//            }
//        }
//        migrationsTree.updateUI();
//    }

    private void initToolbar() {
        JPanel parentPanel = (JPanel) toolbarPanel.getParent();
        parentPanel.setBorder(JBUI.Borders.customLine(JBColor.border(), 0, 0, 0, 1));

        DefaultActionGroup actions = new DefaultActionGroup();
        RefreshAction refreshButton = new RefreshAction(migrationsTree);
        refreshButton.setContextComponent(migrationsTree);
//        refreshButton.addCustomUpdater(anActionEvent -> {
//            System.out.println("RefreshAction.addCustomUpdater()");
//            anActionEvent.getPresentation().setEnabled(true);
//            anActionEvent.getPresentation().setIcon(AllIcons.General.Error);
//            return true;
//        });
        actions.add(refreshButton);
//        actions.addSeparator();
        AnActionButton borderRight = new AnActionButton("Border", AllIcons.Actions.AllRight) {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                migrationsTree.setAutoscrolls(true);
            }
        };
        borderRight.setContextComponent(migrationsTree);
        actions.add(borderRight);

        ActionManager actionManager = ActionManager.getInstance();
        toolbar = actionManager.createActionToolbar("MigrationToolbar", actions, false);

        JComponent component = toolbar.getComponent();
        toolbarPanel.add(component, 0);
    }
}
