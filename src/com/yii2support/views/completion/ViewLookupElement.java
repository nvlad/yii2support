package com.yii2support.views.completion;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.yii2support.views.ViewsUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by NVlad on 28.12.2016.
 */
class ViewLookupElement extends LookupElement {
    final private PsiFile myFile;
    final private String myName;
    final private String myTail;

    ViewLookupElement(PsiFile psiFile) {
        myFile = psiFile;
        VirtualFile file = psiFile.getVirtualFile();

        if (file.getNameWithoutExtension().contains(".")) {
            myName = file.getName();
            myTail = null;
        } else {
            myName = file.getNameWithoutExtension();
            myTail = "." + file.getExtension();
        }
    }

    @NotNull
    @Override
    public String getLookupString() {
        return myName;
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
        presentation.setIcon(myFile.getIcon(0));
        presentation.setItemText(myName);
        presentation.setItemTextBold(true);
        if (myTail != null) {
            presentation.setTailText(myTail, true);
        }
        presentation.setTypeText("View");
        presentation.setTypeGrayed(true);
    }

    @Override
    public void handleInsert(InsertionContext context) {
        PsiElement element = context.getFile().getViewProvider().findElementAt(context.getSelectionEndOffset());
        element = PsiTreeUtil.getParentOfType(element, StringLiteralExpression.class);
        if (element == null) {
            return;
        }

        String value = ((StringLiteralExpression) element).getContents();
        if (value.contains("/")) {
            value = value.substring(value.lastIndexOf('/') + 1);
        }
        if (!myName.equals(value)) {
            String filename = myFile.getName();
            if (!filename.equals(value)) {
                return;
            }
        }

        MethodReference reference = PsiTreeUtil.getParentOfType(element, MethodReference.class);
        if (reference != null) {
            reference.putUserData(ViewsUtil.RENDER_VIEW, ((StringLiteralExpression) element).getContents());
            reference.putUserData(ViewsUtil.RENDER_VIEW_FILE, myFile);
        }

        ArrayList<String> params = ViewsUtil.getViewVariables(myFile);
        if (params.size() > 0) {
            ParameterList parameterList = (ParameterList) element.getParent();
            if (parameterList.getParameters().length == 1) {
                Project project = context.getProject();
                Template template = TemplateManager.getInstance(project).createTemplate("", "");
                template.addTextSegment(", [");
                boolean addComma = false;
                for (String param : params) {
                    String variableName = "$" + param.toUpperCase() + "$";
                    if (addComma) {
                        template.addTextSegment(", ");
                    }
                    template.addTextSegment("'" + param + "' => ");
                    template.addVariable(variableName, "", "\"$" + param + "\"", true);
                    template.addVariableSegment(variableName);
                    addComma = true;
                }
                template.addTextSegment("]");
                int offset = parameterList.getParameters()[0].getTextRange().getEndOffset();
                context.getEditor().getCaretModel().moveToOffset(offset);
                TemplateManager.getInstance(project).startTemplate(context.getEditor(), template);
            }
        }
    }
}
