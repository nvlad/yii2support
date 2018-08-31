package com.nvlad.yii2support.views.handlers;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.roots.ui.configuration.ModuleSourceRootEditHandler;
import com.intellij.ui.JBColor;
import com.nvlad.yii2support.views.actions.YiiViewProperties;
import com.nvlad.yii2support.views.actions.YiiViewRootType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class YiiViewRootEditHandler extends ModuleSourceRootEditHandler<YiiViewProperties> {
    protected YiiViewRootEditHandler() {
        super(YiiViewRootType.VIEW);
    }

    @NotNull
    @Override
    public String getRootTypeName() {
        return "Views";
    }

    @NotNull
    @Override
    public Icon getRootIcon() {
        return AllIcons.FileTypes.Html;
    }

    @Nullable
    @Override
    public Icon getFolderUnderRootIcon() {
        return null;
    }

    @Nullable
    @Override
    public CustomShortcutSet getMarkRootShortcutSet() {
        return null;
    }

    @NotNull
    @Override
    public String getRootsGroupTitle() {
        return "View";
    }

    @NotNull
    @Override
    public Color getRootsGroupColor() {
        return JBColor.CYAN;
    }

    @NotNull
    @Override
    public String getUnmarkRootButtonText() {
        return "Unselect";
    }
}
