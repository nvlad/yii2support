package com.nvlad.yii2support.properties;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocPropertyTag;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.DatabaseUtils;
import com.nvlad.yii2support.common.VirtualProperty;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by oleg on 06.04.2017.
 */
public class PropertiesInspection extends PhpInspection {


    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean isOnTheFly) {


        return new PhpElementVisitor() {

            @Override
            public void visitElement(PsiElement element) {
                if (element instanceof PhpDocComment && DatabaseUtils.HasConnections(element.getProject())) {
                    PhpDocComment docComment = (PhpDocComment) element;
                    PhpIndex index = PhpIndex.getInstance(element.getProject());

                    PhpClass phpClass = DatabaseUtils.getClassByClassPhpDoc(docComment);
                    if (phpClass != null && ClassUtils.isClassInheritsOrEqual(phpClass, ClassUtils.getClass(index, "\\yii\\db\\BaseActiveRecord"))) {
                        Collection<Field> fields = phpClass.getFields();
                        String table = DatabaseUtils.getTableByActiveRecordClass(phpClass);
                        ArrayList<VirtualProperty> notDeclaredColumns = DatabaseUtils.getNotDeclaredColumns(table, fields, element.getProject());
                        if (notDeclaredColumns.size() > 0) {
                            MissingPropertiesQuickFix qFix = new MissingPropertiesQuickFix(notDeclaredColumns, docComment);
                            String str1 = notDeclaredColumns.size() > 1 ? "properties" : "property";
                            problemsHolder.registerProblem(docComment, "Class " + phpClass.getFQN() +
                                    " is missing " + notDeclaredColumns.size() + " " + str1 + " that corresponds to database columns", ProblemHighlightType.WEAK_WARNING, qFix);
                        }

                        ArrayList<PhpDocPropertyTag> unusedProperties = DatabaseUtils.getUnusedProperties(table, docComment.getPropertyTags(), phpClass);
                        if (unusedProperties.size() > 0) {
                            for (PhpDocPropertyTag tag: unusedProperties) {
                                problemsHolder.registerProblem(tag, "Property is unused in class " + phpClass.getFQN(), ProblemHighlightType.LIKE_UNUSED_SYMBOL);
                            }
                        }
                    }

                }
                super.visitElement(element);
            }


        };
    }
}
