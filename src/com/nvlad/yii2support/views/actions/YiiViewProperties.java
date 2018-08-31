package com.nvlad.yii2support.views.actions;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.JpsSimpleElement;
import org.jetbrains.jps.model.ex.JpsElementBase;

public class YiiViewProperties extends JpsElementBase<YiiViewProperties> implements JpsSimpleElement<YiiViewProperties> {
    public String myUrl;

    public YiiViewProperties(VirtualFile file) {
        myUrl = file.getUrl();
    }

    public YiiViewProperties(String url) {
        myUrl = url;
    }

    @NotNull
    @Override
    public YiiViewProperties getData() {
        return this;
    }

    @Override
    public void setData(@NotNull YiiViewProperties data) {
        applyChanges(data);
    }

    @NotNull
    @Override
    public YiiViewProperties createCopy() {
        return new YiiViewProperties(myUrl);
    }

    @Override
    public void applyChanges(@NotNull YiiViewProperties modified) {
        myUrl = modified.myUrl;
    }
}
