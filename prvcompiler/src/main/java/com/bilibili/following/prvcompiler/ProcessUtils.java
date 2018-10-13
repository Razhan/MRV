package com.bilibili.following.prvcompiler;

import com.bilibili.following.prvannotations.None;
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
import javax.lang.model.type.ExecutableType;
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
    static Set<ItemBinderInfo> getItemBinderSet(RoundEnvironment env) {
        Set<ItemBinderInfo> itemBinderSet = new LinkedHashSet<>();

        for (Element element : env.getElementsAnnotatedWith(PrvItemBinder.class)) {
            if (!(element instanceof TypeElement)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Annotation PrvItemBinder should target on a Class");
            }

            itemBinderSet.add(getItemBinderInfo((TypeElement) element));
        }

        return itemBinderSet;
    }

    static Map<TypeElement, Integer> getBinderSet(RoundEnvironment env) {
        Map<TypeElement, Integer> binderSet = new HashMap<>();

        for (Element element : env.getElementsAnnotatedWith(PrvBinder.class)) {
            if (!(element instanceof TypeElement)) {
                throw new IllegalArgumentException("Annotation PrvBinder should target on a Class");
            }

            // TODO: 10/11/18 check duplicated layout
            PrvBinder annotation = element.getAnnotation(PrvBinder.class);
            if (annotation != null) {
                binderSet.put((TypeElement) element, annotation.value());
            }
        }

        return binderSet;
    }

    static List<? extends TypeMirror> getClassGenericTypes(TypeElement element) {
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

    // TODO: 10/13/2018 need to test code
    private static ItemBinderInfo getItemBinderInfo(TypeElement element) {
        List<? extends TypeMirror> interfaces = element.getInterfaces();

        boolean hasItemBinderInterface = false;
        List<String> overrideMethods = new ArrayList<>();
        List<TypeName> binders = new ArrayList<>();
        TypeName dataType;

        if (interfaces == null || interfaces.isEmpty()) {
            hasItemBinderInterface = false;
        } else {
            for (TypeMirror interfaceType : interfaces) {
                TypeElement interfaceElement = (TypeElement) ((DeclaredType) interfaceType).asElement();
                if (interfaceElement.toString().equals(NameStore.ITEM_BINDER)) {
                    hasItemBinderInterface = true;
                    overrideMethods = getOverrideMethodNames(interfaceElement, element);
                    break;
                }
            }
        }

        PrvItemBinder annotation = element.getAnnotation(PrvItemBinder.class);
        try {
            annotation.binder();
        } catch (MirroredTypesException ex) {
            List<? extends TypeMirror> types = ex.getTypeMirrors();
            for (TypeMirror type : types) {
                binders.add(ClassName.get(type));
            }

            if (overrideMethods.isEmpty() && binders.isEmpty()) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Annotation PrvItemBinder must has corresponding binders");
                return null;
            }
        }

        try {
            dataType = ClassName.get(annotation.type());
        } catch (MirroredTypeException ex) {
            dataType = ClassName.get(ex.getTypeMirror());
        }

        if (dataType.toString().equals(None.class.getName())) {
            List<? extends TypeMirror> genericTypes = getClassGenericTypes(element);
            if (genericTypes == null || genericTypes.isEmpty()) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Annotation PrvItemBinder must has a corresponding data type");
                return null;
            }
            dataType = ClassName.bestGuess(genericTypes.get(0).toString());
        }

        return new ItemBinderInfo(element, dataType, binders, hasItemBinderInterface, overrideMethods);
    }

    private static Element getElementByName(String name, Elements elements, Types types) {
        try {
            return elements.getTypeElement(name);
        } catch (MirroredTypeException mte) {
            return types.asElement(mte.getTypeMirror());
        }
    }

    private static List<String> getOverrideMethodNames(TypeElement interfaceElement, TypeElement classElement) {
        List<String> interfaceMethods  = new ArrayList<>();
        for (Element element :  interfaceElement.getEnclosedElements()) {
            if (element.asType() instanceof ExecutableType) {
                interfaceMethods.add(element.getSimpleName().toString());
            }
        }

        List<String> overrideMethods  = new ArrayList<>();


        while (true) {
            TypeMirror superClass = classElement.getSuperclass();
            if (superClass.getKind() == TypeKind.NONE) {
                break;
            }

            for (Element element : classElement.getEnclosedElements()) {
                if (element.asType() instanceof ExecutableType && element.getAnnotation(Override.class) != null) {
                    overrideMethods.add(element.getSimpleName().toString());
                }
            }

            classElement = (TypeElement) ((DeclaredType) superClass).asElement();
        }

        List<String> res  = new ArrayList<>();
        for (String method : overrideMethods) {
            if (interfaceMethods.contains(method)) {
                res.add(method);
            }
        }

        return res;
    }

}