package com.nvlad.yii2support.views.actions;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.actions.MarkRootActionBase;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class MarkViewsRootAction extends MarkRootActionBase {
    public MarkViewsRootAction() {
        Presentation presentation = getTemplatePresentation();

        presentation.setIcon(AllIcons.FileTypes.Html);
        presentation.setText("Mark as Views Root");
    }

    @Override
    protected void modifyRoots(VirtualFile file, ContentEntry entry) {
        entry.addSourceFolder(file, YiiViewRootType.VIEW);
    }

    @Override
    protected boolean isEnabled(@NotNull RootsSelection selection, @NotNull Module module) {
        if (selection.myHaveSelectedFilesUnderSourceRoots) {
            return false;
        }

        if (selection.mySelectedRoots.isEmpty()) {
            return true;
        }

//        for (SourceFolder root : selection.mySelectedRoots) {
//            YiiViewProperties properties = root.getJpsElement().getProperties(JavaModuleSourceRootTypes.SOURCES);
//            if (properties != null && !properties.isForGeneratedSources()) {
//                return true;
//            }
//        }

        return false;
    }
}
