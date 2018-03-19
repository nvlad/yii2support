package com.nvlad.yii2support.views.entities;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.indexing.FileContent;

import java.util.Collection;

public class ViewInfo {
    public String fileUrl;
    public String application;
    public String theme;
    public Collection<ViewParameter> parameters;
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
}
