package com.nvlad.yii2support.migrations.commands;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.process.AnsiEscapeDecoder;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.Alarm;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.UnsupportedEncodingException;
import java.util.List;

public abstract class CommandBase implements Runnable {
    final Project myProject;
    JComponent myComponent;
    private ConsoleView myConsoleView;
    private Alarm myAlarm;
    private Application myApplication;

    CommandBase(Project project) {
        myProject = project;
    }

    public ConsoleView getConsoleView() {
        return myConsoleView;
    }

    public void setConsoleView(ConsoleView myConsoleView) {
        this.myConsoleView = myConsoleView;
    }

    public void repaintComponent(JComponent component) {
        myComponent = component;
        this.myAlarm = new Alarm();
    }

    abstract void processOutput(String text);

    void repaintMigrationNode(Migration migration) {
        DefaultMutableTreeNode treeNode = findTreeNode(migration);
        if (treeNode != null) {
            myApplication.invokeLater(() -> ((DefaultTreeModel) ((JTree) myComponent).getModel()).nodeChanged(treeNode));
        }
    }

    abstract DefaultMutableTreeNode findTreeNode(Migration migration);

    void executeProcess(ProcessHandler processHandler) {
        processHandler.addProcessListener(new CommandProcessListener(this));
        processHandler.startNotify();

        if (myComponent != null) {
            myApplication.invokeLater(() -> myComponent.setEnabled(false));

            myAlarm.addRequest(this::updateComponent, 125);
        }

        processHandler.waitFor();

        if (myComponent != null) {
            myAlarm.cancelAllRequests();

            myApplication.invokeLater(() -> {
                myComponent.repaint();

                myComponent.setEnabled(true);
            });
        }
    }

    void fillParams(List<String> params) {
        Yii2SupportSettings settings = Yii2SupportSettings.getInstance(myProject);
        if (settings.dbConnection != null) {
            params.add("--db=" + settings.dbConnection);
        }
        if (settings.migrationTable != null) {
            params.add("--migrationTable=" + settings.migrationTable);
        }
    }

    void processExecutionException(ExecutionException e) {
//        if (PhpCommandSettingsBuilder.INTERPRETER_NOT_FOUND_ERROR.equals(e.getMessage())) {
        if (myConsoleView != null) {
            myConsoleView.print(e.getMessage() + "\n", ConsoleViewContentType.ERROR_OUTPUT);
        }

        if (myApplication != null) {
            myApplication.invokeLater(() -> Messages.showErrorDialog(e.getMessage(), "Command Error"));
        }
//        }
    }

    private void updateComponent() {
        myComponent.repaint();
        myAlarm.addRequest(this::updateComponent, 125);
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
                    try {
                        text = new String(text.getBytes(), "utf-8");
                    } catch (UnsupportedEncodingException ignored) {

                    }
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
