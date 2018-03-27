package com.nvlad.yii2support.migrations.ui;

import com.intellij.TestRecorder;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.CheckedTreeNode;
import com.nvlad.yii2support.migrations.actions.RefreshAction;

import javax.swing.tree.DefaultMutableTreeNode;

public class MigrationPanel extends SimpleToolWindowPanel {
    private final Project myProject;
    CheckboxTree myTree;
    CheckedTreeNode myRootNode;

    public MigrationPanel(Project project) {
        super(false);

        myProject = project;

        initContent();
        initToolBar();
    }

    private void initContent() {
        MigrationTreeCellRenderer renderer = new MigrationTreeCellRenderer();
        myRootNode = new CheckedTreeNode("");
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));
        myRootNode.add(new DefaultMutableTreeNode("Test"));

        myTree = new CheckboxTree(renderer, myRootNode);
        setContent(myTree);
    }


    private void initToolBar() {
        ActionToolbar toolbar = createToolbar();
        setToolbar(toolbar.getComponent());
    }

    private ActionToolbar createToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new RefreshAction(myTree));

        return ActionManager.getInstance().createActionToolbar("Migrations", group, false);
    }

}
