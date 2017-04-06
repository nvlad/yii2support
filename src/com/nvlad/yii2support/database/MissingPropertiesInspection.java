package com.nvlad.yii2support.database;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.ex.ProblemDescriptorImpl;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocPropertyTag;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.DatabaseUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oleg on 06.04.2017.
 */
public class MissingPropertiesInspection  extends PhpInspection {
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean isOnTheFly) {
        return new PhpElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if (element instanceof PhpDocComment && DatabaseUtils.HasConnections(element.getProject())) {
                    PhpDocComment docComment = (PhpDocComment)element;
                    PhpIndex index = PhpIndex.getInstance(element.getProject());
                    if (docComment != null) {
                        PhpClass phpClass = DatabaseUtils.getClassByClassPhpDoc(docComment);
                        if (phpClass != null && ClassUtils.isClassInheritsOrEqual(phpClass, ClassUtils.getClass(index, "\\yii\\db\\BaseActiveRecord"))) {
                            List<PhpDocPropertyTag> propertyTags = docComment.getPropertyTags();
                            String table = DatabaseUtils.getTableByActiveRecordClass(phpClass);
                            ArrayList<String[]> notDeclaredColumns = DatabaseUtils.getNotDeclaredColumns(table, propertyTags, element.getProject());
                            if (notDeclaredColumns.size() > 0) {
                                MissingPropertiesQuickFix qFix = new MissingPropertiesQuickFix(notDeclaredColumns, docComment);
                                problemsHolder.registerProblem(docComment, "Class " + phpClass.getFQN() +
                                        " is missing " + notDeclaredColumns.size() + " propertie(s) that corresponds to database columns",qFix);
                            }
                        }
                    }
                }
                super.visitElement(element);
            }


        };
    }
}
