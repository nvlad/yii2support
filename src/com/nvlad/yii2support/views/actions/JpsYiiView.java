package com.nvlad.yii2support.views.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.ex.JpsElementBase;

public class JpsYiiView extends JpsElementBase<YiiViewProperties> {
    @NotNull
    @Override
    public YiiViewProperties createCopy() {
        return new YiiViewProperties("testUrl");
    }

    @Override
    public void applyChanges(@NotNull YiiViewProperties yiiViewProperties) {

    }
}

