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
            PsiDirectory dir = myElement.getContainingFile().getContainingDirectory();
            PhpClass phpClass = ObjectFactoryUtils.findClassByArrayCreation(arrayCreation, dir);

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
}
