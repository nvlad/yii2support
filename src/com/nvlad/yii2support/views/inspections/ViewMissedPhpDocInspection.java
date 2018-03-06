package com.nvlad.yii2support.views.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocVariable;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.nvlad.yii2support.views.entities.ViewResolve;
import com.nvlad.yii2support.views.util.RenderUtil;
import com.nvlad.yii2support.views.util.ViewUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ViewMissedPhpDocInspection extends PhpInspection {
    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean b) {
        return new PhpElementVisitor() {
            @Override
            public void visitPhpFile(PhpFile PhpFile) {
                Project project = PhpFile.getProject();
                ViewResolve resolve = ViewUtil.resolveView(PhpFile.getVirtualFile(), project);
                if (resolve == null) {
                    return;
                }

                Map<String, String> params = getVariables(PhpFile);
                Map<String, String> declaredVariables = new HashMap<>();
                Collection<PhpDocVariable> variableCollection = PsiTreeUtil.findChildrenOfType(PhpFile, PhpDocVariable.class);
                for (PhpDocVariable variable : variableCollection) {
                    declaredVariables.put(variable.getName(), variable.getType().toString());
                }

                Map<String, String> missedVariables = new HashMap<>();
                for (String variableName : params.keySet()) {
                    if (!declaredVariables.containsKey(variableName)) {
                        missedVariables.put(variableName, params.get(variableName));
                    }
                }

                if (missedVariables.isEmpty()) {
                    return;
                }

                String problemDescription = "Missed View variable declaration.";
                ViewMissedPhpDocLocalQuickFix quickFix = new ViewMissedPhpDocLocalQuickFix(PhpFile, missedVariables);
                problemsHolder.registerProblem(PhpFile, problemDescription, quickFix);
            }

            private Map<String, String> getVariables(PhpFile phpFile) {
                Collection<PsiReference> references = ReferencesSearch.search(phpFile).findAll();
                Map<String, String> result = new HashMap<>();
                for (PsiReference reference : references) {
                    MethodReference methodReference = PsiTreeUtil.getParentOfType(reference.getElement(), MethodReference.class);
                    if (methodReference == null) {
                        continue;
                    }

                    Map<String, String> params = RenderUtil.getViewParameters(methodReference);
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        if (result.containsKey(entry.getKey())) {
                            String currentType = result.get(entry.getKey()) + "|" + entry.getValue();
                            result.replace(entry.getKey(), currentType);
                        } else {
                            result.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
                return result;
            }
        };
    }
}
