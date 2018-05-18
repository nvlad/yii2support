package com.nvlad.yii2support.common;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.project.Project;
import com.jetbrains.php.config.commandLine.PhpCommandSettings;
import com.jetbrains.php.config.commandLine.PhpCommandSettingsBuilder;
import com.jetbrains.php.remote.PhpRemoteProcessManager;
import com.jetbrains.php.remote.interpreter.PhpRemoteInterpreterFactory;
import com.jetbrains.php.remote.interpreter.PhpRemoteSdkAdditionalData;
import com.jetbrains.php.run.remote.PhpRemoteInterpreterManager;

import java.util.Arrays;
import java.util.List;

public class YiiCommandLineUtil {
    public static GeneralCommandLine create(Project project, String command) throws ExecutionException {
        return create(project, command, (String[]) null);
    }

    public static GeneralCommandLine create(Project project, String command, String... parameters) throws ExecutionException {
        return create(project, command, Arrays.asList(parameters));
    }

    public static GeneralCommandLine create(Project project, String command, List<String> parameters) throws ExecutionException {
        parameters.add("--color");

        String yiiRootPath = YiiApplicationUtils.getYiiRootPath(project);
        PhpCommandSettings commandSettings = commandSettings(project, command, parameters);
        GeneralCommandLine commandLine = commandSettings.createGeneralCommandLine();
        commandLine.setWorkDirectory(yiiRootPath);

        return commandLine;
    }

    public static ProcessHandler configureHandler(Project project, String command, List<String> parameters) throws ExecutionException {
        parameters.add("--color");
//        String yiiRootPath = YiiApplicationUtils.getYiiRootPath(project);

        PhpCommandSettings commandSettings = commandSettings(project, command, parameters);
        GeneralCommandLine commandLine = commandSettings.createGeneralCommandLine();

        if (commandSettings.isRemote()) {
            PhpRemoteInterpreterManager interpreterManager = PhpRemoteInterpreterManager.getInstance();

            PhpRemoteSdkAdditionalData additionalData = (PhpRemoteSdkAdditionalData) commandSettings.getAdditionalData();
            PhpRemoteInterpreterFactory factory = new PhpRemoteInterpreterFactory();

            if (interpreterManager == null) {
                return null;
            }

            return interpreterManager.getRemoteProcessHandler(project, "123", commandSettings.getAdditionalData(), commandLine);
        }

        return new OSProcessHandler(commandLine);
    }

    private static PhpCommandSettings commandSettings(Project project, String command, List<String> parameters) throws ExecutionException {
        String yiiRootPath = YiiApplicationUtils.getYiiRootPath(project);

        PhpCommandSettings commandSettings = PhpCommandSettingsBuilder.create(project, false);
        commandSettings.setScript(yiiRootPath + "/yii");
        commandSettings.addArgument(command);
        commandSettings.addArguments(parameters);

        return commandSettings;
    }
}
