package com.nvlad.yii2support.configurations;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class YiiAppCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        final FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();
        final PsiElement psiElement = completionParameters.getPosition();
        final Project project = psiElement.getProject();
        final GlobalSearchScope scope = GlobalSearchScope.projectScope(project);

        if((psiElement.getParent() instanceof FieldReference)
                && ((FieldReference) psiElement.getParent()).getSignature().endsWith("Yii.app.IntellijIdeaRulezzz"))
        {
            fileBasedIndex.processAllKeys(ComponentsIndex.identity, key -> {
                List<String> values = fileBasedIndex.getValues(ComponentsIndex.identity, key, scope);
                if(values.size() > 0){
                    LookupElementBuilder lookupElement = LookupElementBuilder.create(key)
                            .withTypeText(values.get(0), true)
                            .withIcon(AllIcons.Nodes.Class);

                    completionResultSet.addElement(lookupElement);
                }
                return true;
            }, project);
        }
    }
}
