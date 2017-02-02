package com.yii2support.views.references;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.yii2support.views.ViewsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by NVlad on 02.01.2017.
 */
public class PsiReference extends PsiReferenceBase<PsiElement> {
    PsiReference(@NotNull PsiElement element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return ViewsUtil.getViewFile(myElement);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        final StringLiteralExpression string = (StringLiteralExpression) this.getElement();
        final PsiDirectory context = ViewsUtil.getContextDirectory(string);
        final PsiFile file = (PsiFile) element;
        final PsiElement newValue;

        String fileName = string.getContents();
        if (fileName.contains("/")) {
            fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        }
        if (!file.getContainingDirectory().equals(context)) {
            final PsiDirectory root = ViewsUtil.getRootDirectory(string);
            if (root == null) {
                return null;
            }

            PsiDirectory dir = file.getContainingDirectory();
            while (dir != null && !dir.equals(root)) {
                fileName = dir.getName() + "/" + fileName;
                dir = dir.getParent();
            }

            if (dir != null && dir.equals(root)) {
                fileName = "/" + fileName;
            }
        }
        fileName = string.isSingleQuote() ? "'" + fileName + "'" : "\"" + fileName + "\"";
        newValue = PhpPsiElementFactory.createFromText(element.getProject(), StringLiteralExpression.class, fileName);

        if (newValue != null) {
            string.replace(newValue);
        }
        return newValue;
    }
}
