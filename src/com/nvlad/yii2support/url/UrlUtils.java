package com.nvlad.yii2support.url;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.Parameter;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.nvlad.yii2support.common.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by oleg on 25.04.2017.
 */
public class UrlUtils {
    private static List<String> excludeControllers =  Arrays.asList(
            "\\yii\\rest\\ActiveController",
            "\\yii\\gii\\controllers\\DefaultController",
            "\\yii\\rest\\Controller",
            "\\yii\\debug\\controllers\\DefaultController",
            "\\yii\\debug\\controllers\\UserController"
    );

    private static Collection<PhpClass> getClassesByParent(String parentFqn, Project project) {
        Collection<PhpClass> subclasses = new ArrayList<>();
        Collection<PhpClass> directSubclasses = PhpIndex.getInstance(project).getDirectSubclasses(parentFqn);
        for (PhpClass directSubclass: directSubclasses) {
            subclasses.addAll(getClassesByParent(directSubclass.getFQN(), project));
        }
        subclasses.addAll(directSubclasses);
        return subclasses;
    }

    private static Collection<PhpClass> getControllers(Project project) {
        Collection<PhpClass> classesByParent = getClassesByParent("\\yii\\web\\Controller", project);
        return classesByParent;
    }

    public static HashMap<String, Method> getRoutes(Project project) {
        Collection<PhpClass> controllers = getControllers(project);
        HashMap<String, Method> routes = new HashMap<>();
        for (PhpClass controller: controllers) {
            if (!excludeControllers.contains(controller.getFQN()))
                routes.putAll(controllerToRoutes(controller));
        }
        return routes;
    }

    private static HashMap<String, Method> controllerToRoutes(PhpClass controller) {
        String part1 = controller.getName().replace("Controller", "").toLowerCase();
        Collection<Method> methods = controller.getMethods();
        HashMap<String, Method> routes = new HashMap<>();
        for (Method method: methods ) {
            if (method.getName().length() > 6 && method.getName().substring(0, 6).equals("action") && Character.isUpperCase(method.getName().charAt(6))) {
                String part2 = method.getName().replace("action", "");
                part1 = StringUtils.CamelToId(part1, "-");
                part2 = StringUtils.CamelToId(part2, "-") ; // part2.replaceAll("(?<=[\\p{Lower}\\p{Digit}])[\\p{Upper}]", "-$0").toLowerCase();
                routes.put(part1 + "/" +part2, method);
            }
        }
        return routes;
    }

    public static Parameter[] getParamsByUrl(String url, Project project) {
        final HashMap<String, Method> routes = getRoutes(project);
        if (routes.containsKey(url)) {
            Method method = routes.get(url);
            return method.getParameters();

        }

        return null;

    }
}
