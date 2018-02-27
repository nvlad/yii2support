package com.nvlad.yii2support.database;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.DatabaseUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by oleg on 31.03.2017.
 */
public class MissedParamQuickFix  implements LocalQuickFix {
    MethodReference methodReference;


    public MissedParamQuickFix(MethodReference methodRef ) {
        methodReference = methodRef;
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return getFamilyName();
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Conform parameters to condition";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        Method method = (Method)methodReference.resolve();
        if (method != null) {
          Parameter[] parameters = method.getParameters();
          int paramParameterIndex = ClassUtils.getParamIndex(method, "params");
          int conditionParameterIndex = ClassUtils.getParamIndex(method, new String[]{ "condition", "expression", "sql"});

          if (paramParameterIndex > -1
                  && conditionParameterIndex > -1
                  && conditionParameterIndex == paramParameterIndex - 1
                  && methodReference.getParameters().length > conditionParameterIndex) {
              String condition = methodReference.getParameters()[conditionParameterIndex].getText();
              String[] conditionParams = DatabaseUtils.extractParamsFromCondition(condition);

              ArrayCreationExpression array = null;
              if (methodReference.getParameters().length > paramParameterIndex) {
                  PsiElement paramParameter = methodReference.getParameters()[paramParameterIndex];
                  if (paramParameter instanceof ArrayCreationExpression)
                      array = (ArrayCreationExpression)paramParameter;
                  paramParameter.delete();
              }

              TemplateManager templateManager = TemplateManager.getInstance(project);
              Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
              if (editor == null || methodReference.getParameterList() == null) return;

              editor.getCaretModel().moveToOffset(methodReference.getParameterList().getLastChild().getTextRange().getEndOffset());
              PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());
              Template template = templateManager.createTemplate("", "");
              template.setToReformat(true);
              buildParamArray(template, conditionParams, array, conditionParameterIndex);

              PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());
              templateManager.startTemplate(editor, template);
          }
        }

    }

    private Template buildParamArray(Template template, String[] conditionParams, ArrayCreationExpression array, int conditionParameterIndex) {
        if (methodReference.getParameterList() != null
                && (methodReference.getParameterList().getParameters()[conditionParameterIndex].getNextSibling() == null ||
                ! methodReference.getParameterList().getParameters()[conditionParameterIndex].getNextSibling().getText().equals(",")))
            template.addTextSegment(", ");

        template.addTextSegment("[");
        String separator = " ";
        if (conditionParams.length > 1)
            separator = "\n";
        Boolean addComma = false;
        for (String variable : conditionParams) {
            if (addComma) {
                template.addTextSegment(",");
            }
            template.addTextSegment(separator);
            String templateVariable = "$" + variable.toUpperCase() + "$";
          //  template.addVariable(templateVariable, "", "'variable'", true);
            String value = getArrayValueByHash(variable, array);
            //if (value != null)
            //    template.addVariable("test", value, true);
            String valueStr = value == null ? "''" : value;
            template.addTextSegment("'" + variable + "' => " + valueStr);
          //  template.addVariableSegment(templateVariable);
            addComma = true;
        }
        template.addTextSegment(separator + "]");
        return template;
    }

    private String getArrayValueByHash(String hash, ArrayCreationExpression array) {
        if (array == null)
            return null;
        for (ArrayHashElement el: array.getHashElements()) {
            PsiElement key = el.getKey();
            PsiElement value = el.getValue();

            if (key != null && value != null && ClassUtils.removeQuotes(key.getText()).equals(hash)) {
                return value.getText();
            }
        }
        return null;
    }


}
