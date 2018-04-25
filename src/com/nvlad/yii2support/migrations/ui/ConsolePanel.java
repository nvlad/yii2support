package com.nvlad.yii2support.migrations.ui;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;

import javax.swing.*;

public class ConsolePanel extends SimpleToolWindowPanel {
    private final ConsoleView myConsoleView;

    public ConsolePanel(Project project) {
        super(false);

        myConsoleView = new ConsoleViewImpl(project, true);
        final JComponent consoleViewComponent = myConsoleView.getComponent();
        final DefaultActionGroup actionGroup = new DefaultActionGroup();
        final ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("MigrationsConsole", actionGroup, false);

        actionGroup.addAll(myConsoleView.createConsoleActions());

        setContent(consoleViewComponent);
        setToolbar(toolbar.getComponent());
    }

    public ConsoleView getConsoleView() {
        return myConsoleView;
    }
}
