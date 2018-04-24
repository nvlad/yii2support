package com.nvlad.yii2support.migrations;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.OutputListener;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.jediterm.terminal.TerminalDataStream;
import com.jediterm.terminal.TerminalOutputStream;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.model.CharBuffer;
import com.jediterm.terminal.model.TerminalLine;
import com.jediterm.terminal.model.TerminalTextBuffer;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.config.commandLine.PhpCommandLineCommand;
import com.jetbrains.php.config.commandLine.PhpCommandSettings;
import com.jetbrains.php.config.commandLine.PhpCommandSettingsBuilder;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.run.PhpCommandLineSettings;
import com.jetbrains.php.run.script.PhpScriptRunConfiguration;
import com.nvlad.yii2support.common.FileUtil;
import com.nvlad.yii2support.common.YiiApplicationUtils;
import com.nvlad.yii2support.common.YiiCommandLineUtil;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.util.MigrationUtil;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.terminal.JBTerminalPanel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MigrationManager {
    private static final Map<Project, MigrationManager> migrationManagerMap = new HashMap<>();

    public static MigrationManager getInstance(Project project) {
        if (!migrationManagerMap.containsKey(project)) {
            migrationManagerMap.put(project, new MigrationManager(project));
        }

        return migrationManagerMap.get(project);
    }

    private final Project myProject;
    private ConsoleView myConsoleView;

    private MigrationManager(Project project) {
        myProject = project;
    }

    public void setConsoleView(ConsoleView consoleView) {
        this.myConsoleView = consoleView;
    }

    public Map<String, Collection<Migration>> getMigrations() {
        PhpIndex phpIndex = PhpIndex.getInstance(myProject);
        Collection<PhpClass> migrations = phpIndex.getAllSubclasses("\\yii\\db\\Migration");
        int baseUrlLength = myProject.getBaseDir().getUrl().length();

        Map<String, Collection<Migration>> migrationMap = new HashMap<>();
        for (PhpClass migration : migrations) {
            VirtualFile virtualFile = FileUtil.getVirtualFile(migration.getContainingFile());
            if (virtualFile.getUrl().length() < baseUrlLength) {
                continue;
            }

            String path = virtualFile.getUrl().substring(baseUrlLength + 1);
            int pathLength = path.length();
            path = path.substring(0, pathLength - virtualFile.getName().length() - 1);

            if (!migrationMap.containsKey(path)) {
                migrationMap.put(path, new LinkedList<>());
            }

            migrationMap.get(path).add(new Migration(migration, path));
        }

        return migrationMap;
    }

    private static Pattern historyPattern = Pattern.compile("(\\(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\) m\\d{6}_\\d{6}_[\\w+_-]+)", Pattern.MULTILINE);
    private static Pattern historyEntryPattern = Pattern.compile("\\((\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\) (m\\d{6}_\\d{6}_[\\w+_-]+)");

    @Nullable
    public Map<String, Date> migrateHistory() {
        try {
            Yii2SupportSettings settings = Yii2SupportSettings.getInstance(myProject);
//            LinkedList<String> params = new LinkedList<>();
//            params.push("all");
//            if (settings.dbConnection != null) {
//                params.push("--db=" + settings.dbConnection);
//            }
//            if (settings.migrationTable != null) {
//                params.push("--migrationTable=" + settings.migrationTable);
//            }

            PhpCommandSettings commandSettings = PhpCommandSettingsBuilder.create(myProject, false);
            String workDirectory = YiiApplicationUtils.getYiiRootPath(myProject);
            String script = workDirectory + "/yii";
            commandSettings.setScript(script);
            commandSettings.addArgument("migrate/history");
            commandSettings.addArgument("all");
            if (settings.dbConnection != null) {
                commandSettings.addArgument("--db=" + settings.dbConnection);
            }
            if (settings.migrationTable != null) {
                commandSettings.addArgument("--migrationTable=" + settings.migrationTable);
            }
//            commandSettings.addArgument("--color");

            GeneralCommandLine commandLine = commandSettings.createGeneralCommandLine();
            Process process = commandLine.createProcess();
            commandLine.setWorkDirectory(workDirectory);
            myConsoleView.print("> " + commandLine.getCommandLineString() + "\n\n", ConsoleViewContentType.SYSTEM_OUTPUT);

//            Process process = YiiCommandLineUtil.executeCommand(myProject, "migrate/history", params);
            if (myConsoleView != null) {
//                ProcessHandler processHandler = new ProcessHandler() {
//                    @Override
//                    protected void destroyProcessImpl() {
//                        System.out.println("destroyProcessImpl");
//                    }
//
//                    @Override
//                    protected void detachProcessImpl() {
//                        System.out.println("detachProcessImpl");
//                    }
//
//                    @Override
//                    public boolean detachIsDefault() {
//                        System.out.println("detachIsDefault");
//                        return true;
//                    }
//
//                    @Nullable
//                    @Override
//                    public OutputStream getProcessInput() {
//                        System.out.println("getProcessInput");
//                        return process.getOutputStream();
//                    }
//                };
//                processHandler.addProcessListener(new ProcessListener() {
//                    @Override
//                    public void startNotified(ProcessEvent processEvent) {
//                        System.out.println("startNotified");
//                    }
//
//                    @Override
//                    public void processTerminated(ProcessEvent processEvent) {
//                        System.out.println("processTerminated");
//                    }
//
//                    @Override
//                    public void processWillTerminate(ProcessEvent processEvent, boolean b) {
//                        System.out.println("processWillTerminate");
//                    }
//
//                    @Override
//                    public void onTextAvailable(ProcessEvent processEvent, Key key) {
//                        System.out.println(key);
//                    }
//                });
//
//                myConsoleView.attachToProcess(processHandler);
                ProcessHandler processHandler = new OSProcessHandler(commandLine) {
                    @Override
                    public boolean isSilentlyDestroyOnClose() {
                        return true;
                    }
                };
                myConsoleView.attachToProcess(processHandler);
            }

            if (process.waitFor() != 0) {
                return null;
            }
            String stream = readStream(process.getInputStream());
            if (stream == null) {
                return null;
            }

            myConsoleView.print(stream, ConsoleViewContentType.NORMAL_OUTPUT);
//            if (myTerminalPanel != null) {
//                TerminalOutputStream outputStream = myTerminalPanel.getTerminalOutputStream();
//                TerminalTextBuffer buffer = myTerminalPanel.getTerminalTextBuffer();
////                TerminalLine terminalLine = new TerminalLine();
////                TextStyle textStyle = buffer.getStyleAt(0, 0);
////                terminalLine.writeString(0, new CharBuffer("Test"), textStyle);
////                buffer.addLine(terminalLine);
//                myTerminalPanel.setForeground(JBColor.RED);
//                buffer.writeString(1, 1, new CharBuffer("Test"));
////                outputStream.sendString(stream);
//                myTerminalPanel.updateUI();
//                myTerminalPanel.setBlinkingCursor(true);
//                myTerminalPanel.setAutoscrolls(true);
//            }

            String error = readStream(process.getErrorStream());
            if (error != null) {
                return null;
            }

            Map<String, Date> result = new HashMap<>();
            if (stream.contains("No migration has been done before.")) {
                return result;
            }

            Matcher matcher = historyPattern.matcher(stream);
            while (matcher.find()) {
                String historyEntry = matcher.group(1);
                Matcher entryMatcher = historyEntryPattern.matcher(historyEntry);
                if (entryMatcher.find()) {
                    result.put(entryMatcher.group(2), MigrationUtil.applyDate(entryMatcher.group(1)));
                }
            }

            return result;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    Pattern migrateUpPattern = Pattern.compile("\\*\\*\\* applied (m\\d{6}_\\d{6}_.+?) \\(time: ");

    public Set<String> migrateUp(String path, int count) {
        try {
            Yii2SupportSettings settings = Yii2SupportSettings.getInstance(myProject);
            LinkedList<String> params = new LinkedList<>();
            params.push(String.valueOf(count));
            if (settings.dbConnection != null) {
                params.push("--db=" + settings.dbConnection);
            }
            if (settings.migrationTable != null) {
                params.push("--migrationTable=" + settings.migrationTable);
            }
            params.push("--migrationPath=" + path);
            params.push("--interactive=0");
            params.push("--color");

            Process process = YiiCommandLineUtil.executeCommand(myProject, "migrate/up", params);
            if (process.waitFor() != 0) {
                return null;
            }
            String error = readStream(process.getErrorStream());
            if (error != null) {
                return null;
            }
            String stream = readStream(process.getInputStream());
            if (stream == null) {
                return null;
            }

            Set<String> result = new HashSet<>();
            if (stream.contains("No new migrations found. Your system is up-to-date.")) {
                return result;
            }

            Matcher matcher = migrateUpPattern.matcher(stream);
            while (matcher.find()) {
                result.add(matcher.group(1));
            }

            return result;
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String readStream(InputStream stream) {
        try {
            int available = stream.available();
            if (available == 0) {
                return null;
            }
            byte[] data = new byte[available];
            int readBytes = stream.read(data);
            if (readBytes < available) {
                return null;
            }
            return new String(data);
        } catch (IOException e) {
            return null;
        }
    }
}
