package com.nvlad.yii2support.database;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocCommentImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocPropertyTagImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocPropertyTag;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oleg on 06.04.2017.
 */
public class MissingPropertiesQuickFix   implements LocalQuickFix {

    private ArrayList<String[]> missingProperties;
    private PhpDocComment comment;

    public MissingPropertiesQuickFix(ArrayList<String[]> missingProperties, PhpDocComment comment) {
        this.missingProperties = missingProperties;
        this.comment = comment;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Fix missing properties";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        List<PhpDocPropertyTag> propertyTags = this.comment.getPropertyTags();


            Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            Document document = editor.getDocument();
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document);
            TemplateManager templateManager = TemplateManager.getInstance(project);
            Template template = templateManager.createTemplate("", "");
            template.setToReformat(true);
            for (String[] missingProperty: this.missingProperties)
            {
                String propertyText = "* @property "+ missingProperty[1] + ' $' + missingProperty[0];
                if (missingProperty.length > 2 && missingProperty[2] != null) {
                    propertyText += " " + missingProperty[2];
                }
                template.addTextSegment(propertyText + "\n");
            }

            int offset =  comment.getLastChild().getTextOffset();
            if (comment.getPropertyTags().size() > 0) {
                PhpDocPropertyTag phpDocPropertyTag = comment.getPropertyTags().get(comment.getPropertyTags().size() - 1);
                offset = phpDocPropertyTag.getTextOffset() + phpDocPropertyTag.getTextLength();
            }
            editor.getCaretModel().moveToOffset(offset);
            comment.getPropertyTags().add(new PhpDocPropertyTagImpl(comment.getNode()));
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document);
            templateManager.startTemplate(editor, template);


    }
}
