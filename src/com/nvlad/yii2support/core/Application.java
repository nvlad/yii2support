package com.nvlad.yii2support.core;

import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.PhpFile;

/**
 * Created by NVlad on 03.02.2017.
 */
public class Application {
    private String myPath;

    public Application(PsiFile file) {
        myPath = file.getParent().getParent().getVirtualFile().getCanonicalPath();
    }

    public String getPath() {
        return myPath;
    }
}
