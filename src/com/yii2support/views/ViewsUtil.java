package com.yii2support.views;

import com.intellij.codeInsight.template.macro.SplitWordsMacro;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.*;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by NVlad on 15.01.2017.
 */
public class ViewsUtil {
    public static final Key<String> RENDER_VIEW = Key.create("com.yii2support.views.render.view");
    public static final Key<PsiFile> RENDER_VIEW_FILE = Key.create("com.yii2support.views.viewFile");
    private static final Key<String> RENDER_VIEW_PATH = Key.create("views.viewPath");
    private static final Key<Long> VIEW_FILE_MODIFIED = Key.create("com.yii2support.views.viewFileModified");
    private static final Key<ArrayList<String>> VIEW_VARIABLES = Key.create("com.yii2support.views.viewVariables");
    private static final Key<PsiDirectory> VIEWS_DIRECTORY = Key.create("views.directory");
    private static final Key<Long> VIEWS_DIRECTORY_MODIFIED = Key.create("views.directory.modified");
    private static final Key<PsiDirectory> VIEWS_CONTEXT_DIRECTORY = Key.create("views.context.directory");

    private static final Set<String> ignoredVariables = getIgnoredVariables();

    public static final String[] renderMethods = {"render", "renderAjax", "renderPartial"};

    private static Set<String> getIgnoredVariables() {
        final Set<String> set = new THashSet<>(Arrays.asList("this", "_file_", "_params_"));
        set.addAll(Variable.SUPERGLOBALS);
        return set;
    }

    @NotNull
    private static ArrayList<String> getPhpViewVariables(PsiFile psiFile) {
        final ArrayList<String> result = new ArrayList<>();
        final HashSet<String> allVariables = new HashSet<>();
        final HashSet<String> declaredVariables = new HashSet<>();
        final Collection<Variable> viewVariables = PsiTreeUtil.findChildrenOfType(psiFile, Variable.class);

        for (FunctionReference reference : PsiTreeUtil.findChildrenOfType(psiFile, FunctionReference.class)) {
            if (reference.getNode().getElementType() == PhpElementTypes.FUNCTION_CALL && psiFile.getUseScope().equals(reference.getUseScope())) {
                if (reference.getName() != null && reference.getName().equals("compact")) {
                    for (PsiElement element : reference.getParameters()) {
                        if (element instanceof StringLiteralExpression) {
                            allVariables.add(((StringLiteralExpression) element).getContents());
                        }
                    }
                }
            }
        }

        for (Variable variable : viewVariables) {
            String variableName = variable.getName();
            if (variable.isDeclaration()) {
                if (!declaredVariables.contains(variableName)) {
                    declaredVariables.add(variableName);
                }
            } else {
                if (!ignoredVariables.contains(variableName)) {
                    if (!allVariables.contains(variableName) && psiFile.getUseScope().equals(variable.getUseScope())) {
                        allVariables.add(variableName);
                    }
                }
            }
        }

        for (String variable : allVariables) {
            if (!declaredVariables.contains(variable)) {
                result.add(variable);
            }
        }

        return result;
    }

    @NotNull
    public static ArrayList<String> getViewVariables(PsiFile psiFile) {
        ArrayList<String> result = null;

        Long viewModified = psiFile.getUserData(VIEW_FILE_MODIFIED);
        if (viewModified != null && psiFile.getModificationStamp() == viewModified) {
            result = psiFile.getUserData(VIEW_VARIABLES);
        }

        if (result == null) {
            if (psiFile instanceof PhpFile) {
                result = getPhpViewVariables(psiFile);
            }

            if (result == null) {
                result = new ArrayList<>();
            }

            psiFile.putUserData(VIEW_VARIABLES, result);
            psiFile.putUserData(VIEW_FILE_MODIFIED, psiFile.getModificationStamp());
        }

        return new ArrayList<>(result);
    }

    public static PsiFile getViewFile(PsiElement element) {
        final MethodReference reference = PsiTreeUtil.getParentOfType(element, MethodReference.class);
        if (reference == null) {
            return null;
        }

        final PsiElement[] parameters = reference.getParameters();
        if (parameters.length == 0 || !(parameters[0] instanceof StringLiteralExpression)) {
            return null;
        }

        String view = reference.getUserData(RENDER_VIEW);
        if (!((StringLiteralExpression) parameters[0]).getContents().equals(view)) {
            view = ((StringLiteralExpression) parameters[0]).getContents();
            reference.putUserData(RENDER_VIEW, view);
            reference.putUserData(RENDER_VIEW_FILE, null);
            reference.putUserData(RENDER_VIEW_PATH, null);
        }

        PsiFile file = reference.getUserData(RENDER_VIEW_FILE);
        if (file != null && file.isValid()) {
            if (!file.getVirtualFile().getPath().equals(reference.getUserData(RENDER_VIEW_PATH))) {
                reference.putUserData(RENDER_VIEW_FILE, null);
                reference.putUserData(RENDER_VIEW_PATH, null);
                file = null;
            }
        }

        if (file == null || !file.isValid()) {
            if (reference.getParameters()[0] instanceof StringLiteralExpression) {
                PsiDirectory directory;

                String path = ((StringLiteralExpression) reference.getParameters()[0]).getContents();
                if (path.startsWith("/")) {
                    directory = ViewsUtil.getRootDirectory(element);
                    path = path.substring(1);
                } else {
                    directory = ViewsUtil.getContextDirectory(element);
                }

                String filename;
                if (path.contains("/")) {
                    filename = path.substring(path.lastIndexOf('/') + 1);
                    path = path.substring(0, path.lastIndexOf('/') + 1);
                } else {
                    filename = path;
                    path = "";
                }

                while (path.contains("/") && directory != null) {
                    directory = directory.findSubdirectory(path.substring(0, path.indexOf('/')));
                    path = path.substring(path.indexOf('/') + 1);
                }

                if (directory == null) {
                    return null;
                }

                if (filename.contains(".")) {
                    file = directory.findFile(filename);
                } else {
                    file = directory.findFile(filename + ".php");
                    if (file == null) {
                        file = directory.findFile(filename + ".twig");
                    }
                    if (file == null) {
                        file = directory.findFile(filename + ".tpl");
                    }
                }

                if (file != null) {
                    reference.putUserData(RENDER_VIEW_FILE, file);
                    reference.putUserData(RENDER_VIEW_PATH, file.getVirtualFile().getPath());
                }
            }
        }

        return file;
    }

    public static PsiDirectory getRootDirectory(PsiElement element) {
        final PsiFile file = element.getContainingFile();

        if (file.getUserData(VIEWS_DIRECTORY) != null) {
            Long modified = file.getUserData(VIEWS_DIRECTORY_MODIFIED);
            if (modified != null && modified == file.getModificationStamp()) {
                return file.getUserData(VIEWS_DIRECTORY);
            }
        }

        return findDirectory(element);
    }

    public static PsiDirectory getContextDirectory(PsiElement element) {
        final PsiFile file = element.getContainingFile();

        if (file.getUserData(VIEWS_CONTEXT_DIRECTORY) != null) {
            Long modified = file.getUserData(VIEWS_DIRECTORY_MODIFIED);
            if (modified != null && modified == file.getModificationStamp()) {
                return file.getUserData(VIEWS_CONTEXT_DIRECTORY);
            }
        }

        findDirectory(element);

        return file.getUserData(VIEWS_CONTEXT_DIRECTORY);
    }

    private static PsiDirectory findDirectory(PsiElement element) {
        final PhpClass phpClass = PsiTreeUtil.getParentOfType(element, PhpClass.class);
        final PsiFile file = element.getContainingFile();
        if (phpClass != null) {
            return findClassDirectory(phpClass, file);
        }

        PsiDirectory context = file.getOriginalFile().getContainingDirectory();
        PsiDirectory root = context.getParentDirectory();
        while (root != null && !root.getName().equals("views")) {
            root = root.getParentDirectory();
        }

        file.putUserData(VIEWS_CONTEXT_DIRECTORY, context);
        file.putUserData(VIEWS_DIRECTORY, root);
        file.putUserData(VIEWS_DIRECTORY_MODIFIED, file.getModificationStamp());

        return root;
    }

    private static PsiDirectory findClassDirectory(PhpClass phpClass, PsiFile file) {
        if (phpClass != null) {
            Method getViewPath = phpClass.findMethodByName("getViewPath");
            PhpClass ownClass = phpClass.getSuperClass();
            PsiDirectory directory = file.getOriginalFile().getContainingDirectory();
            if (directory != null) {
                while (ownClass != null) {
                    if (getViewPath != null) {
                        getViewPath = ownClass.findMethodByName("getViewPath");
                    }
                    switch (ownClass.getFQN()) {
                        case "\\yii\\base\\Controller":
                            directory = directory.getParentDirectory();
                            if (directory == null) {
                                return null;
                            }
                            if (getViewPath != null && Objects.equals(getViewPath.getContainingClass(), ownClass)) {
                                directory = directory.findSubdirectory("views");
                            }

                            if (directory != null) {
                                file.putUserData(VIEWS_DIRECTORY_MODIFIED, file.getModificationStamp());
                                file.putUserData(VIEWS_DIRECTORY, directory);

                                String controllerId = phpClass.getName();
                                controllerId = controllerId.substring(0, controllerId.length() - 10);
                                controllerId = new SplitWordsMacro.LowercaseAndDash().convertString(controllerId);

                                PsiDirectory context = directory.findSubdirectory(controllerId);
                                if (context != null) {
                                    file.putUserData(VIEWS_CONTEXT_DIRECTORY, context);
                                }
                            }

                            return directory;
                        case "\\yii\\base\\Widget":
                            directory = directory.findSubdirectory("views");
                            if (directory != null) {
                                file.putUserData(VIEWS_DIRECTORY_MODIFIED, file.getModificationStamp());
                                file.putUserData(VIEWS_DIRECTORY, directory);
                                file.putUserData(VIEWS_CONTEXT_DIRECTORY, directory);
                            }

                            return directory;
                    }
                    ownClass = ownClass.getSuperClass();
                }
            }
        }

        return null;
    }
}
