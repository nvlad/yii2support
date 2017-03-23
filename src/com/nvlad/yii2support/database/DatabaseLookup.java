package com.nvlad.yii2support.database;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.openapi.editor.Document;
import org.jetbrains.annotations.NotNull;

/**
 * Created by oleg on 23.03.2017.
 */
public class DatabaseLookup extends LookupElement {
    DatabaseLookup(String columnName, String type) {

    }

    @NotNull
    @Override
    public String getLookupString() {

        return "string";
    }


    @Override
    public void renderElement(LookupElementPresentation presentation) {
        /*
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
        */

    }

    @Override
    public void handleInsert(InsertionContext context) {
        super.handleInsert(context);

        Document document = context.getDocument();
        int insertPosition = context.getSelectionEndOffset();

        /*
        if (myElement.getParent().getParent() instanceof ArrayCreationExpression) {
            document.insertString(insertPosition + 1, " => ");
            insertPosition += 5;

            context.getEditor().getCaretModel().getCurrentCaret().moveToOffset(insertPosition);
        }
        */
    }
}
