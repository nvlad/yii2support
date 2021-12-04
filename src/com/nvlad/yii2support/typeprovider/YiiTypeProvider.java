package com.nvlad.yii2support.typeprovider;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.VariableImpl;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider4;
import com.nvlad.yii2support.common.ClassUtils;
import com.nvlad.yii2support.common.MethodUtils;
import com.nvlad.yii2support.common.PsiUtil;
import com.nvlad.yii2support.configurations.ComponentsIndex;
import com.nvlad.yii2support.objectfactory.ObjectFactoryUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

/**
 * Created by oleg on 2017-06-08.
 */
public class YiiTypeProvider implements PhpTypeProvider4 {
    final static char TRIM_KEY = '\u0197';
    final static char TRIM_KEY2 = '\u0199';
    final static char TRIM_KEY3 = '\u0193';

    @Override
    public char getKey() {
        return '\u0857';
    }

    @Nullable
    @Override
    public PhpType getType(PsiElement psiElement) {
        if (MethodUtils.isYiiCreateObjectMethod(psiElement)) {
            PhpPsiElement firstParam = (PhpPsiElement)MethodUtils.getParameter((MethodReference)psiElement, 0);
            //  Case: Yii::createObject($obj->array_var)
            if (firstParam instanceof FieldReference ) {
                String signature = ((MethodReference)psiElement).getSignature();
                return new PhpType().add("#" + this.getKey() + signature + TRIM_KEY + ((FieldReference) firstParam).getSignature());
            }
            // Case: Yii::createObject($array_var)
            if (firstParam instanceof Variable  && ((VariableImpl) firstParam).getType().getTypes().contains("\\array") ) {
                firstParam = getArrayCreationByVariableRef(firstParam);
            }
            if (firstParam instanceof ArrayCreationExpression) {
                return getClassByArrayCreationOptimized((ArrayCreationExpression) firstParam);
            }
            else {
                return getClass(firstParam);
            }
        }else if(psiElement instanceof FieldReference) {
            String fieldName = PsiUtil.getYiiAppField((FieldReference) psiElement);
            if (fieldName != null) {
                return new PhpType().add("#" + this.getKey() + TRIM_KEY2 + fieldName + TRIM_KEY2);
            }
        }else if(psiElement instanceof Parameter){
            PsiElement el = walkParents(psiElement,5); // Get "'value' => function()" element
            if(el instanceof ArrayHashElement){
                String s = getHashKeyContents(el);
                if(s != null && s.equals("value")){
                    PsiElement top = walkParents(el,6); // Get config array of widget
                    if(top instanceof ArrayCreationExpression){
                        for (PsiElement conf : top.getChildren()){
                            String key = getHashKeyContents(conf);
                            if(key != null && (key.equals("model") || key.equals("filterModel"))){
                                PsiElement val = ((ArrayHashElement) conf).getValue();
                                if(val instanceof Variable){
                                    String signature = ((Variable) val).getSignature();
                                    return new PhpType().add("#" + this.getKey() + TRIM_KEY3 + signature + TRIM_KEY3);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable PhpType complete(String s, Project project) {
        PhpType phpType = new PhpType();

        int trimIndex = s.indexOf(TRIM_KEY);
        if (trimIndex > -1 && s.length() + 1 > trimIndex) {
            String origSignature = s.substring(0, trimIndex);
            if (origSignature.endsWith("\\Yii.createObject")) {
                String variableRef = s.substring(trimIndex + 1);
                final Collection<? extends PhpNamedElement> indexedVariabled = PhpIndex.getInstance(project).getBySignature(variableRef);
                for (PhpNamedElement elem : indexedVariabled) {
                    if (elem instanceof Field) {
                        Field field = (Field) elem;
                        if (field.getLastChild() instanceof ArrayCreationExpression) {
                            PhpClass classByArray = ObjectFactoryUtils.findClassByArray((ArrayCreationExpression) field.getLastChild());
                            if(classByArray != null) {
                                phpType.add(classByArray.getType());
                            }
                        }
                    }
                }
            }
        }else {
            int trimIndexStart = s.indexOf(TRIM_KEY2);
            int trimIndexEnd = s.lastIndexOf(TRIM_KEY2);
            if (trimIndexStart > -1 && trimIndexStart < trimIndexEnd) {
                String fieldName = s.substring(trimIndexStart + 1, trimIndexEnd);
                final GlobalSearchScope scope = GlobalSearchScope.projectScope(project);

                for (String className : FileBasedIndex.getInstance().getValues(ComponentsIndex.identity, fieldName, scope)) {
                    for (PhpClass phpClass : PhpIndex.getInstance(project).getAnyByFQN(className)) {
                        phpType.add(phpClass.getType());
                    }
                }
            }else{
                trimIndexStart = s.indexOf(TRIM_KEY3);
                trimIndexEnd = s.lastIndexOf(TRIM_KEY3);
                if (trimIndexStart > -1 && trimIndexStart < trimIndexEnd) {
                    String signature = s.substring(trimIndexStart + 1, trimIndexEnd);
                    for (PhpNamedElement elem : PhpIndex.getInstance(project).getBySignature(signature)) {
                        if(elem instanceof PhpClass) {
                            phpType.add(elem.getType());
                        }
                    }
                }
            }
        }
        return phpType;
    }

    @NotNull
    private PhpPsiElement getArrayCreationByVariableRef(PhpPsiElement firstParam) {
        Collection<? extends PhpNamedElement> localResolvedVariables = ((VariableImpl) firstParam).resolveLocal();
        PhpNamedElement firstElem = localResolvedVariables.iterator().next();
        if (firstElem instanceof Variable) {
            Variable variableDecl = (Variable)firstElem;
            if (variableDecl.getParent() != null) {
                PsiElement array = variableDecl.getParent().getLastChild();
                if (array instanceof ArrayCreationExpression) {
                    firstParam = (ArrayCreationExpression) array;
                }
            }
        }
        return firstParam;
    }

    @Nullable
    private PhpType getClassByArrayCreationOptimized(ArrayCreationExpression arrayCreationExpr) {
        for (ArrayHashElement elem : arrayCreationExpr.getHashElements()) {
            if (elem.getKey() != null && elem.getKey().getText() != null &&
                    ClassUtils.removeQuotes(elem.getKey().getText()).equals("class")) {
                return getClass(elem.getValue());
            }
        }
        return null;
    }

    private PhpType getClass(PhpPsiElement elem) {
        if (elem instanceof ClassConstantReference) {
            if (elem.getName() != null && elem.getName().equals("class")
                    && ((ClassConstantReference) elem).getClassReference() != null)
                return ((ClassConstantReference) elem).getClassReference().getType();
        }
        if (elem instanceof MethodReference) {
            if (elem.getName() != null && elem.getName().equals("className")
                    && ((MethodReference) elem).getClassReference() != null)
                return ((MethodReference) elem).getClassReference().getType();
        }

        return null;
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String s, Set<String> set, int i, Project project) {
        return null;
    }

    @Nullable
    private PsiElement walkParents(PsiElement el, int level) {
        PsiElement element = el;
        for (int i = 0; i < level; i++) {
            if (element == null) {
                return null;
            }
            element = element.getParent();
        }
        return element;
    }

    @Nullable
    private String getHashKeyContents(PsiElement e) {
        if (e instanceof ArrayHashElement) {
            PhpPsiElement key = ((ArrayHashElement) e).getKey();
            if (key instanceof StringLiteralExpression) {
                return ((StringLiteralExpression) key).getContents();
            }
        }
        return null;
    }
}
