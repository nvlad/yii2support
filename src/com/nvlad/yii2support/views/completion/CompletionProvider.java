package com.nvlad.yii2support.views.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.nvlad.yii2support.common.PhpUtil;
import com.nvlad.yii2support.views.ViewResolve;
import com.nvlad.yii2support.views.ViewResolveFrom;
import com.nvlad.yii2support.views.ViewUtil;
import com.nvlad.yii2support.views.ViewsUtil;
import com.nvlad.yii2support.views.index.ViewFileIndex;
import com.nvlad.yii2support.views.index.ViewInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by NVlad on 27.12.2016.
 */
class CompletionProvider extends com.intellij.codeInsight.completion.CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters,
                                  ProcessingContext processingContext,
                                  @NotNull CompletionResultSet completionResultSet) {

        final PsiElement psiElement = completionParameters.getPosition();
        final MethodReference method = PsiTreeUtil.getParentOfType(psiElement, MethodReference.class);

        if (method == null || method.getParameters().length == 0) {
            return;
        }

        if (!ViewsUtil.isValidRenderMethod(method)) {
            return;
        }

        PsiElement viewParameter = psiElement;
        while (viewParameter != null && !(viewParameter.getParent() instanceof ParameterList)) {
            viewParameter = viewParameter.getParent();
        }

        if (viewParameter == null || !viewParameter.equals(method.getParameters()[0])) {
            return;
        }

        final ViewResolve resolve = ViewUtil.resolveView(viewParameter);
        if (resolve == null) {
            return;
        }

        final Project project = psiElement.getProject();
        final GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        final FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();

        int prefixLength = resolve.key.length();
        if (resolve.key.contains("/") && !resolve.key.endsWith("/")) {
            prefixLength = resolve.key.lastIndexOf('/') + 1;
        }

        final String prefixFilter = resolve.key.substring(0, prefixLength);
        final Set<String> keys = new HashSet<>();
        fileBasedIndex.processAllKeys(ViewFileIndex.identity, key -> {
            if (key.startsWith(prefixFilter)) {
                keys.add(key);
            }
            return true;
        }, scope, null);

        if (!completionParameters.isAutoPopup()) {
            completionResultSet = completionResultSet.withPrefixMatcher(resolve.key.substring(prefixLength));
        }

        final PsiManager psiManager = PsiManager.getInstance(project);
        boolean localViewSearch = false;
        if (resolve.from == ViewResolveFrom.View) {
            final String value = PhpUtil.getValue(viewParameter);
            localViewSearch = !value.startsWith("@") && !value.startsWith("//");
        }
        for (String key : keys) {
            Collection<ViewInfo> views = fileBasedIndex.getValues(ViewFileIndex.identity, key, scope);
            for (ViewInfo view : views) {
                if (!resolve.application.equals(view.application)) {
                    continue;
                }

                if (localViewSearch && !resolve.theme.equals(view.theme)) {
                    continue;
                }

                PsiFile psiFile = psiManager.findFile(view.getVirtualFile());
                if (psiFile != null) {
                    String insertText = key.substring(prefixLength);
                    completionResultSet.addElement(new ViewLookupElement(psiFile, insertText));
                    break;
                } else {
                    System.out.println(view.fileUrl + " => not exists");
                }
            }
        }

//        Collection<String> keys = StubIndex.getInstance().getAllKeys(YiiViewIndex.KEY, completionParameters.getPosition().getProject());

//


//        String path = getValue(method.getParameters()[0]);
//        PsiDirectory directory;
//        if (path.startsWith("/")) {
//            path = path.substring(1);
//            directory = ViewsUtil.getRootDirectory(psiElement);
//        } else {
//            directory = ViewsUtil.getContextDirectory(psiElement);
//        }
//        if (path.contains("/")) {
//            path = path.substring(0, path.lastIndexOf('/') + 1);
//        }
//
//        while (path.contains("/") && directory != null) {
//            String subdirectory = path.substring(0, path.indexOf('/'));
//            path = path.substring(path.indexOf('/') + 1);
//            directory = subdirectory.equals("..") ? directory.getParent() : directory.findSubdirectory(subdirectory);
//        }
//        if (directory != null) {
//            if (completionResultSet.getPrefixMatcher().getPrefix().contains("/")) {
//                String prefix = completionResultSet.getPrefixMatcher().getPrefix();
//                prefix = prefix.substring(prefix.lastIndexOf("/") + 1);
//                completionResultSet = completionResultSet.withPrefixMatcher(prefix);
//            }
//
//            for (PsiDirectory psiDirectory : directory.getSubdirectories()) {
//                completionResultSet.addElement(new DirectoryLookupElement(psiDirectory));
//            }
//
//            for (PsiFile psiFile : directory.getFiles()) {
//                completionResultSet.addElement(new ViewLookupElement(psiFile));
//            }
//        }
    }
//
//    @NotNull
//    private String getValue(PsiElement expression) {
//        if (expression instanceof StringLiteralExpression) {
//            String value = ((StringLiteralExpression) expression).getContents();
//            return value.substring(0, value.indexOf("IntellijIdeaRulezzz "));
//        }
//
//        return "";
//    }
}
