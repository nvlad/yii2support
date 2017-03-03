package com.nvlad.yii2support.objectfactory;


import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.openapi.editor.Document;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocParamTag;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
import org.jetbrains.annotations.NotNull;

public class ObjectFactoryFieldLookupElement extends LookupElement {
    private PhpExpression myElement;
    private Field myField;

    ObjectFactoryFieldLookupElement(PhpExpression element, Field field) {
        myElement = element;
        myField = field;
    }

    @NotNull
    @Override
    public String getLookupString() {
        if (myElement instanceof ConstantReference) {
            return "'" + myField.getName() + "'";
        }

        return myField.getName();
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        presentation.setIcon(myField.getIcon());
        presentation.setItemText(myField.getName());
        presentation.setItemTextBold(true);

        presentation.setTypeText(myField.getType().toString());
        presentation.setTypeGrayed(true);

        PhpDocComment docComment = myField.getDocComment();
        if (docComment != null) {
            PhpDocParamTag paramTag = docComment.getVarTag();
            if (paramTag != null) {
                presentation.setTailText(" " + paramTag.getTagValue(), true);
            }
        }

    }

    @Override
    public void handleInsert(InsertionContext context) {
        super.handleInsert(context);

        Document document = context.getDocument();
        int insertPosition = context.getSelectionEndOffset();

        if (myElement instanceof ArrayCreationExpression) {
            document.insertString(insertPosition + 1, " => ");
            insertPosition += 5;

            context.getEditor().getCaretModel().getCurrentCaret().moveToOffset(insertPosition);
        }
    }
}