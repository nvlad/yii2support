package com.yii2support.i18n;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ArrayHashElement;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.PhpExpression;
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

        if (myMessage.getKey() instanceof StringLiteralExpression) {
            presentation.setItemText(((StringLiteralExpression) myMessage.getKey()).getContents());
            presentation.setIcon(myMessage.getKey().getIcon(0));
        }

        PhpExpression value = (PhpExpression) myMessage.getValue();
        if (value != null) {
            if (value instanceof StringLiteralExpression) {
                presentation.setTailText(" = " + ((StringLiteralExpression) value).getContents(), true);
            }

            presentation.setTypeText(value.getType().toString());
            presentation.setTypeGrayed(true);
        }
    }

    @Override
    public void handleInsert(InsertionContext context) {
        super.handleInsert(context);

        int suffixLength = myElement.getText().length() - 21 - myElement.getText().lastIndexOf("IntellijIdeaRulezzz ");
        int blockStart = context.getSelectionEndOffset();
        context.getDocument().deleteString(blockStart, blockStart + suffixLength);

        if (myMessage.getValue() instanceof StringLiteralExpression) {
            ArrayList<String> matches = new ArrayList<>();

            StringLiteralExpression value = (StringLiteralExpression) myMessage.getValue();

            Pattern pointers = Pattern.compile("\\{([\\w\\d]+)[},]", Pattern.MULTILINE | Pattern.UNICODE_CASE);
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
            } else {
                cleanParams(context);
            }
        } else {
            cleanParams(context);
        }
    }

    @Override
    public AutoCompletionPolicy getAutoCompletionPolicy() {
        return AutoCompletionPolicy.GIVE_CHANCE_TO_OVERWRITE;
    }

    private void cleanParams(InsertionContext context) {
        ParameterList parameterList = (ParameterList) myElement.getParent();
        if (parameterList.getParameters().length == 3) {
            PsiElement[] parameters = parameterList.getParameters();
            int blockStart = context.getSelectionEndOffset() + 1;
            int paramSpace = parameters[2].getTextRange().getStartOffset() - parameters[1].getTextRange().getEndOffset();
            int blockLength = parameters[2].getTextLength() + paramSpace;
            context.getDocument().deleteString(blockStart, blockStart + blockLength);
        }
    }
}
