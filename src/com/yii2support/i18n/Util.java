package com.yii2support.i18n;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by NVlad on 06.01.2017.
 */
public class Util {
    static PsiElement[] getCategories(StringLiteralExpression element) {
        ArrayList<PsiElement> categories = new ArrayList<>();

        PsiDirectory directory = getDirectory(element);
        if (directory != null) {
            Collections.addAll(categories, directory.getFiles());
        }

        return categories.toArray(new PsiElement[categories.size()]);
    }

    static String[] getMessageKeys(StringLiteralExpression element, String category) {
        ArrayList<String> messageKeys = new ArrayList<>();

        PsiDirectory directory = getDirectory(element);
        if (directory != null) {
            PsiFile file = directory.findFile(category.concat(".php"));
            if (file != null) {
                messageKeys.addAll(loadMessageKeysFromFile(file));
            }
        }

        return messageKeys.toArray(new String[messageKeys.size()]);
    }

    public String[] getMessagePointers(String category, String message) {
        return new String[0];
    }

    private static PsiDirectory getDirectory(PsiElement element) {
        PsiFile file = element.getContainingFile().getOriginalFile();
        String filename = file.getName();
        PsiDirectory directory = file.getParent();

        filename = filename.substring(0, filename.lastIndexOf("."));

        if (directory != null) {
            if (filename.endsWith("Controller")) {
                directory = directory.getParentDirectory();
            } else {
                PsiDirectory messageParent = directory.findSubdirectory("messages");
                while (messageParent == null) {
                    directory = directory.getParentDirectory();
                    if (directory == null) {
                        break;
                    }
                    messageParent = directory.findSubdirectory("messages");
                }
            }
        }
        if (directory != null) {
            directory = directory.findSubdirectory("messages");
            if (directory != null && directory.getSubdirectories().length > 0) {
                directory = directory.getSubdirectories()[0];
                return directory;
            }
        }

        return null;
    }

    private static Collection<String> loadMessageKeysFromFile(PsiFile file) {
        ArrayList<String> result = new ArrayList<>();

        GroupStatement groupStatement = (GroupStatement) file.getFirstChild();
        ArrayHashElement returnArray;
        for (PsiElement element : groupStatement.getChildren()) {
            if (element instanceof PhpReturn) {
                if (((PhpReturn) element).getFirstPsiChild() instanceof ArrayCreationExpression) {
                    ArrayCreationExpression array = (ArrayCreationExpression) ((PhpReturn) element).getFirstPsiChild();
                    for (ArrayHashElement hashElement : array.getHashElements()) {
                        if (hashElement.getKey() instanceof StringLiteralExpression) {
                            result.add(((StringLiteralExpression) hashElement.getKey()).getContents());
                        }
                    }
                }

                break;
            }
        }

        return result;
    }
}
