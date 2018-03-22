package com.nvlad.yii2support.migrations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.nvlad.yii2support.common.FileUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.util.*;

public class MigrationsToolWindowFactory implements ToolWindowFactory {
    private JTree migrationsTree;
    private JPanel mainPanel;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        Map<String, Set<PhpClass>> migrationMap = getMigrations(project);


        for (Map.Entry<String, Set<PhpClass>> entry : migrationMap.entrySet()) {

            TreePath path = new TreePath(entry.getKey());
            migrationsTree.addSelectionPath(path);

            System.out.println(entry.getKey());

            for (PhpClass phpClass : entry.getValue()) {
                System.out.println("- " + phpClass.getName());
            }
        }

    }

    private Map<String, Set<PhpClass>> getMigrations(Project project) {
        PhpIndex phpIndex = PhpIndex.getInstance(project);
        Collection<PhpClass> migrations = phpIndex.getAllSubclasses("\\yii\\db\\Migration");
        int baseUrlLength = project.getBaseDir().getUrl().length();

        Map<String, Set<PhpClass>> migrationMap = new HashMap<>();
        for (PhpClass migration : migrations) {
            VirtualFile virtualFile = FileUtil.getVirtualFile(migration.getContainingFile());
            if (virtualFile.getUrl().length() < baseUrlLength) {
                continue;
            }

            String path = virtualFile.getUrl().substring(baseUrlLength);
            int pathLength = path.length();
            path = path.substring(0, pathLength - virtualFile.getName().length() - 1);

            if (!migrationMap.containsKey(path)) {
                migrationMap.put(path, new HashSet<>());
            }

            migrationMap.get(path).add(migration);
        }

        return migrationMap;
    }
}
