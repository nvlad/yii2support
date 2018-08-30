package com.nvlad.yii2support.migrations.commands;

import com.intellij.execution.process.AnsiEscapeDecoder;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.nvlad.yii2support.migrations.entities.MigrateCommand;
import com.nvlad.yii2support.migrations.entities.Migration;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class CommandBase implements Runnable {
    final Project myProject;
    final MigrateCommand myCommand;
    protected JComponent myComponent;
    private ConsoleView myConsoleView;
    private Application myApplication;
    private ScheduledExecutorService myExecutorService;

    CommandBase(Project project, MigrateCommand command) {
        myProject = project;
        myCommand = command;
    }

    public ConsoleView getConsoleView() {
        return myConsoleView;
    }

    public void setConsoleView(ConsoleView myConsoleView) {
        this.myConsoleView = myConsoleView;
    }

    public void repaintComponent(JComponent component) {
        myComponent = component;
        myExecutorService = Executors.newScheduledThreadPool(1);
    }

    abstract void processOutput(String text);

    void repaintMigrationNode(Migration migration) {
        DefaultMutableTreeNode treeNode = findTreeNode(migration);
        if (treeNode != null) {
            myApplication.invokeLater(() -> ((DefaultTreeModel) ((JTree) myComponent).getModel()).nodeChanged(treeNode));
        }
    }

    abstract DefaultMutableTreeNode findTreeNode(Migration migration);

    Integer executeProcess(@NotNull ProcessHandler processHandler) {
        if (myConsoleView != null && myConsoleView.getContentSize() > 0) {
            myConsoleView.print("\n*************************************\n\n", ConsoleViewContentType.NORMAL_OUTPUT);
        }

        processHandler.addProcessListener(new CommandProcessListener(this));
        processHandler.startNotify();

        if (myComponent != null) {
            myApplication.invokeLater(() -> myComponent.setEnabled(false));

            myExecutorService.scheduleWithFixedDelay(this::updateComponent, 0, 125, TimeUnit.MILLISECONDS);
        }

        processHandler.waitFor();

        if (myComponent != null) {
            myExecutorService.shutdown();
            if (!myExecutorService.isShutdown()) {
                myExecutorService.shutdownNow();
            }

            myApplication.invokeLater(() -> {
                myComponent.repaint();

                myComponent.setEnabled(true);
            });
        }

        return processHandler.getExitCode();
    }

    void prepareCommandParams(List<String> params, String path) {
        if (!StringUtil.isEmpty(path)) {
            params.add("--migrationPath=" + path);
        }

        if (!StringUtil.isEmpty(myCommand.db)) {
            params.add("--db=" + myCommand.db);
        }

        if (myCommand.migrationTable != null) {
            params.add("--migrationTable=" + myCommand.migrationTable);
        }
//        params.add("--useTablePrefix=" + (myCommand.useTablePrefix ? "1" : "0")); // only for "create" action
        params.add("--interactive=0");
    }

    private void updateComponent() {
        myComponent.repaint();
    }

    public void setApplication(Application application) {
        myApplication = application;
    }

    class CommandProcessListener implements ProcessListener {
        private final AnsiEscapeDecoder decoder = new AnsiEscapeDecoder();
        private final CommandBase myProcessor;

        CommandProcessListener(CommandBase processor) {
            myProcessor = processor;
        }

        @Override
        public void startNotified(@NotNull ProcessEvent processEvent) {

        }

        @Override
        public void processTerminated(@NotNull ProcessEvent processEvent) {

        }

        @Override
        public void processWillTerminate(@NotNull ProcessEvent processEvent, boolean b) {

        }

        @Override
        public void onTextAvailable(@NotNull ProcessEvent processEvent, @NotNull Key key) {
            final StringBuilder builder = new StringBuilder();
            final boolean toUtf8 = SystemInfo.isWindows && key.toString().equals("stdout");
            decoder.escapeText(processEvent.getText(), key, (text, processOutputType) -> {
                if (toUtf8) {
                    text = new String(text.getBytes(), StandardCharsets.UTF_8);
                }

                builder.append(text);

                if (myConsoleView != null) {
                    myConsoleView.print(text, ConsoleViewContentType.getConsoleViewType(processOutputType));
                }
            });

            myProcessor.processOutput(builder.toString());
        }
    }
}
