package com.bilibili.following.prvcompiler.util;

import com.bilibili.following.prvcompiler.info.ResourceInfo;
import com.squareup.javapoet.ClassName;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class ResourceUtils {

    private static Elements elementUtils;
    private static Types typeUtils;
    private static Trees trees;
    private static AnnotationResourceParamScanner scanner = new AnnotationResourceParamScanner();

    public static void init(ProcessingEnvironment processingEnv, Elements element, Types type) {
        elementUtils = element;
        typeUtils = type;

        try {
            trees = Trees.instance(processingEnv);
        } catch (IllegalArgumentException ignored) {

        }
    }

    public static ResourceInfo getLayoutsInAnnotation(Element element, Class annotationClass) {
        AnnotationMirror mirror = null;
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (annotationMirror.getAnnotationType().toString()
                    .equals(annotationClass.getCanonicalName())) {
                mirror = annotationMirror;
            }
        }

        if (mirror == null) {
            return null;
        }

        List<ResourceInfo> resources = new ArrayList<>();
        JCTree tree = (JCTree) trees.getTree(element, mirror);

        if (tree != null) {
            scanner.clearResults();
            scanner.setCurrentAnnotationDetails("layout");
            tree.accept(scanner);
            resources.addAll(scanner.getResults());
        }

        return resources.get(0);
    }

    private static class AnnotationResourceParamScanner extends TreeScanner {

        private final List<ResourceInfo> results = new ArrayList<>();
        private String resourceType;

        void clearResults() {
            results.clear();
        }

        List<ResourceInfo> getResults() {
            return results;
        }

        void setCurrentAnnotationDetails(String resourceType) {
            this.resourceType = resourceType;
        }

        @Override
        public void visitSelect(JCTree.JCFieldAccess jcFieldAccess) {
            Symbol symbol = jcFieldAccess.sym;

            if (symbol instanceof Symbol.VarSymbol
                    && symbol.getEnclosingElement() != null // The R.resourceType class
                    && symbol.getEnclosingElement().getEnclosingElement() != null // The R class
                    && symbol.getEnclosingElement().getEnclosingElement().enclClass() != null) {

                ResourceInfo result = parseResourceSymbol((VariableElement) symbol);
                if (result != null) {
                    results.add(result);
                }
            }
        }

        private ResourceInfo parseResourceSymbol(VariableElement symbol) {
            TypeElement resourceClass = (TypeElement) symbol.getEnclosingElement();

            String rClass = ((TypeElement) resourceClass.getEnclosingElement()).getQualifiedName().toString();

            String resourceClassName = resourceClass.getQualifiedName().toString();

            if (!(rClass + "." + resourceType).equals(resourceClassName)) {
                return null;
            }

            String resourceName = symbol.getSimpleName().toString();

            Object resourceValue = symbol.getConstantValue();
            if (!(resourceValue instanceof Integer)) {
                return null;
            }

            ClassName rClassName = getClassName(resourceClassName, resourceType);

            return new ResourceInfo(rClassName, resourceName);
        }

        private ClassName getClassName(String rClass, String resourceType) {
            ClassName className;
            Element rClassElement = getElementByName(rClass, elementUtils, typeUtils);

            String rClassPackageName = elementUtils.getPackageOf(rClassElement).getQualifiedName().toString();
            className = ClassName.get(rClassPackageName, "R", resourceType);

            return className;
        }

        private Element getElementByName(String name, Elements elements, Types types) {
            try {
                return elements.getTypeElement(name);
            } catch (MirroredTypeException mte) {
                return types.asElement(mte.getTypeMirror());
            }
        }
    }

}
