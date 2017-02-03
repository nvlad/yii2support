package com.nvlad.yii2support.views.inspections;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Created by NVlad on 15.01.2017.
 */
class RequireParameterLocalQuickFix implements LocalQuickFix {
    final private String[] myVariables;

    RequireParameterLocalQuickFix(Collection<String> variables) {
        myVariables = variables.toArray(new String[variables.size()]);
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        if (myVariables.length == 1) {
            return "Add \"%param%\" parameter".replace("%param%", myVariables[0]);
        }

        return "Add View parameter(s)";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Add View parameter";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        ParameterList parameterList = ((MethodReference) descriptor.getPsiElement()).getParameterList();
        if (parameterList == null) {
            return;
        }

        PsiElement[] parameters = parameterList.getParameters();
        if (parameters.length == 1) {
            ArrayCreationExpression params = PhpPsiElementFactory.createFromText(project, ArrayCreationExpression.class, "[]");
            if (params == null) {
                return;
            }
            parameterList.add(PhpPsiElementFactory.createComma(project));
            parameterList.add(params);
            parameters = parameterList.getParameters();
        }

        TemplateManager templateManager = TemplateManager.getInstance(project);
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            return;
        }

        if (parameters[1] instanceof FunctionReference) {
            FunctionReference functionReference = (FunctionReference) parameters[1];
            if (functionReference.getName() == null || !functionReference.getName().equals("compact")) {
                return;
            }

            Template template = templateManager.createTemplate("", "");
            template.setToReformat(true);

            Boolean firstElement = functionReference.getParameters().length == 0;
            for (String variable : myVariables) {
                template.addTextSegment(firstElement ? "'" : ", '");
                String var = "$" + variable.toUpperCase() + "$";
                template.addVariable(var, "", "\"" + variable + "\"", true);
                template.addVariableSegment(var);
                template.addTextSegment("'");
                firstElement = false;
            }

            PsiElement psiElement = functionReference.getParameterList();
            if (psiElement != null) {
                editor.getCaretModel().moveToOffset(psiElement.getTextRange().getEndOffset());
                PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());
                templateManager.startTemplate(editor, template);
            }

            return;
        }

        if (!(parameters[1] instanceof ArrayCreationExpression)) {
            return;
        }
        ArrayCreationExpression params = (ArrayCreationExpression) parameters[1];

        Template template = templateManager.createTemplate("", "");
        template.setToReformat(true);
        Boolean addComma = params.getHashElements().iterator().hasNext();
        Boolean newLined = false;

        PsiElement psiElement = params.getLastChild();
        while (psiElement instanceof PsiWhiteSpace || psiElement.getText().equals("]")) {
            if (psiElement instanceof PsiWhiteSpace && psiElement.getText().contains("\n")) {
                newLined = true;
            }
            psiElement = psiElement.getPrevSibling();
        }
        if (psiElement.getText().equals(",")) {
            addComma = false;
        }

        for (String variable : myVariables) {
            if (addComma) {
                template.addTextSegment(",");
            }
            template.addTextSegment((newLined ? "\n" : " "));
            String templateVariable = "$" + variable.toUpperCase() + "$";
            template.addVariable(templateVariable, "", "\"$" + variable + "\"", true);
            template.addTextSegment("'" + variable + "' => ");
            template.addVariableSegment(templateVariable);
            addComma = true;
        }
        template.addTextSegment(newLined ? "," : " ");

        editor.getCaretModel().moveToOffset(psiElement.getTextRange().getEndOffset());
        PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());
        templateManager.startTemplate(editor, template);
    }
}
