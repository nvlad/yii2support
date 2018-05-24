package com.nvlad.yii2support.common;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PathMappingSettings;
import com.jetbrains.php.config.commandLine.PhpCommandSettings;
import com.jetbrains.php.config.commandLine.PhpCommandSettingsBuilder;
import com.jetbrains.php.remote.interpreter.PhpRemoteSdkAdditionalData;
import com.jetbrains.php.run.remote.PhpRemoteInterpreterManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class YiiCommandLineUtil {
    private static final boolean is2016 = ApplicationInfo.getInstance().getMajorVersion().equals("2016");

    private static final String[] knownErrors = new String[]{
            "userName must not be null",
            "Auth cancel",
    };

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

    @Nullable
    public static ProcessHandler configureHandler(Project project, String command, List<String> parameters) throws ExecutionException {
        parameters.add("--color");

        PhpCommandSettings commandSettings = commandSettings(project, command, parameters);
        GeneralCommandLine commandLine = commandSettings.createGeneralCommandLine();

        if (commandSettings.isRemote()) {
            PhpRemoteInterpreterManager interpreterManager = PhpRemoteInterpreterManager.getInstance();
            if (interpreterManager == null) {
                return null;
            }

            PhpRemoteSdkAdditionalData additionalData = (PhpRemoteSdkAdditionalData) commandSettings.getAdditionalData();
            if (additionalData == null) {
                return null;
            }

            try {
                Method getRemoteProcessHandler = getMethod(interpreterManager);
                if (is2016) {
                    return (ProcessHandler) getRemoteProcessHandler
                            .invoke(interpreterManager, project, "Unknown string", additionalData, commandLine);
                } else {
                    PathMappingSettings.PathMapping[] pathMappings = new PathMappingSettings.PathMapping[0];

                    return (ProcessHandler) getRemoteProcessHandler
                            .invoke(interpreterManager, project, additionalData, commandLine, pathMappings);
                }
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                String message = e.getTargetException().getMessage();

                if (!ArrayUtil.contains(message,knownErrors)) {
                    throw new RuntimeException(e);
                }

                SwingUtilities.invokeLater(() -> Messages.showErrorDialog(message, "Execution Error"));
            }

            return null;
        }

        return new OSProcessHandler(commandLine);
    }

    private static Method getMethod(PhpRemoteInterpreterManager manager) throws NoSuchMethodException {
        for (Method method : manager.getClass().getMethods()) {
            if (method.getName().equals("getRemoteProcessHandler")) {
                return method;
            }
        }

        throw new NoSuchMethodException("getRemoteProcessHandler");
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
