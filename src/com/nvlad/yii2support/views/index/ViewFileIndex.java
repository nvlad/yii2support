package com.nvlad.yii2support.views.index;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import com.nvlad.yii2support.views.ViewResolve;
import com.nvlad.yii2support.views.ViewUtil;
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
        return 21;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        System.out.println("FileBasedIndex.InputFilter getInputFilter()");
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
            final Project project = inputData.getProject();

            ViewResolve resolve = ViewUtil.resolveView(inputData.getFile(), project);
            if (resolve == null) {
                return Collections.emptyMap();
            }

            final String absolutePath = inputData.getFile().getPath();
            System.out.println("ViewDataIndexer.map > " + absolutePath + " => " + resolve.key);

            Map<String, ViewInfo> map = new HashMap<>();
            ViewInfo viewInfo = new ViewInfo(inputData);
            viewInfo.application = resolve.application;
            viewInfo.theme = resolve.theme;
            viewInfo.parameters = ViewUtil.getPhpViewVariables(inputData.getPsiFile());

            map.put(resolve.key, viewInfo);
            if (resolve.key.startsWith("@app/modules/") && !resolve.relativePath.startsWith("/modules/")) {
                map.put("@app/views/modules" + resolve.key.substring(12), viewInfo);
                System.out.println("ViewDataIndexer.map > " + absolutePath + " => @app/views/modules" + resolve.key.substring(12));
            }
            return map;

//            final String projectPath = project.getBaseDir().getPath();
//            int projectBaseDirLength = projectPath.length();
//            final String absolutePath = inputData.getFile().getPath();
//            if (!absolutePath.startsWith(projectPath)) {
//                return Collections.emptyMap();
//            }
//
//            String path = absolutePath.substring(projectBaseDirLength);
//            if (!path.startsWith("/vendor/")) {
//                String application = YiiApplicationUtils.getApplicationName(inputData.getFile(), project);
//                String theme = "";
//                if (path.startsWith("/" + application + "/")) {
//                    path = path.substring(application.length() + 1);
//                }
//
//                final String viewRelativePath = path;
//                path = "@app" + path;
//                if (!path.startsWith("@app/views/") && !(path.startsWith("@app/modules/") && path.contains("/views/"))) {
//                    String viewPath = null;
//                    for (Map.Entry<Pattern, String> entry : ViewUtil.getPatterns(project).entrySet()) {
//                        Matcher matcher = entry.getKey().matcher(path);
//                        if (matcher.find()) {
//                            viewPath = entry.getValue() + path.substring(matcher.end(1));
//                            theme = matcher.group(2);
//                            break;
//                        }
//                    }
//                    if (viewPath == null) {
//                        return Collections.emptyMap();
//                    }
//                    path = viewPath;
//                }
//                if (inputData.getFile().getExtension() != null) {
//                    path = path.substring(0, path.length() - inputData.getFile().getExtension().length() - 1);
//                }
//
//                System.out.println("ViewDataIndexer.map > " + absolutePath + " => " + path);
//
//                Map<String, ViewInfo> map = new HashMap<>();
//                ViewInfo viewInfo = new ViewInfo(inputData);
//                viewInfo.application = application;
//                viewInfo.theme = theme;
//                viewInfo.parameters = ViewUtil.getPhpViewVariables(inputData.getPsiFile());
//
//                map.put(path, viewInfo);
//                if (path.startsWith("@app/modules/") && !viewRelativePath.startsWith("/modules/")) {
//                    map.put("@app/views/modules" + path.substring(12), viewInfo);
//                    System.out.println("ViewDataIndexer.map > " + absolutePath + " => @app/views/modules" + path.substring(12));
//                }
//                return map;
//            }
//
//            return Collections.emptyMap();
        }
    }

    private static class ViewInfoDataExternalizer implements DataExternalizer<ViewInfo> {
        @Override
        public void save(@NotNull DataOutput dataOutput, @NotNull ViewInfo viewInfo) throws IOException {
            System.out.println("ViewInfoDataExternalizer.save ==> " + viewInfo.fileUrl);

            writeString(dataOutput, viewInfo.fileUrl);
            writeString(dataOutput, viewInfo.application);
            writeString(dataOutput, viewInfo.theme);
            dataOutput.writeInt(viewInfo.parameters.size());
            for (String parameter : viewInfo.parameters) {
                writeString(dataOutput, parameter);
            }
        }

        @Override
        @NotNull
        public ViewInfo read(@NotNull DataInput dataInput) throws IOException {
            ViewInfo viewInfo = new ViewInfo();
            viewInfo.fileUrl = readString(dataInput);
            viewInfo.application = readString(dataInput);
            viewInfo.theme = readString(dataInput);
            final int parameterCount = dataInput.readInt();
            viewInfo.parameters = new ArrayList<>(parameterCount);
            for (int i = 0; i < parameterCount; i++) {
                viewInfo.parameters.add(readString(dataInput));
            }

            System.out.println("ViewInfoDataExternalizer.read <== " + viewInfo.fileUrl);
            return viewInfo;
        }

        private void writeString(@NotNull DataOutput dataOutput, @NotNull String data) throws IOException {
            dataOutput.writeInt(data.length());
            if (data.length() > 0) {
                dataOutput.writeChars(data);
            }
        }

        @NotNull
        private String readString(@NotNull DataInput dataInput) throws IOException {
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

        ViewFileInputFilter(FileType fileType) {
            this.myFileType = fileType;
        }

        @Override
        public boolean acceptInput(@NotNull VirtualFile virtualFile) {
            return virtualFile.getFileType() == myFileType;
        }
    }
}
