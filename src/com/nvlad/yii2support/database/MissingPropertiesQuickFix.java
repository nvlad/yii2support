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
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocProperty;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocCommentImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.tags.PhpDocPropertyTagImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocPropertyTag;
import com.nvlad.yii2support.common.VirtualProperty;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oleg on 06.04.2017.
 */
public class MissingPropertiesQuickFix   implements LocalQuickFix {

    private ArrayList<VirtualProperty> missingProperties;
    private PhpDocComment comment;

    public MissingPropertiesQuickFix(ArrayList<VirtualProperty> missingProperties, PhpDocComment comment) {
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
            if (editor == null)
                return;
            Document document = editor.getDocument();
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document);
            TemplateManager templateManager = TemplateManager.getInstance(project);
            Template template = templateManager.createTemplate("", "");
            template.setToReformat(true);
            for (VirtualProperty missingProperty: this.missingProperties)
            {

                String propertyText = "* @property "+ (missingProperty.getType() != null ? missingProperty.getType() : "") + " $" +missingProperty.getName();
                if ( missingProperty.getComment() != null) {
                    propertyText += " " + missingProperty.getComment();
                }
                template.addTextSegment("\n" + propertyText);
            }
            template.addTextSegment("\n");

            int offset =  comment.getLastChild().getTextOffset();
            if (propertyTags.size() > 0) {
                PhpDocPropertyTag phpDocPropertyTag = propertyTags.get(comment.getPropertyTags().size() - 1);
                offset = phpDocPropertyTag.getTextOffset() + phpDocPropertyTag.getTextLength();
            }
            editor.getCaretModel().moveToOffset(offset);
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document);
            templateManager.startTemplate(editor, template);
    }

}
