package com.nvlad.yii2support.configurations;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static com.nvlad.yii2support.common.PsiUtil.getArrayCreationChild;

public class ComponentsIndex extends FileBasedIndexExtension<String, String> {
    public static final ID<String, String> identity = ID.create("Yii2Support.ComponentsIndex");

    @Override
    public @NotNull ID<String, String> getName() {
        return identity;
    }

    @Override
    public @NotNull DataIndexer<String, String, FileContent> getIndexer() {
        return file -> {
            final HashMap<String, String> result = new HashMap<>();

            PsiFile psiFile = file.getPsiFile();
            if(psiFile instanceof PhpFile){
                PsiElement openTag = psiFile.getFirstChild();
                for(PsiElement child : openTag.getChildren()){
                    if(child instanceof PhpReturn){
                        ArrayCreationExpression arrayExpression = getArrayCreationChild(child);
                        if(arrayExpression != null) {
                            for (ArrayHashElement arrayHashElement : arrayExpression.getHashElements()) {
                                if(arrayHashElement.getKey() == null || arrayHashElement.getValue() == null){
                                    continue;
                                }

                                if(((StringLiteralExpression) arrayHashElement.getKey()).getContents().equals("components")){
                                    for (PsiElement component : arrayHashElement.getValue().getChildren()) {
                                        if(!(component instanceof ArrayHashElement)) {
                                            continue;
                                        }
                                        StringLiteralExpression keyExpr = (StringLiteralExpression)((ArrayHashElement) component).getKey();
                                        PhpPsiElement confArray = ((ArrayHashElement) component).getValue();
                                        if(keyExpr == null || !(confArray instanceof ArrayCreationExpression)){
                                            continue;
                                        }

                                        String componentName = keyExpr.getContents();
                                        for(PsiElement conf : confArray.getChildren()){
                                            if(!(conf instanceof ArrayHashElement)) {
                                                continue;
                                            }

                                            StringLiteralExpression prop = (StringLiteralExpression)((ArrayHashElement) conf).getKey();
                                            if(prop == null){
                                                continue;
                                            }
                                            if(prop.getContents().equals("class")){
                                                PhpPsiElement classValue = ((ArrayHashElement) conf).getValue();
                                                String classFqn;
                                                if(classValue instanceof ClassConstantReference && ((ClassConstantReference) classValue).getClassReference() != null){
                                                    classFqn = ((ClassConstantReference) classValue).getClassReference().getType().toString();
                                                }else if(classValue instanceof StringLiteralExpression){
                                                    classFqn = ((StringLiteralExpression) classValue).getContents();
                                                }else{
                                                    continue;
                                                }
                                                result.put(componentName, classFqn);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return result;
        };
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public @NotNull DataExternalizer<String> getValueExternalizer() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 6;
    }

    @Override
    public @NotNull FileBasedIndex.InputFilter getInputFilter() {
        return file -> (
                file.getFileType() == PhpFileType.INSTANCE
                && file.getUrl().contains("/config/")
                && !file.getUrl().contains("/environments/")
        );
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }
}
