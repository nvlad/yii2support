package com.nvlad.yii2support.objectfactory;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.nvlad.yii2support.views.ViewsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by oleg on 14.03.2017.
 */
public class ObjectFactoryReference extends PsiReferenceBase<PsiElement> {
    ObjectFactoryReference(@NotNull PsiElement element)
    {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        PsiElement possibleArrayCreation = myElement.getParent().getParent().getParent();
        if (possibleArrayCreation instanceof ArrayCreationExpression) {
            ArrayCreationExpression  arrayCreation = (ArrayCreationExpression)possibleArrayCreation;
            PhpClass phpClass = ObjectFactoryUtils.findClassByArray(arrayCreation);
            if (phpClass == null) {
                phpClass = ObjectFactoryUtils.getPhpClassByYiiCreateObject(arrayCreation);
            }
            if (phpClass == null) {
                PsiDirectory dir = myElement.getContainingFile().getContainingDirectory();
                phpClass = ObjectFactoryUtils.getPhpClassInConfig(dir, arrayCreation);
            }

            if (phpClass == null) {
                phpClass = ObjectFactoryUtils.getPhpClassInWidget(arrayCreation);
            }
            if (phpClass == null) {
                phpClass = ObjectFactoryUtils.getPhpClassInGridColumns(arrayCreation);
            }

            if (phpClass != null) {
                PsiElement field = ClassUtils.findField(phpClass, myElement.getText());
                return field;
            }

        }
        return null;
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
            while (dir != null && !(dir.equals(root) || dir.equals(context))) {
                fileName = dir.getName() + "/" + fileName;
                dir = dir.getParent();
            }

            if (dir == null) {
                return null;
            }

            if (dir.equals(root)) {
                fileName = "/" + fileName;
            }
        }
        fileName = string.isSingleQuote() ? "'" + fileName + "'" : "\"" + fileName + "\"";
        newValue = PhpPsiElementFactory.createFromText(element.getProject(), StringLiteralExpression.class, fileName);

        if (newValue != null) {
            string.replace(newValue);
        }

        for (MethodReference reference : PsiTreeUtil.findChildrenOfType(file, MethodReference.class)) {
            if (reference.getName() != null && ArrayUtil.contains(reference.getName(), ViewsUtil.renderMethods)) {
                reference.putUserData(ViewsUtil.RENDER_VIEW_FILE, null);
            }
        }

        return newValue;
    }
}
