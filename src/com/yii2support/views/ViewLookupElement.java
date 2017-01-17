package com.yii2support.views;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by NVlad on 28.12.2016.
 */
public class ViewLookupElement extends LookupElement {
    private PsiFile myFile;
    private String name;
    private String tail;

    ViewLookupElement(PsiFile psiFile) {
        myFile = psiFile;
        String filename = psiFile.getName().substring(0, psiFile.getName().lastIndexOf("."));
        if (filename.contains(".")) {
            name = psiFile.getName();
        } else {
            name = filename;
            tail = psiFile.getName().substring(filename.length());
        }
    }

    @NotNull
    @Override
    public String getLookupString() {
        return name;
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        presentation.setIcon(myFile.getIcon(0));
        presentation.setItemText(name);
        presentation.setItemTextBold(true);
        if (tail != null) {
            presentation.setTailText(tail, true);
        }
        presentation.setTypeText("View");
        presentation.setTypeGrayed(true);
    }

    @Override
    public void handleInsert(InsertionContext context) {
        ArrayList<String> params = ViewsUtil.getViewVariables(myFile);
        if (params.size() > 0) {
            PsiElement element = context.getFile().getViewProvider().findElementAt(context.getSelectionEndOffset());
            element = PsiTreeUtil.getParentOfType(element, StringLiteralExpression.class);
            if (element != null) {
                ParameterList parameterList = (ParameterList) element.getParent();
                if (parameterList.getParameters().length == 1) {
                    Project project = context.getProject();
                    Template template = TemplateManager.getInstance(project).createTemplate("", "");
                    template.addTextSegment(", [");
                    for (String param : params) {
                        String variableName = "$" + param.toUpperCase() + "$";
                        template.addVariable(variableName, "", "\"$" + param + "\"", true);
                        template.addTextSegment("'" + param + "' => ");
                        template.addVariableSegment(variableName);
                    }
                    template.addTextSegment("]");
                    int offset = parameterList.getParameters()[0].getTextRange().getEndOffset();
                    context.getEditor().getCaretModel().moveToOffset(offset);
                    TemplateManager.getInstance(project).startTemplate(context.getEditor(), template);
                }
            }
        }
    }
}
