package com.yii2support.i18n;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by NVlad on 06.01.2017.
 */
public class MessageLookupElement extends LookupElement {
    private StringLiteralExpression myElement;
    private ArrayHashElement myMessage;

    MessageLookupElement(StringLiteralExpression element, ArrayHashElement message) {
        myElement = element;
        myMessage = message;
    }

    @NotNull
    @Override
    public String getLookupString() {
        StringLiteralExpression key = ((StringLiteralExpression) myMessage.getKey());
        if (key != null) {
            return key.getContents();
        }
        return "";
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        super.renderElement(presentation);

        presentation.setItemText(((StringLiteralExpression) myMessage.getKey()).getContents());
        presentation.setIcon(myMessage.getKey().getIcon(0));

        if (myMessage.getValue() instanceof StringLiteralExpression) {
            presentation.setTypeText(((StringLiteralExpression) myMessage.getValue()).getContents());
            presentation.setTypeGrayed(true);
        }
        if (myMessage.getValue() instanceof ArrayCreationExpression) {
            presentation.setTypeText("Array()");
        }
    }

    @Override
    public void handleInsert(InsertionContext context) {
        super.handleInsert(context);

        if (myMessage.getValue() instanceof StringLiteralExpression) {
            ArrayList<String> matches = new ArrayList<>();

            StringLiteralExpression value = (StringLiteralExpression) myMessage.getValue();

            Pattern pointers = Pattern.compile("\\{([\\w\\d]+)[},]", Pattern.MULTILINE);
            Matcher matcher = pointers.matcher(value.getContents());
            while (matcher.find()) {
                String match = matcher.group(1);

                if (!matches.contains(match)) {
                    matches.add(match);
                }
            }

            if (matches.size() > 0) {
                ParameterList parameterList = (ParameterList) myElement.getParent();
                if (parameterList.getParameters().length == 2) {

                    String params = "";
                    if (matches.size() == 1 && matches.get(0).equals("0")) {
                        params = ", []";
                    } else {
                        for (String match : matches) {
                            if (params.length() > 0) {
                                params = params + ", ";
                            }
                            params = params.concat("'" + match + "' => ");
                        }
                        params = ", [" + params + "]";
                    }

                    context.getDocument().insertString(context.getSelectionEndOffset() + 1, params);
                }
                context.getDocument();
            }
        }
    }

}
