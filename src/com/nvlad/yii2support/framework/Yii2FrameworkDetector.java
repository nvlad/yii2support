package com.nvlad.yii2support.framework;

import com.intellij.framework.FrameworkType;
import com.intellij.framework.detection.DetectedFrameworkDescription;
import com.intellij.framework.detection.FileContentPattern;
import com.intellij.framework.detection.FrameworkDetectionContext;
import com.intellij.framework.detection.FrameworkDetector;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileContent;
import com.jetbrains.php.lang.PhpFileType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created by NVlad on 07.02.2017.
 */
public class Yii2FrameworkDetector extends FrameworkDetector {
    protected Yii2FrameworkDetector() {
        super("Yii2Framework", 5);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return PhpFileType.INSTANCE;
    }

    @NotNull
    @Override
    public ElementPattern<FileContent> createSuitableFilePattern() {
        return FileContentPattern.fileContent().withName("yii").with(new PatternCondition<FileContent>(null) {
            @Override
            public boolean accepts(@NotNull FileContent fileContent, ProcessingContext processingContext) {
                if (Objects.equals(fileContent.getFile().getParent(), fileContent.getProject().getBaseDir())) {
                    System.out.println("createSuitableFilePattern: " + fileContent.getFile().getPath());
                }

                return Objects.equals(fileContent.getFile().getParent(), fileContent.getProject().getBaseDir());
            }
        });
    }

    @Override
    public List<? extends DetectedFrameworkDescription> detect(@NotNull Collection<VirtualFile> collection, @NotNull FrameworkDetectionContext frameworkDetectionContext) {
        System.out.println("Yii2FrameworkDetector::fount new files - " + collection.size());

        if (collection.size() > 0) {
            return Collections.singletonList(new Yii2DetectedFrameworkDescription(collection));
        }

        return Collections.emptyList();
    }

    @Override
    public FrameworkType getFrameworkType() {
        return Yii2FrameworkType.INSTANCE;
    }

    public class Yii2DetectedFrameworkDescription extends DetectedFrameworkDescription {
        private final Collection<VirtualFile> myNewFiles;

        Yii2DetectedFrameworkDescription(Collection<VirtualFile> newFiles) {
            myNewFiles = newFiles;
        }

        @NotNull
        @Override
        public Collection<? extends VirtualFile> getRelatedFiles() {
            return myNewFiles;
        }

        @NotNull
        @Override
        public String getSetupText() {
            return "Yii2Framework";
        }

        @NotNull
        @Override
        public FrameworkDetector getDetector() {
            return Yii2FrameworkDetector.this;
        }

        @Override
        public void setupFramework(@NotNull ModifiableModelsProvider modifiableModelsProvider, @NotNull ModulesProvider modulesProvider) {

        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Yii2DetectedFrameworkDescription && myNewFiles.equals(((Yii2DetectedFrameworkDescription) o).myNewFiles);
        }

        @Override
        public int hashCode() {
            return myNewFiles.hashCode();
        }
    }
}
