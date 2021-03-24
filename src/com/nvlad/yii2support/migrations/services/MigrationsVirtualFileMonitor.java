package com.nvlad.yii2support.migrations.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpClassDeclarationInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpInstruction;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.nvlad.yii2support.common.ClassUtils;
import org.jetbrains.annotations.NotNull;

public class MigrationsVirtualFileMonitor implements VirtualFileListener {
    private final Project myProject;
    private final MigrationService service;

    public MigrationsVirtualFileMonitor(Project project) {
        myProject = project;
        service = MigrationService.getInstance(project);
    }

    @Override
    public void propertyChanged(@NotNull VirtualFilePropertyEvent virtualFilePropertyEvent) {

    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent virtualFileEvent) {
//        MigrationService service = MigrationService.getInstance(myProject);
//        service.findMigrationByFile(virtualFileEvent.getFile());
    }

    @Override
    public void fileCreated(@NotNull VirtualFileEvent event) {
        if (isMigrationFile(event)) {
            service.sync();
        }
    }

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent event) {
        ApplicationManager.getApplication().invokeLater(service::sync);
    }

    @Override
    public void fileMoved(@NotNull VirtualFileMoveEvent virtualFileMoveEvent) {

    }

    @Override
    public void fileCopied(@NotNull VirtualFileCopyEvent virtualFileCopyEvent) {

    }

    @Override
    public void beforePropertyChange(@NotNull VirtualFilePropertyEvent virtualFilePropertyEvent) {

    }

    @Override
    public void beforeContentsChange(@NotNull VirtualFileEvent virtualFileEvent) {

    }

    @Override
    public void beforeFileDeletion(@NotNull VirtualFileEvent virtualFileEvent) {

    }

    @Override
    public void beforeFileMovement(@NotNull VirtualFileMoveEvent virtualFileMoveEvent) {

    }

    private boolean isMigrationFile(VirtualFileEvent event) {
        VirtualFile virtualFile = event.getFile();
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

                if(DumbService.getInstance(myProject).isDumb()){
                    DumbService.getInstance(myProject).runWhenSmart(() -> this.fileCreated(event));
                    return false;
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
