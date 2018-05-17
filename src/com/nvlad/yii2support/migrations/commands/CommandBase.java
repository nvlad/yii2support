package com.nvlad.yii2support.migrations.commands;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.util.Alarm;
import com.jetbrains.php.config.commandLine.PhpCommandSettingsBuilder;
import com.nvlad.yii2support.migrations.MigrationService;
import com.nvlad.yii2support.migrations.util.MigrationUtil;
import com.nvlad.yii2support.utils.Yii2SupportSettings;

import javax.swing.*;
import java.util.List;

public abstract class CommandBase implements Runnable {
    final Project myProject;
    ConsoleView myConsoleView;
    Alarm myAlarm;
    JComponent myComponent;
    Application myApplication;

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

    void executeCommandLine(GeneralCommandLine commandLine) throws ExecutionException {
        Process process = commandLine.createProcess();
        ProcessHandler processHandler = new OSProcessHandler(process, "> " + commandLine.getCommandLineString());

        processHandler.addProcessListener(new CommandProcessListener(this));
        processHandler.startNotify();

        try {
            if (myComponent != null) {
                myApplication.invokeLater(() -> myComponent.setEnabled(false));

                myAlarm.addRequest(this::updateComponent, 125);
            }

            process.waitFor();

            if (myComponent != null) {
                myAlarm.cancelAllRequests();

                myApplication.invokeLater(() -> {
                    myComponent.repaint();

                    myComponent.setEnabled(true);
                });
            }
        } catch (InterruptedException e) {
            e.printStackTrace();

            if (myComponent != null) {
                myApplication.invokeLater(() -> myComponent.setEnabled(true));
            }
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
        if (PhpCommandSettingsBuilder.INTERPRETER_NOT_FOUND_ERROR.equals(e.getMessage())) {
            if (myConsoleView != null) {
                myConsoleView.print(e.getMessage() + "\n", ConsoleViewContentType.ERROR_OUTPUT);
            }

            if (myApplication != null) {
                myApplication.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Messages.showErrorDialog(e.getMessage(), "Command Error");
                    }
                });
            }

//            e.printStackTrace();
        }
    }

    private void updateComponent() {
        myComponent.repaint();
        myAlarm.addRequest(this::updateComponent, 125);
    }

    public void updateTree() {
        if (myComponent instanceof JTree) {
            Yii2SupportSettings settings = Yii2SupportSettings.getInstance(myProject);
            MigrationService service = MigrationService.getInstance(myProject);
            MigrationUtil.updateTree((JTree) myComponent, service.getMigrations(), settings.newestFirst);
        }
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
        public void startNotified(ProcessEvent processEvent) {

        }

        @Override
        public void processTerminated(ProcessEvent processEvent) {

        }

        @Override
        public void processWillTerminate(ProcessEvent processEvent, boolean b) {

        }

        @Override
        public void onTextAvailable(ProcessEvent processEvent, Key key) {
            StringBuilder builder = new StringBuilder();
            decoder.escapeText(processEvent.getText(), key, (text, processOutputType) -> {
                builder.append(text);

                if (myConsoleView != null) {
                    myConsoleView.print(text, ConsoleViewContentType.getConsoleViewType(processOutputType));
                }
            });

            myProcessor.processOutput(builder.toString());
        }
    }
}
