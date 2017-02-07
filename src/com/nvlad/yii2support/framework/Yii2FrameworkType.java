package com.nvlad.yii2support.framework;

import com.intellij.framework.FrameworkType;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by NVlad on 07.02.2017.
 */
public class Yii2FrameworkType extends FrameworkType {
    public static final Yii2FrameworkType INSTANCE = new Yii2FrameworkType();

    protected Yii2FrameworkType() {
        super("Yii2Framework");
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return "Yii2 Framework";
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return IconLoader.getIcon("/icons/yii.png");
    }
}
