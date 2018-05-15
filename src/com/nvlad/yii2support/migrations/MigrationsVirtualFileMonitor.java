package com.nvlad.yii2support.migrations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpClassDeclarationInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpInstruction;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.migrations.util.MigrationUtil;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class MigrationsVirtualFileMonitor extends VirtualFileAdapter {
    private final Project myProject;
    private final JTree myTree;
    private final Yii2SupportSettings mySettings;

    MigrationsVirtualFileMonitor(Project project, JTree tree) {
        myProject = project;
        myTree = tree;
        mySettings = Yii2SupportSettings.getInstance(project);
    }

    @Override
    public void fileCreated(@NotNull VirtualFileEvent event) {
        if (isMigrationFile(event.getFile())) {
            MigrationManager manager = MigrationManager.getInstance(myProject);
            manager.refresh();
            MigrationUtil.updateTree(myTree, manager.getMigrations(), mySettings.newestFirst);
        }
    }

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent event) {
        MigrationManager manager = MigrationManager.getInstance(myProject);
        manager.refresh();
        MigrationUtil.updateTree(myTree, manager.getMigrations(), mySettings.newestFirst);
    }

    private boolean isMigrationFile(VirtualFile virtualFile) {
        PsiFile psiFile = PsiManager.getInstance(myProject).findFile(virtualFile);
        if (!(psiFile instanceof PhpFile)) {
            return false;
        }

        for (PhpInstruction instruction : ((PhpFile) psiFile).getControlFlow().getInstructions()) {
            if (instruction instanceof PhpClassDeclarationInstruction) {
                PhpClass phpClass = ((PhpClassDeclarationInstruction) instruction).getClassDeclaration();
                if (phpClass.isAbstract()) {
                    continue;
                }

                PhpIndex phpIndex = PhpIndex.getInstance(myProject);
                if (ClassUtils.isClassInheritsOrEqual(phpClass, "\\yii\\db\\Migration", phpIndex)) {
                    return true;
                }
            }
        }

        return false;
    }
}
