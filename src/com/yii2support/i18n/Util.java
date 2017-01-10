package com.yii2support.i18n;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by NVlad on 06.01.2017.
 */
class Util {
    @NotNull
    static PsiElement[] getCategories(StringLiteralExpression element) {
        ArrayList<PsiElement> categories = new ArrayList<>();

        PsiDirectory directory = getDirectory(element);
        if (directory != null) {
            Collections.addAll(categories, directory.getFiles());
        }

        return categories.toArray(new PsiElement[categories.size()]);
    }

    @NotNull
    static ArrayHashElement[] getMessages(StringLiteralExpression element, String category) {
        ArrayList<ArrayHashElement> messages = new ArrayList<>();

        PsiDirectory directory = getDirectory(element);
        if (directory != null) {
            PsiFile file = directory.findFile(category.concat(".php"));
            if (file != null) {
                messages.addAll(loadMessagesFromFile(file));
            }
        }

        return messages.toArray(new ArrayHashElement[messages.size()]);
    }
//
//    public String[] getMessagePointers(String category, String message) {
//        return new String[0];
//    }

    @NotNull
    static String PhpExpressionValue(PhpExpression expression) {
        if (expression instanceof StringLiteralExpression) {
            return ((StringLiteralExpression) expression).getContents();
        }
        if (expression instanceof ConstantReference) {
            Constant constant = (Constant) ((ConstantReference) expression).resolve();
            if (constant != null) {
                return PhpExpressionValue((PhpExpression) constant.getValue());
            }
        }
        if (expression instanceof ClassConstantReference) {
            ClassReference classReference = (ClassReference) ((ClassConstantReference) expression).getClassReference();
            if (classReference != null) {
                PhpClass phpClass = (PhpClass) classReference.resolve();
                if (phpClass != null) {
                    Field field = phpClass.findFieldByName(expression.getName(), true);
                    if (field != null) {
                        return PhpExpressionValue((PhpExpression) field.getDefaultValue());
                    }
                }
            }
        }
        if (expression instanceof Variable) {
            PhpExpression variable = (PhpExpression) ((Variable) expression).resolve();

            if (variable != null && variable.getContext() instanceof AssignmentExpression) {
                AssignmentExpression assignmentExpression = (AssignmentExpression) variable.getContext();
                return PhpExpressionValue((PhpExpression) assignmentExpression.getValue());
            }
        }
        if (expression instanceof ConcatenationExpression) {
            ConcatenationExpression concatenation = (ConcatenationExpression) expression;
            return PhpExpressionValue((PhpExpression) concatenation.getLeftOperand()) + PhpExpressionValue((PhpExpression) concatenation.getRightOperand());
        }
        String expressionType = expression.getType().toString();
        if (expressionType.equals("int") || expressionType.equals("float")) {
            return expression.getText();
        }

        return "";
    }

    @Nullable
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

    private static Collection<ArrayHashElement> loadMessagesFromFile(PsiFile file) {
        ArrayList<ArrayHashElement> result = new ArrayList<>();

        GroupStatement groupStatement = (GroupStatement) file.getFirstChild();
        for (PsiElement element : groupStatement.getChildren()) {
            if (element instanceof PhpReturn) {
                if (((PhpReturn) element).getFirstPsiChild() instanceof ArrayCreationExpression) {
                    ArrayCreationExpression array = (ArrayCreationExpression) ((PhpReturn) element).getFirstPsiChild();
                    if (array != null) {
                        for (ArrayHashElement hashElement : array.getHashElements()) {
                            result.add(hashElement);
                        }
                    }
                }

                break;
            }
        }

        return result;
    }
}
