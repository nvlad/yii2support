package com.nvlad.yii2support.core;

import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created by NVlad on 03.02.2017.
 */
public class ApplicationIndex extends FileBasedIndexExtension {
    final public static ID<String, Void> id = ID.create("Application.Index");
    private final KeyDescriptor<String> descriptor = new EnumeratorStringDescriptor();

    @Override
    public boolean indexDirectories() {
        return false;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file -> {

            if (file.getFileType() == PhpFileType.INSTANCE) {
                return file.getPath().endsWith("/web/index.php");
            }

            return false;
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return false;
    }

    @NotNull
    @Override
    public ID getName() {
        return id;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return fileContent -> {
            final Map<String, Void> map = new THashMap<>();
            return map;
        };
    }

    @NotNull
    @Override
    public KeyDescriptor getKeyDescriptor() {
        return this.descriptor;
    }

    @NotNull
    @Override
    public DataExternalizer getValueExternalizer() {
        return ScalarIndexExtension.VOID_DATA_EXTERNALIZER;
    }

    @Override
    public int getVersion() {
        return 1;
    }
}
