package com.nvlad.yii2support.common;

import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.StubIndexKey;

public interface YiiIndexKeys {
    StubIndexKey<String, PsiFile> VIEW = StubIndexKey.createIndexKey("yii2support.view.index");
}
