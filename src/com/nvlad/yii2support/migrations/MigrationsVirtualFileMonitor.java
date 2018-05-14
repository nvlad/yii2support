package com.nvlad.yii2support.migrations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.ClassUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpClassDeclarationInstruction;
import com.jetbrains.php.codeInsight.controlFlow.instructions.PhpInstruction;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.nvlad.yii2support.common.ClassUtils;
import org.jetbrains.annotations.NotNull;

public class MigrationsVirtualFileMonitor extends VirtualFileAdapter {
    private final Project myProject;

    public MigrationsVirtualFileMonitor(Project project) {
        myProject = project;
    }

    @Override
    public void fileCreated(@NotNull VirtualFileEvent event) {
                PsiFile psiFile = PsiManager.getInstance(myProject).findFile(event.getFile());
                if (psiFile instanceof PhpFile) {
//                    ToolWindow window = ToolWindowManager.getInstance(myProject).getToolWindow(MigrationsToolWindowFactory.TOOL_WINDOW_ID);
//                    if (window.isVisible()) {
//                    }
                    
                    PhpFile phpFile = (PhpFile) psiFile;
                    for (PhpInstruction instruction : phpFile.getControlFlow().getInstructions()) {
                        if (instruction instanceof PhpClassDeclarationInstruction) {
                            PhpClass phpClass = ((PhpClassDeclarationInstruction) instruction).getClassDeclaration();
                            PhpIndex phpIndex = PhpIndex.getInstance(myProject);
                            if (ClassUtils.isClassInheritsOrEqual(phpClass, "\\yii\\db\\Migration", phpIndex)) {
                                System.out.println("This is migration!!!");
                            }
                        }
                    }
                }
    }
}
