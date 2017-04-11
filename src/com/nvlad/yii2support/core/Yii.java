package com.nvlad.yii2support.core;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.nvlad.yii2support.common.PhpUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by NVlad on 03.02.2017.
 */
public class Yii {
    static HashSet<Application> applications;

    public static Set<Application> applications(Project project) {
        if (applications == null || applications.isEmpty()) {
            applications = new HashSet<>();

            PhpIndex index = PhpIndex.getInstance(project);
            Collection<PhpClass> classes = index.getAllSubclasses("\\yii\\base\\Application");

            for (PhpClass aClass : classes) {
                aClass.getFQN();
            }
            PsiFile[] indexFiles = FilenameIndex.getFilesByName(project, "index.php", GlobalSearchScope.projectScope(project));
            findApplications(indexFiles);
            indexFiles = FilenameIndex.getFilesByName(project, "yii", GlobalSearchScope.projectScope(project));
            findApplications(indexFiles);

            return applications;
        }

        return applications;
    }

    public static void clear() {
        applications.clear();
    }

    public static boolean frameworkDetected() {
        return applications != null && !applications.isEmpty();
    }

    private static void findApplications(PsiFile[] files) {
        for (PsiFile file : files) {
            if (file.getContainingDirectory().getName().equals("web")) {
                Collection<MethodReference> references = PsiTreeUtil.findChildrenOfType(file, MethodReference.class);

                for (MethodReference reference : references) {
                    if (Objects.equals(reference.getName(), "run")) {
                        PhpClass phpClass = PhpUtil.getPhpClass(reference);
                        if (phpClass != null) {
                            if (!reference.getContainingFile().getVirtualFile().getCanonicalPath().contains("/environments/")) {
                                Application application = new Application(reference.getContainingFile());
                                applications.add(application);
                                System.out.print(application.getPath() + ": ");
                                System.out.println(phpClass.getFQN());
                            }
                        }
                    }
                }
            }
        }
    }
}
