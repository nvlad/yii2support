package com.nvlad.yii2support.views;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewUtil {
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
        if (result.startsWith("/")) {
            return "@app" + result;
        }
        if (result.startsWith("@app/")) {
            return result;
        }

        final MethodReference method = PsiTreeUtil.getParentOfType(element, MethodReference.class);
        if (method == null || method.getClassReference() == null) {
            return null;
        }
        String className = method.getClassReference().getType().toStringResolved().replace('\\', '/');
        if (className.endsWith("Controller")) {
            String path = StringUtils.CamelToId(className.substring(className.indexOf("/controllers/") + 12, className.length() - 10), "-");
            path += '/' + result;
            Pattern pattern = Pattern.compile("/([a-z-0-9-]+/\\.\\.)");
            Matcher matcher = pattern.matcher(path);
            while (matcher.find()) {
                path = pattern.matcher(path).replaceAll("");
                matcher = pattern.matcher(path);
            }

            return "@app/views" + path;
        }

        return null;
    }

    public static boolean isValidRenderMethod(MethodReference methodReference) {
        final PhpClass clazz = ClassUtils.getPhpClassByCallChain(methodReference);
        if (clazz == null) {
            return false;
        }
        final PhpIndex phpIndex = PhpIndex.getInstance(clazz.getProject());

        return ClassUtils.isClassInheritsOrEqual(clazz, "\\yii\\base\\Controller", phpIndex)
                || ClassUtils.isClassInheritsOrEqual(clazz, "\\yii\\base\\View", phpIndex)
                || ClassUtils.isClassInheritsOrEqual(clazz, "\\yii\\base\\Widget", phpIndex)
                || ClassUtils.isClassInheritsOrEqual(clazz, "\\yii\\mail\\BaseMailer", phpIndex);
    }

    @NotNull
    private static String getValue(PsiElement expression) {
        if (expression instanceof StringLiteralExpression) {
            String value = ((StringLiteralExpression) expression).getContents();
            return value.substring(0, value.indexOf("IntellijIdeaRulezzz "));
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
