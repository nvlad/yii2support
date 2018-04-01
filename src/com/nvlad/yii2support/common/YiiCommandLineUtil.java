package com.nvlad.yii2support.common;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;

import java.io.IOException;
import java.io.InputStream;

public class YiiCommandLineUtil {
    public static void executeCommand(Project project, String command) {
        executeCommand(project, command, null);
    }

    public static void executeCommand(Project project, String command, String[] parameters) {
        String path = YiiApplicationUtils.getYiiRootPath(project) + "/yii";
        if (SystemInfo.isWindows) {
            path += ".bat";
        }
        GeneralCommandLine commandLine = new GeneralCommandLine(path);
        commandLine.setWorkDirectory(path);
        commandLine.addParameter(command);
        if (parameters != null) {
            commandLine.addParameters(parameters);
        }

        try {
            Process process = commandLine.createProcess();
            InputStream stream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();
            try {
                process.waitFor();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            System.out.println("Process exit value: " + process.exitValue());
            try {
                int available = stream.available();
                byte[] data = new byte[available];
                stream.read(data);
                System.out.println(new String(data));

                available = errorStream.available();
                if (available > 0) {
                    byte[] errorData = new byte[available];
                    int readed = errorStream.read(errorData);
                    String string = new String(errorData);
                    System.out.println(string);
                }

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (ExecutionException e1) {
            e1.printStackTrace();
        }
    }
}
