package com.nvlad.yii2support.views.index;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ViewFileIndex extends FileBasedIndexExtension<String, ViewInfo> {
    public static final ID<String, ViewInfo> identity = ID.create("Yii2Support.ViewFileIndex");
    private final ViewDataIndexer myViewDataIndexer;
    private final ViewInfoDataExternalizer myViewInfoDataExternalizer;
    private final FileBasedIndex.InputFilter myInputFilter;

    public ViewFileIndex() {
        myViewDataIndexer = new ViewDataIndexer();
        myViewInfoDataExternalizer = new ViewInfoDataExternalizer();
        myInputFilter = new ViewFileInputFilter(PhpFileType.INSTANCE);
    }

    @NotNull
    @Override
    public ID<String, ViewInfo> getName() {
        return identity;
    }

    @NotNull
    @Override
    public DataIndexer<String, ViewInfo, FileContent> getIndexer() {
        return myViewDataIndexer;
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @NotNull
    @Override
    public DataExternalizer<ViewInfo> getValueExternalizer() {
        return myViewInfoDataExternalizer;
    }

    @Override
    public int getVersion() {
        return 2;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return myInputFilter;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    private static class ViewDataIndexer implements DataIndexer<String, ViewInfo, FileContent> {
        @Override
        @NotNull
        public Map<String, ViewInfo> map(@NotNull final FileContent inputData) {
            int projectBaseDirLength = inputData.getProject().getBaseDir().getPath().length();
            final String absolutePath = inputData.getFile().getPath();
            String path = absolutePath.substring(projectBaseDirLength);
            if (!path.startsWith("/vendor/")) {
                path = "@app" + path;
                System.out.println("ViewDataIndexer.map > " + absolutePath + " => " + path);

                Map<String, ViewInfo> map = new HashMap<>();
                ViewInfo viewInfo = new ViewInfo();
                viewInfo.fileUrl = inputData.getFile().getUrl();
                viewInfo.virtualFile = inputData.getFile();
                viewInfo.namespace = "basic";
                viewInfo.parameters = new ArrayList<>();

                map.put(path, viewInfo);
                return map;
            }

            return Collections.emptyMap();
        }
    }

    private static class ViewInfoDataExternalizer implements DataExternalizer<ViewInfo> {
        VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();

        @Override
        public void save(@NotNull DataOutput dataOutput, ViewInfo viewInfo) throws IOException {
            System.out.println("ViewInfoDataExternalizer.save > " + viewInfo.fileUrl);

            writeString(dataOutput, viewInfo.fileUrl);
            writeString(dataOutput, viewInfo.namespace);
            dataOutput.writeInt(viewInfo.parameters.size());
            for (String parameter : viewInfo.parameters) {
                writeString(dataOutput, parameter);
            }
        }

        @Override
        public ViewInfo read(@NotNull DataInput dataInput) throws IOException {
            ViewInfo viewInfo = new ViewInfo();
            final String fileUrl = readString(dataInput);
            System.out.println("ViewInfoDataExternalizer.read > " + fileUrl);
            final VirtualFile virtualFile = virtualFileManager.findFileByUrl(fileUrl);
            if (virtualFile == null) {
                return viewInfo;
            }

            viewInfo.fileUrl = fileUrl;
            viewInfo.virtualFile = virtualFile;
            viewInfo.namespace = readString(dataInput);
            final int parameterCount = dataInput.readInt();
            viewInfo.parameters = new ArrayList<>(parameterCount);
            for (int i = 0; i < parameterCount; i++) {
                viewInfo.parameters.add(readString(dataInput));
            }

            return viewInfo;
        }

        private void writeString(DataOutput dataOutput, String data) throws IOException {
            dataOutput.writeInt(data.length());
            dataOutput.writeChars(data);
        }

        private String readString(DataInput dataInput) throws IOException {
            final int length = dataInput.readInt();
            if (length == 0) {
                return "";
            }
            char[] chars = new char[length];
            for (int i = 0; i < length; i++) {
                chars[i] = dataInput.readChar();
            }
            return new String(chars);
        }
    }

    private class ViewFileInputFilter implements FileBasedIndex.InputFilter {
        private final FileType myFileType;

        public ViewFileInputFilter(FileType fileType) {
            this.myFileType = fileType;
        }

        @Override
        public boolean acceptInput(@NotNull VirtualFile virtualFile) {
            if (virtualFile.getFileType() != myFileType) {
                return false;
            }

            return virtualFile.getPath().contains("/views/");
        }
    }
}
