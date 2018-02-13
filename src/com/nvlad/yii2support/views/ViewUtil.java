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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewUtil {
    private static final Set<String> ignoredVariables = getIgnoredVariables();

    public static String resolveViewNamespace(String viewPath) {
        if (viewPath.startsWith("/views/")) {
            return "basic";
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
