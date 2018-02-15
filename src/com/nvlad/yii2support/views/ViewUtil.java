package com.nvlad.yii2support.views;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.InvalidPathException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewUtil {
    private static final Set<String> ignoredVariables = getIgnoredVariables();

    public static String resolveViewNamespace(String viewPath) {
        if (viewPath.startsWith("/views/")) {
            return "app";
        }

        return viewPath.substring(1, viewPath.indexOf('/', 1));
    }

    public static String resolveViewName(String viewPath) {
        if (viewPath.startsWith("/views/")) {
            return "@app" + viewPath;
        }

        return null;
    }

    @Nullable
    public static String getViewPrefix(PsiElement element) {
        String value = getValue(element);
        if (value.startsWith("@app")) {
            return value;
        }
        if (value.startsWith("//")) {
            return "@app/views" + value.substring(1);
        }

        final MethodReference method = PsiTreeUtil.getParentOfType(element, MethodReference.class);
        if (method == null || method.getClassReference() == null) {
            return null;
        }
        PhpClass callerClass = ClassUtils.getPhpClassByCallChain(method);
        if (callerClass == null) {
            return null;
        }

        final PhpIndex phpIndex = PhpIndex.getInstance(element.getProject());
        final String key;
        if (callerClass.getName().endsWith("Controller") && ClassUtils.isClassInheritsOrEqual(callerClass, "\\yii\\base\\Controller", phpIndex)) {
            key = resolveViewFromController(callerClass, method, element, value);
        } else {
            return null;
        }

        return normalizePath(key);
    }

    @NotNull
    private static String resolveViewFromController(PhpClass clazz, MethodReference method, PsiElement element, String value) {
        final String classFQN = clazz.getFQN().replace('\\', '/');
        StringBuilder result = new StringBuilder("@app");
        String path = deletePathPart(classFQN);
        if (path.startsWith("/modules/")) {
            result.append("/modules");
            path = deletePathPart(path);
        }
        int controllersPathPartPosition = path.indexOf("/controllers/");
        if (controllersPathPartPosition == -1) {
            throw new InvalidPathException(path, "");
        }
        if (controllersPathPartPosition > 0) {
            result.append(path, 0, controllersPathPartPosition);
            path = path.substring(controllersPathPartPosition);
        }
        path = deletePathPart(path);
        result.append("/views");
        if (value.startsWith("/")) {
            return result + value;
        }
        final String className = clazz.getName();
        result.append(path, 0, path.length() - className.length());
        result.append(StringUtils.CamelToId(className.substring(0, className.length() - 10), "-"));
        result.append('/');
        result.append(value);

        return result.toString();
    }

    private static String deletePathPart(String path) {
        int returnFromPosition = path.indexOf('/', path.startsWith("/") ? 1 : 0);
        return returnFromPosition == -1 ? path : path.substring(returnFromPosition);
    }

    @Nullable
    public static String _getViewPrefix(PsiElement element) {
        String result = getValue(element);

        if (result.startsWith("//")) {
            return "@app/views" + result.substring(1);
        }
        if (result.startsWith("/")) {
            return "@app/views" + result;
        }
        if (result.startsWith("@app/")) {
            return result;
        }

        final MethodReference method = PsiTreeUtil.getParentOfType(element, MethodReference.class);
        if (method == null || method.getClassReference() == null) {
            return null;
        }

        Project project = element.getProject();
        PhpIndex phpIndex = PhpIndex.getInstance(project);
        PhpClass clazz = ClassUtils.getPhpClassByCallChain(method);
        if (ClassUtils.isClassInheritsOrEqual(clazz, "\\yii\\base\\Controller", phpIndex)) {
            final String className = method.getClassReference().getType().toStringResolved().replace('\\', '/');
            final String path = StringUtils.CamelToId(className.substring(className.indexOf("/controllers/") + 12, className.length() - 10), "-");
            return "@app/views" + normalizePath(path + '/' + result);
        }
//
//        if (ClassUtils.isClassInheritsOrEqual(clazz, "\\yii\\base\\View", phpIndex)) {
//
//        }

        return null;
    }

    private static String normalizePath(String path) {
        Pattern pattern = Pattern.compile("/([a-z0-9-]+/\\.\\./)");
        Matcher matcher = pattern.matcher(path);
        while (matcher.find()) {
            path = pattern.matcher(path).replaceAll("/");
            matcher = pattern.matcher(path);
        }
        return path;
    }


    @NotNull
    public static Collection<String> getPhpViewVariables(PsiFile psiFile) {
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

        final SearchScope fileScope = psiFile.getUseScope();
        final HashSet<String> usedBeforeDeclaration = new HashSet<>();
        for (Variable variable : viewVariables) {
            String variableName = variable.getName();
            if (variable.isDeclaration()
                    && fileScope.equals(variable.getUseScope())
                    && !(variable.getParent() instanceof PhpUseList)
                    && !(variable.getParent() instanceof UnaryExpression)
                    && !(variable.getParent() instanceof SelfAssignmentExpression)
                    && !usedBeforeDeclaration.contains(variableName)) {
                declaredVariables.add(variableName);
            } else {
                if (!ignoredVariables.contains(variableName)) {
                    if (fileScope.equals(variable.getUseScope()) || variable.getParent() instanceof PhpUseList) {
                        if (variable.getName().equals("") && variable.getParent() instanceof StringLiteralExpression) {
                            Variable inlineVariable = PsiTreeUtil.findChildOfType(variable, Variable.class);
                            if (inlineVariable != null) {
                                allVariables.add(inlineVariable.getName());
                                usedBeforeDeclaration.add(variableName);
                            }
                        } else {
                            allVariables.add(variableName);
                            usedBeforeDeclaration.add(variableName);
                        }
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

    private static Set<String> getIgnoredVariables() {
        final Set<String> set = new HashSet<>(Arrays.asList("this", "_file_", "_params_"));
        set.addAll(Variable.SUPERGLOBALS);
        return set;
    }

//    public static boolean isValidRenderMethod(MethodReference methodReference) {
//        final PhpClass clazz = ClassUtils.getPhpClassByCallChain(methodReference);
//        if (clazz == null) {
//            return false;
//        }
//        final PhpIndex phpIndex = PhpIndex.getInstance(clazz.getProject());
//
//        return ClassUtils.isClassInheritsOrEqual(clazz, "\\yii\\base\\Controller", phpIndex)
//                || ClassUtils.isClassInheritsOrEqual(clazz, "\\yii\\base\\View", phpIndex)
//                || ClassUtils.isClassInheritsOrEqual(clazz, "\\yii\\base\\Widget", phpIndex)
//                || ClassUtils.isClassInheritsOrEqual(clazz, "\\yii\\mail\\BaseMailer", phpIndex);
//    }

    @NotNull
    private static String getValue(PsiElement expression) {
        if (expression instanceof StringLiteralExpression) {
            String value = ((StringLiteralExpression) expression).getContents();
            if (value.contains("IntellijIdeaRulezzz ")) {
                return value.substring(0, value.indexOf("IntellijIdeaRulezzz "));
            }
            return value;
        }

        return "";
    }

//    private static String transformName(String name) {
//        Pattern pattern = Pattern.compile("([A-Z])");
//        Matcher matcher = pattern.matcher(name);
//        if (!matcher.find()) {
//            return name;
//        }
//
//        for (int i = matcher.groupCount(); i > 0; i--) {
//            int groupStart = matcher.start(i);
//            if (groupStart > 0) {
//                if (name.charAt(groupStart - 1) != '/') {
//                    name = name.substring(0, groupStart) + name.charAt(groupStart) + name.substring(groupStart);
//                }
//            }
//        }
//
//        return name.toLowerCase();
//    }
}
