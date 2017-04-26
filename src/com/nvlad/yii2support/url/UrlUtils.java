package com.nvlad.yii2support.url;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by oleg on 25.04.2017.
 */
public class UrlUtils {
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
                part2 = part2.replaceAll("(?<=[\\p{Lower}\\p{Digit}])[\\p{Upper}]", "-$0").toLowerCase();
                routes.put(part1 + "/" +part2, method);
            }
        }
        return routes;
    }
}
