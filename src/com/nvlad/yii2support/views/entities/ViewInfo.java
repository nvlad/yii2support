package com.nvlad.yii2support.views.entities;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.indexing.FileContent;

import java.util.Arrays;
import java.util.Collection;

public class ViewInfo {
    public String fileUrl;
    public String application;
    public String theme;
    public Collection<String> parameters;
    private VirtualFile myVirtualFile;

    public ViewInfo() {

    }

    public ViewInfo(FileContent inputData) {
        myVirtualFile = inputData.getFile();
        fileUrl = myVirtualFile.getUrl();
    }

    public VirtualFile getVirtualFile() {
        if (myVirtualFile == null) {
            myVirtualFile = VirtualFileManager.getInstance().findFileByUrl(fileUrl);
        }

        return myVirtualFile;
    }

    @Override
    public int hashCode() {
        return fileUrl.hashCode() + application.hashCode() + theme.hashCode() + parameters.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }


        if (!(obj instanceof ViewInfo)) {
            return false;
        }

        ViewInfo viewInfo = (ViewInfo) obj;
        return StringUtil.equals(this.fileUrl, viewInfo.fileUrl)
                && StringUtil.equals(this.application, viewInfo.application)
                && StringUtil.equals(this.theme, viewInfo.theme)
                && Arrays.equals(new Collection[]{this.parameters}, new Collection[]{viewInfo.parameters});
    }
}
