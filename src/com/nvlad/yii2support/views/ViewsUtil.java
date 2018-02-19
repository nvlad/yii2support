package com.nvlad.yii2support.views;

import com.intellij.codeInsight.template.macro.SplitWordsMacro;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Created by NVlad on 15.01.2017.
 */
public class ViewsUtil {
    private static final Key<PsiDirectory> VIEWS_DIRECTORY = Key.create("views.directory");
    private static final Key<Long> VIEWS_DIRECTORY_MODIFIED = Key.create("views.directory.modified");
    private static final Key<PsiDirectory> VIEWS_CONTEXT_DIRECTORY = Key.create("views.context.directory");

    public static PsiDirectory getRootDirectory(PsiElement element) {
        final PsiFile file = element.getContainingFile();

        if (file.getUserData(VIEWS_DIRECTORY) != null) {
            Long modified = file.getUserData(VIEWS_DIRECTORY_MODIFIED);
            if (modified != null && modified == file.getModificationStamp()) {
                return file.getUserData(VIEWS_DIRECTORY);
            }
        }

        return findDirectory(element);
    }

    public static PsiDirectory getContextDirectory(PsiElement element) {
        final PsiFile file = element.getContainingFile();

        if (file.getUserData(VIEWS_CONTEXT_DIRECTORY) != null) {
            Long modified = file.getUserData(VIEWS_DIRECTORY_MODIFIED);
            if (modified != null && modified == file.getModificationStamp()) {
                return file.getUserData(VIEWS_CONTEXT_DIRECTORY);
            }
        }

        findDirectory(element);

        return file.getUserData(VIEWS_CONTEXT_DIRECTORY);
    }

    @Nullable
    private static PsiDirectory findDirectory(PsiElement element) {
        final PhpClass phpClass = PsiTreeUtil.getParentOfType(element, PhpClass.class);
        final PsiFile file = element.getContainingFile();
        if (phpClass != null) {
            return findClassDirectory(phpClass, file);
        }

        PsiDirectory context = file.getOriginalFile().getContainingDirectory();
        if (context != null) {
            PsiDirectory root = context.getParentDirectory();
            while (root != null && !root.getName().equals("views")) {
                root = root.getParentDirectory();
            }

            file.putUserData(VIEWS_CONTEXT_DIRECTORY, context);
            file.putUserData(VIEWS_DIRECTORY, root);
            file.putUserData(VIEWS_DIRECTORY_MODIFIED, file.getModificationStamp());

            return root;
        }

        return null;
    }

    @Nullable
    private static PsiDirectory findClassDirectory(PhpClass phpClass, PsiFile file) {
        if (phpClass != null) {
            Method getViewPath = phpClass.findMethodByName("getViewPath");
            PhpClass ownClass = phpClass.getSuperClass();
            PsiDirectory directory = file.getOriginalFile().getContainingDirectory();
            if (directory != null) {
                while (ownClass != null) {
                    if (getViewPath != null) {
                        getViewPath = ownClass.findMethodByName("getViewPath");
                    }
                    switch (ownClass.getFQN()) {
                        case "\\yii\\base\\Controller":
                            directory = directory.getParentDirectory();
                            if (directory == null) {
                                return null;
                            }
                            if (getViewPath != null && Objects.equals(getViewPath.getContainingClass(), ownClass)) {
                                directory = directory.findSubdirectory("views");
                            }

                            if (directory != null) {
                                file.putUserData(VIEWS_DIRECTORY_MODIFIED, file.getModificationStamp());
                                file.putUserData(VIEWS_DIRECTORY, directory);

                                String controllerId = phpClass.getName();
                                controllerId = controllerId.substring(0, controllerId.length() - 10);
                                controllerId = new SplitWordsMacro.LowercaseAndDash().convertString(controllerId);

                                PsiDirectory context = directory.findSubdirectory(controllerId);
                                if (context != null) {
                                    file.putUserData(VIEWS_CONTEXT_DIRECTORY, context);
                                }
                            }

                            return directory;
                        case "\\yii\\base\\Widget":
                            directory = directory.findSubdirectory("views");
                            if (directory != null) {
                                file.putUserData(VIEWS_DIRECTORY_MODIFIED, file.getModificationStamp());
                                file.putUserData(VIEWS_DIRECTORY, directory);
                                file.putUserData(VIEWS_CONTEXT_DIRECTORY, directory);
                            }

                            return directory;
                    }
                    ownClass = ownClass.getSuperClass();
                }
            }
        }

        return null;
    }
}
