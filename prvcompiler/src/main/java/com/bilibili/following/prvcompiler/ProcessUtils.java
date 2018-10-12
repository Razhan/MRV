package com.bilibili.following.prvcompiler;

import com.bilibili.following.prvannotations.PrvBinder;
import com.bilibili.following.prvannotations.PrvItemBinder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public class ProcessUtils {

    private static Messager messager;
    private static Types types;
    private static Elements elements;

    static void init(Messager msger, Types typeUtils, Elements elementUtils) {
        messager = msger;
        types = typeUtils;
        elements = elementUtils;
    }

    @SuppressWarnings("ConstantConditions")
    static Set<Pair<TypeElement, List<TypeName>>> getItemBinderSet(RoundEnvironment env) {
        Set<Pair<TypeElement, List<TypeName>>> itemBinderSet = new LinkedHashSet<>();

        for (Element element : env.getElementsAnnotatedWith(PrvItemBinder.class)) {
            if (!(element instanceof TypeElement)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Annotation PrvItemBinder should target on a Class");
            }

            List<TypeName> binders = new ArrayList<>();
            PrvItemBinder annotation = element.getAnnotation(PrvItemBinder.class);
            try {
                annotation.value();
            } catch (MirroredTypesException ex) {
                List<? extends TypeMirror> types = ex.getTypeMirrors();
                for (TypeMirror type : types) {
                    binders.add(ClassName.get(type));
                }
            }

            // TODO: 10/11/18 检验binder是否重复
            itemBinderSet.add(Pair.create((TypeElement) element, binders));
        }

        return itemBinderSet;
    }

    static Map<TypeElement, Integer> getBinderSet(RoundEnvironment env) {
        Map<TypeElement, Integer> binderSet = new HashMap<>();

        for (Element element : env.getElementsAnnotatedWith(PrvBinder.class)) {
            if (!(element instanceof TypeElement)) {
                throw new IllegalArgumentException("Annotation PrvBinder should target on a Class");
            }

            // TODO: 10/11/18 检验布局是否重复，布局作为TYPE ID，不可重复
            PrvBinder annotation = element.getAnnotation(PrvBinder.class);
            if (annotation != null) {
                binderSet.put((TypeElement) element, annotation.value());
            }
        }

        return binderSet;
    }

    static List<? extends TypeMirror> getGenericTypes(TypeElement element) {
        TypeElement typeElement = element;
        List<? extends TypeMirror> typeMirrors;

        List<? extends TypeMirror> interfaceTypes = typeElement.getInterfaces();
        if (interfaceTypes == null || interfaceTypes.isEmpty()) {
            while (true) {
                TypeMirror superClass = typeElement.getSuperclass();
                if (superClass.getKind() == TypeKind.NONE) {
                    return null;
                }

                typeMirrors = ((DeclaredType) superClass).getTypeArguments();

                if (typeMirrors == null || typeMirrors.isEmpty()) {
                    typeElement = (TypeElement) ((DeclaredType) superClass).asElement();
                } else {
                    return typeMirrors;
                }
            }
        } else {
            typeMirrors = ((DeclaredType) interfaceTypes.get(0)).getTypeArguments();
        }

        return typeMirrors;
    }

    static String getRootModuleString(Element element) {
        PackageElement packageOf = elements.getPackageOf(element);
        String packageName = packageOf.getQualifiedName().toString() ;

        while (true) {
            if (packageName.lastIndexOf(".") > 0) {
                packageName = packageName.substring(0, packageName.lastIndexOf("."));

                Element rClass = getElementByName(packageName + ".R", elements, types);
                if (rClass != null) {
                    return packageName;
                }
            } else {
               break;
            }
        }

        return null;
    }

    private static Element getElementByName(String name, Elements elements, Types types) {
        try {
            return elements.getTypeElement(name);
        } catch (MirroredTypeException mte) {
            return types.asElement(mte.getTypeMirror());
        }
    }

}