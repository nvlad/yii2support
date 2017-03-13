package com.nvlad.yii2support.objectfactory;


import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.openapi.editor.Document;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocParamTag;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import org.jetbrains.annotations.NotNull;

public class ObjectFactoryMethodLookupElement extends LookupElement {
    private PhpExpression myElement;
    private Method myMethod;

    ObjectFactoryMethodLookupElement(PhpExpression element, Method method) {
        myElement = element;
        myMethod = method;
    }

    @NotNull
    @Override
    public String getLookupString() {
        return getAsPropertyName();
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        presentation.setIcon(myMethod.getIcon());
        String name = myMethod.getName();
        if (name.length() > 3){
            name =  Character.toLowerCase(name.charAt(3)) + name.substring(4);
        }
        presentation.setItemText(name);
        presentation.setItemTextBold(true);

        if (myMethod.getParameters().length > 0) {
            PhpType paramType = myMethod.getParameters()[0].getType();
            if(paramType.isComplete()) {
                presentation.setTypeText(paramType.toString());
            }
        }
        presentation.setTypeGrayed(true);

        PhpDocComment docComment = myMethod.getDocComment();
        if (docComment != null) {
            PhpDocParamTag paramTag = docComment.getVarTag();
            if (paramTag != null) {
                presentation.setTailText(" " + paramTag.getTagValue(), true);
            }
        }

    }

    public String getAsPropertyName() {
        String methodName = myMethod.getName();
        String propertyName = methodName.substring(3);
        propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
        return propertyName;
    }

    @Override
    public void handleInsert(InsertionContext context) {
        super.handleInsert(context);

        Document document = context.getDocument();
        int insertPosition = context.getSelectionEndOffset();

        if (myElement.getParent().getParent() instanceof ArrayCreationExpression) {
            document.insertString(insertPosition + 1, " => ");
            insertPosition += 5;

            context.getEditor().getCaretModel().getCurrentCaret().moveToOffset(insertPosition);
        }
    }
}