package com.nvlad.yii2support.migrations;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.nvlad.yii2support.common.FileUtil;
import com.nvlad.yii2support.common.YiiCommandLineUtil;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.util.MigrationUtil;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
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

    private MigrationManager(Project project) {
        myProject = project;
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

            String path = virtualFile.getUrl().substring(baseUrlLength);
            int pathLength = path.length();
            path = path.substring(0, pathLength - virtualFile.getName().length() - 1);

            if (!migrationMap.containsKey(path)) {
                migrationMap.put(path, new LinkedList<>());
            }

            migrationMap.get(path).add(new Migration(migration, path));
        }

        return migrationMap;
    }

    private static Pattern historyPattern = Pattern.compile("(\\(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\) m\\d{6}_\\d{6}_[\\w+_-]+)");
    private static Pattern historyEntryPattern = Pattern.compile("\\((\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\) (m\\d{6}_\\d{6}_[\\w+_-]+)");

    @Nullable
    public Map<String, Date> migrateHistory() {
        try {
            Process process = YiiCommandLineUtil.executeCommand(myProject, "migrate/history", "all");
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

            if (stream.contains("No migration has been done before.")) {
                return new HashMap<>();
            }

            Matcher matcher = historyPattern.matcher(stream);
            if (!matcher.find()) {
                return null;
            }

            Map<String, Date> result = new HashMap<>();
            for (int i = matcher.groupCount(); i > 0; i--) {
                String historyEntry = matcher.group(i);
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
