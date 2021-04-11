package com.nvlad.yii2support.widgetsconfig;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.configurations.ComponentsIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WidgetConfigCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, @NotNull ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        boolean isArrayItem = false;
        final Project project = completionParameters.getPosition().getProject();
        final PhpIndex phpIndex = PhpIndex.getInstance(project);

        // Case of first level in attribute/columns array
        PsiElement top = walkParents(completionParameters,8);
        if(!(top instanceof MethodReference)) {
            // Case of ['?'] in attribute/column array
            top = walkParents(completionParameters, 10);
            if(top instanceof MethodReference){
                if (walkParents(completionParameters, 3) instanceof ArrayCreationExpression) {
                    isArrayItem = true;
                }
            }else{
                // Case of ['attribute' => '?'] in array
                top = walkParents(completionParameters, 11);
            }
        }

        PhpClass modelClass = null;

        if(top instanceof MethodReference){
            PsiElement method = ((MethodReference) top).resolve();
            if(method instanceof Method && ((Method) method).getName().equals("widget")){
                for(PsiElement child : top.getChildren()){
                    if(child instanceof ParameterList){
                        PsiElement[] params = ((ParameterList) child).getParameters();
                        for(PsiElement conf : params[0].getChildren()){
                            String key = getHashKeyContents(conf);
                            // 'model' key for DetailView widget and 'filterModel' for GridView
                            if(key != null && (key.equals("model") || key.equals("filterModel"))){
                                PsiElement val = ((ArrayHashElement) conf).getValue();
                                if(val instanceof Variable){
                                    modelClass = ClassUtils.getClassByVariable((Variable) val);
                                }
                            }
                        }
                    }
                }
            }
        }

        if(isArrayItem){
            PhpClass phpClass = ClassUtils.getPhpClassByCallChain((MethodReference) top);
            if(phpClass != null) {
                if (ClassUtils.isClassInheritsOrEqual(phpClass, "\\yii\\widgets\\DetailView", phpIndex)) {
                    String[] attributesString = new String[]{"attribute", "label", "format"};
                    String[] attributes = new String[]{"value", "visible", "contentOptions", "captionOptions"};

                    for (String attribute : attributesString) {
                        completionResultSet.addElement(buildLookup(attribute, true));
                    }
                    for (String attribute : attributes) {
                        completionResultSet.addElement(buildLookup(attribute, false));
                    }
                }else if(ClassUtils.isClassInheritsOrEqual(phpClass, "\\yii\\widgets\\BaseListView", phpIndex)){
                    completionResultSet.addElement(buildValueClosureLookup(false));
                }
            }
        } else {
            final PsiElement element = completionParameters.getPosition().getParent();
            if (!(element instanceof PhpExpression)) {
                return;
            }

            String key = getHashKeyContents(element.getParent().getParent());
            if(key != null) {
                if (key.equals("format")) {
                    doFormatterCompletion(completionResultSet, project, phpIndex);
                    return;
                }else if (!key.equals("attribute")){
                    return;
                }
            }

            if(element instanceof StringLiteralExpression){
                String attributeString = ((StringLiteralExpression) element).getContents();
                int elCount = attributeString.split(":").length;
                if(elCount == 2){ // Case 'field:' to call formatter
                    completionResultSet = completionResultSet.withPrefixMatcher(
                            attributeString.substring(attributeString.indexOf(':')+1)
                                .replace("IntellijIdeaRulezzz ",""));
                    doFormatterCompletion(completionResultSet, project, phpIndex);
                    return;
                }else if(elCount > 2){
                    return;
                }
            }

            if(modelClass != null) {
                for (Field field : ClassUtils.getClassFields(modelClass)) {
                    LookupElementBuilder lookupBuilder = buildLookup(field);
                    completionResultSet.addElement(lookupBuilder);
                }
            }
        }
    }

    private void doFormatterCompletion(@NotNull CompletionResultSet completionResultSet, Project project, PhpIndex phpIndex){
        final GlobalSearchScope scope = GlobalSearchScope.projectScope(project);

        PhpClass completionClass = null;
        for (String className : FileBasedIndex.getInstance().getValues(ComponentsIndex.identity, "formatter", scope)) {
            completionClass = PhpIndex.getInstance(project).getAnyByFQN(className).iterator().next();
        }
        if(completionClass == null) {
            completionClass = ClassUtils.getClass(phpIndex, "yii\\i18n\\Formatter");
        }
        if (completionClass != null) {
            for (Method method : ClassUtils.getFormatterAsMethods(completionClass)) {
                LookupElementBuilder lookupBuilder = buildLookup(method);
                completionResultSet.addElement(lookupBuilder);
            }
        }
    }

    @Nullable
    private PsiElement walkParents(CompletionParameters parameters, int level) {
        PsiElement element = parameters.getPosition();
        for (int i = 0; i < level; i++) {
            if (element == null) {
                return null;
            }
            element = element.getParent();
        }
        return element;
    }

    @NotNull
    private LookupElementBuilder buildLookup(PhpClassMember field) {
        String lookupString = field instanceof Method ? ClassUtils.getAsPropertyName((Method) field) : field.getName();
        LookupElementBuilder builder = LookupElementBuilder.create(field, lookupString).withIcon(field.getIcon());
        if (field instanceof Field) {
            builder = builder.withTypeText(field.getType().toString());
        }

        return buildLookup(builder, false, false);
    }

    @NotNull
    private LookupElementBuilder buildLookup(String field, boolean addString) {
        LookupElementBuilder builder = LookupElementBuilder.create(field).withIcon(AllIcons.Nodes.Variable);
        return buildLookup(builder,true, addString);
    }

    @NotNull
    private LookupElementBuilder buildLookup(LookupElementBuilder builder, boolean isArrayItem, boolean addString) {
        return builder.withInsertHandler((insertionContext, lookupElement) -> {
            Document document = insertionContext.getDocument();
            int insertPosition = insertionContext.getSelectionEndOffset();
            if (isArrayItem) {
                document.insertString(insertPosition + 1, " => "+(addString?"''":"")+",");
                insertPosition += 5;
                if(addString){
                    insertPosition++;
                }
                insertionContext.getEditor().getCaretModel().getCurrentCaret().moveToOffset(insertPosition);
            }
        });
    }

    @NotNull
    private LookupElementBuilder buildValueClosureLookup(boolean allParams){
        return LookupElementBuilder.create("value")
            .withIcon(AllIcons.Nodes.Function)
            .withTypeText(allParams?"function ($model, $key, $index, $column)":"function ($data) {}")
            .withInsertHandler((insertionContext, lookupElement) -> {
                Document document = insertionContext.getDocument();
                int insertPosition = insertionContext.getSelectionEndOffset();
                if(allParams) {
                    document.insertString(insertPosition + 1, " => function ($model, $key, $index, $column) {},");
                    insertPosition += 47;
                }else{
                    document.insertString(insertPosition + 1, " => function ($data) {},");
                    insertPosition += 23;
                }
                insertionContext.getEditor().getCaretModel().getCurrentCaret().moveToOffset(insertPosition);
        });
    }

    @Nullable
    private String getHashKeyContents(PsiElement e) {
        if (e instanceof ArrayHashElement) {
            PhpPsiElement key = ((ArrayHashElement) e).getKey();
            if (key instanceof StringLiteralExpression) {
                return ((StringLiteralExpression) key).getContents();
            }
        }
        return null;
    }
}
