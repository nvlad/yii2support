package com.yii2support.core;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpReference;
import com.jetbrains.php.lang.psi.stubs.PhpClassStub;
import com.yii2support.common.PhpUtil;

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
        if (applications == null) {
            applications = new HashSet<>();

            PhpIndex index = PhpIndex.getInstance(project);
            Collection<PhpClass> classes = index.getAllSubclasses("\\yii\\base\\Application");

            for (PhpClass aClass : classes) {
                aClass.getFQN();
            }
            final PsiFile[] indexFiles = FilenameIndex.getFilesByName(project, "index.php", GlobalSearchScope.projectScope(project));
            for (PsiFile file : indexFiles) {
                if (file.getContainingDirectory().getName().equals("web")) {
                    Collection<MethodReference> references = PsiTreeUtil.findChildrenOfType(file, MethodReference.class);

                    for (MethodReference reference : references) {
                        if (Objects.equals(reference.getName(), "run")) {
                            PhpClass phpClass = PhpUtil.getPhpClass(reference);
                            if (phpClass != null) {
                                System.out.println(phpClass.getFQN());
                            }
                        }
                    }
                }
            }
            return applications;
        }

        return applications;
    }
}
