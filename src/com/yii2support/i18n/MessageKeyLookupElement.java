package com.yii2support.i18n;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import org.jetbrains.annotations.NotNull;

/**
 * Created by NVlad on 06.01.2017.
 */
public class MessageKeyLookupElement extends LookupElement {
    private String myMessageKey;

    MessageKeyLookupElement(String messageKey) {
        myMessageKey = messageKey;
    }

    @Override
    public void handleInsert(InsertionContext context) {
        super.handleInsert(context);
    }

    @NotNull
    @Override
    public String getLookupString() {
        return myMessageKey;
    }
}
