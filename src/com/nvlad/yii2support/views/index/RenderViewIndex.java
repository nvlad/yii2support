package com.nvlad.yii2support.views.index;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.nvlad.yii2support.views.entities.RenderInfo;
import com.nvlad.yii2support.views.entities.ViewInfo;
import com.nvlad.yii2support.views.entities.ViewResolve;
import com.nvlad.yii2support.views.util.ViewUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RenderViewIndex extends FileBasedIndexExtension {
    public static final ID<String, RenderInfo> identity = ID.create("Yii2Support.RenderViewIndex");
    private final RenderDataIndexer myDataIndexer;

    public RenderViewIndex() {
        this.myDataIndexer = new RenderDataIndexer();
    }


    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return new FileBasedIndex.InputFilter() {
            @Override
            public boolean acceptInput(@NotNull VirtualFile virtualFile) {
                return virtualFile.getFileType() == PhpFileType.INSTANCE;
            }
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @NotNull
    @Override
    public ID getName() {
        return identity;
    }

    @NotNull
    @Override
    public DataIndexer getIndexer() {
        return myDataIndexer;
    }

    @NotNull
    @Override
    public KeyDescriptor getKeyDescriptor() {
        return null;
    }

    @NotNull
    @Override
    public DataExternalizer getValueExternalizer() {
        return null;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    private static class RenderDataIndexer implements DataIndexer<String, RenderInfo, FileContent> {

        @Override
        @NotNull
        public Map<String, RenderInfo> map(@NotNull final FileContent inputData) {
            final Project project = inputData.getProject();
            Map<String, RenderInfo> map = new HashMap<>();
            Collection<MethodReference> methods = PsiTreeUtil.findChildrenOfType(inputData.getPsiFile(), MethodReference.class);
            
//
//
//            ViewResolve resolve = ViewUtil.resolveView(inputData.getFile(), project);
//            if (resolve == null) {
//                return Collections.emptyMap();
//            }
//
//            final String absolutePath = inputData.getFile().getPath();
//            System.out.println("ViewDataIndexer.map > " + absolutePath + " => " + resolve.key);
//
//            Map<String, ViewInfo> map = new HashMap<>();
//            ViewInfo viewInfo = new ViewInfo(inputData);
//            viewInfo.application = resolve.application;
//            viewInfo.theme = resolve.theme;
//            viewInfo.parameters = ViewUtil.getPhpViewVariables(inputData.getPsiFile());
//
//            map.put(resolve.key, viewInfo);
//            if (resolve.key.startsWith("@app/modules/") && !resolve.relativePath.startsWith("/modules/")) {
//                map.put("@app/views/modules" + resolve.key.substring(12), viewInfo);
//                System.out.println("ViewDataIndexer.map > " + absolutePath + " => @app/views/modules" + resolve.key.substring(12));
//            }
//            if (resolve.key.startsWith("@app/widgets/") && !resolve.relativePath.startsWith("/widgets/")) {
//                map.put("@app/views/widgets" + resolve.key.substring(12), viewInfo);
//                System.out.println("ViewDataIndexer.map > " + absolutePath + " => @app/views/widgets" + resolve.key.substring(12));
//            }
            return map;
        }
    }
}
