package com.nvlad.yii2support.views.util;

import com.google.common.io.Files;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.PhpUtil;
import com.nvlad.yii2support.common.StringUtils;
import com.nvlad.yii2support.common.YiiApplicationUtils;
import com.nvlad.yii2support.utils.Yii2SupportSettings;
import com.nvlad.yii2support.views.entities.ViewResolve;
import com.nvlad.yii2support.views.entities.ViewResolveFrom;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.InvalidPathException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewUtil {
    private static final Set<String> ignoredVariables = getIgnoredVariables();
    private static final Map<Project, Map<Pattern, String>> projectViewPatterns = new HashMap<>();

    public static final String[] renderMethods = {"render", "renderAjax", "renderPartial"};

    @Nullable
    public static ViewResolve resolveView(VirtualFile virtualFile, Project project) {
        final String projectPath = YiiApplicationUtils.getYiiRootPath(project);
        if (projectPath == null) {
            return null;
        }

        int projectBaseDirLength = projectPath.length();
        final String absolutePath = virtualFile.getPath();
        if (!absolutePath.startsWith(projectPath)) {
            return null;
        }

        String path = absolutePath.substring(projectBaseDirLength);
        if (!path.startsWith("/vendor/")) {
            ViewResolve result = new ViewResolve();
            result.application = YiiApplicationUtils.getApplicationName(virtualFile, project);
            result.theme = "";
            if (path.startsWith("/" + result.application + "/")) {
                path = path.substring(result.application.length() + 1);
            }

            result.relativePath = path;
            path = "@app" + path;
            if (!path.startsWith("@app/views/")
                    && !(path.startsWith("@app/modules/") && path.contains("/views/"))
                    && !(path.startsWith("@app/widgets/") && path.contains("/views/"))) {
                String viewPath = null;
                for (Map.Entry<Pattern, String> entry : ViewUtil.getPatterns(project).entrySet()) {
                    Matcher matcher = entry.getKey().matcher(path);
                    if (matcher.find()) {
                        viewPath = entry.getValue() + path.substring(matcher.end(1));
                        result.theme = matcher.group(2);
                        break;
                    }
                }
                if (viewPath == null) {
                    return null;
                }
                path = viewPath;
            }
            result.key = path;

            return result;
        }

        return null;
    }

    @Nullable
    public static ViewResolve resolveView(PsiElement element) {
        String value = PhpUtil.getValue(element);
        if (value.startsWith("@")) {
            ViewResolve resolve = new ViewResolve(value);
            resolve.application = YiiApplicationUtils.getApplicationName(element.getContainingFile());
            return resolve;
        }
        if (value.startsWith("//")) {
            ViewResolve resolve = new ViewResolve("@app/views" + value.substring(1));
            resolve.application = YiiApplicationUtils.getApplicationName(element.getContainingFile());
            return resolve;
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
        final ViewResolve viewResolve;
        try {
            if (callerClass.getName().endsWith("Controller") && ClassUtils.isClassInheritsOrEqual(callerClass, "\\yii\\base\\Controller", phpIndex)) {
                viewResolve = resolveViewFromController(callerClass, value);
            } else if (ClassUtils.isClassInheritsOrEqual(callerClass, "\\yii\\base\\View", phpIndex)) {
                viewResolve = resolveViewFromView(element, value);
            } else if (ClassUtils.isClassInheritsOrEqual(callerClass, "\\yii\\base\\Widget", phpIndex)) {
                viewResolve = resolveViewFromWidget(callerClass, value);
            } else {
                return null;
            }
        } catch (InvalidPathException e) {
            return null;
        }

        viewResolve.application = YiiApplicationUtils.getApplicationName(element.getContainingFile());
        return viewResolve;
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
    public static Collection<String> viewResolveToPaths(@NotNull ViewResolve resolve, @NotNull Project project) {
        Set<String> result = new HashSet<>();

        String path = resolve.key;
        if (Files.getFileExtension(path).isEmpty()) {
            path = path + '.' + Yii2SupportSettings.getInstance(project).defaultViewExtension;
        }
        if (path.startsWith("@app/")) {
            path = path.substring(4);
        }

        String projectUrl = project.getBaseDir().getUrl();
        VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
        if (virtualFileManager.findFileByUrl(projectUrl + "/web") == null) {
            path = '/' + resolve.application + path;
        }
        result.add(path);

        return result;
    }

    public static void resetPathMapPatterns(Project project) {
        projectViewPatterns.remove(project);
    }

    @NotNull
    private static Map<Pattern, String> getPatterns(Project project) {
        Map<Pattern, String> patterns = projectViewPatterns.get(project);
        if (patterns == null) {
            patterns = new LinkedHashMap<>();
            Yii2SupportSettings settings = Yii2SupportSettings.getInstance(project);
            for (Map.Entry<String, String> entry : settings.viewPathMap.entrySet()) {
                String patternString = "^(" + entry.getKey().replace("*", "([\\w-]+)") + ").+";
                Pattern pattern = Pattern.compile(patternString);
                patterns.put(pattern, entry.getValue());
            }
            projectViewPatterns.put(project, patterns);
        }
        return patterns;
    }

    @NotNull
    private static ViewResolve resolveViewFromController(PhpClass clazz, String value) {
        ViewResolve result = new ViewResolve(ViewResolveFrom.Controller);
        final String classFQN = clazz.getFQN().replace('\\', '/');
        StringBuilder key = new StringBuilder("@app");
        String path = deletePathPart(classFQN);
        if (path.startsWith("/modules/")) {
            key.append("/modules");
            path = deletePathPart(path);
        }
        int controllersPathPartPosition = path.indexOf("/controllers/");
        if (controllersPathPartPosition == -1) {
            throw new InvalidPathException(path, "Not found \"controllers\" directory.");
        }
        result.application = getFirstPathPart(classFQN);
        if (controllersPathPartPosition > 0) {
            final String module = path.substring(0, controllersPathPartPosition);
            key.append(module);
            path = path.substring(controllersPathPartPosition);
            result.module = module;
        }
        path = deletePathPart(path);
        key.append("/views");
        if (value.startsWith("/")) {
            result.key = normalizePath(key + value);
            return result;
        }
        final String className = clazz.getName();
        key.append(path, 0, path.length() - className.length());
        key.append(StringUtils.CamelToId(className.substring(0, className.length() - 10), "-"));
        key.append('/');
        key.append(value);
        result.key = normalizePath(key.toString());

        return result;
    }

    @NotNull
    private static ViewResolve resolveViewFromView(PsiElement element, String value) {
        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
        if (virtualFile == null) {
            virtualFile = element.getContainingFile().getOriginalFile().getVirtualFile();
        }
        ViewResolve result = resolveView(virtualFile, element.getProject());
        if (result == null) {
            throw new InvalidPathException(virtualFile.getPath(), "Not resolved");
        }

        result.from = ViewResolveFrom.View;
        if (value.startsWith("/")) {
            int viewsPathPartPosition = result.key.lastIndexOf("/views/");
            if (viewsPathPartPosition == -1) {
                throw new InvalidPathException(result.key, "Not found \"views\" directory");
            }
            result.key = result.key.substring(0, viewsPathPartPosition + 6) + value;
            return result;
        }

        int lastSlashPosition = result.key.lastIndexOf('/');
        result.key = normalizePath(result.key.substring(0, lastSlashPosition + 1) + value);
        return result;
    }

    @NotNull
    private static ViewResolve resolveViewFromWidget(PhpClass clazz, String value) {
        ViewResolve result = new ViewResolve(ViewResolveFrom.Widget);
        final String classFQN = clazz.getFQN().replace('\\', '/');
        StringBuilder key = new StringBuilder("@app");
        String path = deletePathPart(classFQN);
        final int widgetsPathPartPosition = path.indexOf("/widgets/");
        if (widgetsPathPartPosition == -1) {
            throw new InvalidPathException(path, "Not found \"widgets\" directory.");
        }
        result.application = getFirstPathPart(classFQN);
        if (widgetsPathPartPosition > 0) {
            final String modulePath = path.substring(0, widgetsPathPartPosition);
            key.append(modulePath);
            result.module = modulePath.substring(modulePath.lastIndexOf('/') + 1);
            path = path.substring(modulePath.length());
        }

        if (value.startsWith("/")) {
            key.append("/views");
        } else {
            key.append("/widgets");
            path = deletePathPart(path);
            key.append(path, 0, path.length() - clazz.getName().length());
            key.append("views/");
        }
        key.append(value);
        result.key = normalizePath(key.toString());
        return result;
    }

    private static String deletePathPart(String path) {
        int returnFromPosition = path.indexOf('/', path.startsWith("/") ? 1 : 0);
        return returnFromPosition == -1 ? path : path.substring(returnFromPosition);
    }

    private static String getFirstPathPart(String path) {
        int start = path.startsWith("/") ? 1 : 0;
        int end = path.indexOf('/', start) - 1;
        return end == -1 ? path : path.substring(start, end);
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

    private static Set<String> getIgnoredVariables() {
        final Set<String> set = new HashSet<>(Arrays.asList("this", "_file_", "_params_"));
        set.addAll(Variable.SUPERGLOBALS);
        return set;
    }
}
