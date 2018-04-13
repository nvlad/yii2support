package com.nvlad.yii2support.common;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;

import java.util.List;

public class YiiCommandLineUtil {
    public static Process executeCommand(Project project, String command) throws ExecutionException {
        return executeCommand(project, command, (String[]) null);
    }

    public static Process executeCommand(Project project, String command, List<String> parameters) throws ExecutionException {
        return executeCommand(project, command, parameters.toArray(new String[0]));
    }

    public static Process executeCommand(Project project, String command, String... parameters) throws ExecutionException {
        String path = YiiApplicationUtils.getYiiRootPath(project);
        String yii = path + "/yii";
        if (SystemInfo.isWindows) {
            yii += ".bat";
        }
        GeneralCommandLine commandLine = new GeneralCommandLine(yii);
        commandLine.setWorkDirectory(path);
        commandLine.addParameter(command);
        if (parameters != null) {
            commandLine.addParameters(parameters);
        }
        commandLine.addParameters("--color");

        return commandLine.createProcess();
    }
}
