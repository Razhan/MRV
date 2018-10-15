package com.bilibili.following.prvcompiler.util;

import com.bilibili.following.prvannotations.None;
import com.bilibili.following.prvannotations.PrvAttribute;
import com.bilibili.following.prvannotations.PrvBinder;
import com.bilibili.following.prvannotations.PrvItemBinder;
import com.bilibili.following.prvcompiler.info.BindingModelInfo;
import com.bilibili.following.prvcompiler.info.GeneratedModelInfo;
import com.bilibili.following.prvcompiler.info.ItemBinderInfo;
import com.bilibili.following.prvcompiler.info.ResourceInfo;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static com.bilibili.following.prvcompiler.util.StringUtils.PATTERN_STARTS_WITH_SET;

public class ProcessUtils {

    private static Messager messager;
    private static Types types;
    private static Elements elements;

    private static List<String> excludedFields = new ArrayList<>(Arrays.asList("lifecycleOwner"));

    public static void init(Messager msger, Types typeUtils, Elements elementUtils) {
        messager = msger;
        types = typeUtils;
        elements = elementUtils;
    }

    @SuppressWarnings("ConstantConditions")
    public static Set<ItemBinderInfo> getItemBinderSet(RoundEnvironment env) {
        Set<ItemBinderInfo> itemBinderSet = new LinkedHashSet<>();

        for (Element element : env.getElementsAnnotatedWith(PrvItemBinder.class)) {
            if (!(element instanceof TypeElement)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Annotation PrvItemBinder should target on a Class");
            }

            itemBinderSet.add(getItemBinderInfo((TypeElement) element));
        }

        return itemBinderSet;
    }

    public static Map<TypeElement, Integer> getBinderSet(RoundEnvironment env) {
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

    public static List<? extends TypeMirror> getClassGenericTypes(TypeElement element) {
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

    public static String getRootModuleString(Element element) {
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

    public static Map<TypeElement, Set<BindingModelInfo>> getBindingModelSet(RoundEnvironment env) {
        Map<TypeElement, Set<BindingModelInfo>> BindingModelMap = new HashMap<>();

        for (Element element : env.getElementsAnnotatedWith(PrvAttribute.class)) {
            if (!(element instanceof VariableElement)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Annotation PrvAttribute should target on a Field");
            }

            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            Set<BindingModelInfo> attributeSet;
            if (BindingModelMap.containsKey(typeElement)) {
                attributeSet = BindingModelMap.get(typeElement);
            } else {
                attributeSet = new HashSet<>();
            }

            String fieldName = element.getSimpleName().toString();
            TypeMirror typeMirror = element.asType();

            attributeSet.add(new BindingModelInfo(fieldName, typeMirror));
            BindingModelMap.put(typeElement, attributeSet);
        }

        return BindingModelMap;
    }

    private static ClassName getDataBindingClassNameForResource(ResourceInfo info, String moduleName) {
        String modelName = StringUtils.toUpperCamelCase(info.resourceName).concat(NameStore.BINDING_SUFFIX);

        return ClassName.get(moduleName.concat("." + NameStore.DATA_BINDING), modelName);
    }

    private static String getDataBindingClassNameStringForResource(ResourceInfo info) {
        String[] strArray = info.resourceName.split("_");
        return StringUtils.toUpperCamelCase(strArray[strArray.length - 1]).concat(NameStore.BINDING_MODEL_SUFFIX);
    }

    private static TypeElement getElementByName(ClassName name) {
        String canonicalName = name.reflectionName().replace("$", ".");
        return (TypeElement) getElementByName(canonicalName, elements, types);
    }

    public static boolean isSetterMethod(Element element) {
        if (element.getKind() != ElementKind.METHOD) {
            return false;
        }

        ExecutableElement method = (ExecutableElement) element;
        String methodName = method.getSimpleName().toString();

        return !excludedFields.contains(methodName) && PATTERN_STARTS_WITH_SET.matcher(methodName).matches()
                && method.getParameters().size() == 1;
    }

    public static GeneratedModelInfo getGeneratedModelInfo(TypeElement typeElement, ResourceInfo resource, String rootPackage) {

        ClassName dataBindingClassName = getDataBindingClassNameForResource(resource, rootPackage);
        TypeElement dataBinding = getElementByName(dataBindingClassName);

        //第一轮解析dataBinding为null
        if (dataBinding == null) {
            return null;
        }

        GeneratedModelInfo modelInfo = new GeneratedModelInfo();
        modelInfo.setPackageName(ClassName.get(typeElement).packageName());
        modelInfo.setClassName(ProcessUtils.getDataBindingClassNameStringForResource(resource));

        List<BindingModelInfo> bindingModelInfo = new ArrayList<>();
        for (Element element: dataBinding.getEnclosedElements()) {
            if (element.getKind() != ElementKind.METHOD) {
                continue;
            }

            if (ProcessUtils.isSetterMethod(element)) {
                String name = StringUtils.removeSetPrefix(element.getSimpleName().toString());
                TypeMirror typeMirror = ((ExecutableElement) element).getParameters().get(0).asType();
                bindingModelInfo.add(new BindingModelInfo(name, typeMirror));
            }
        }

        modelInfo.setBindingModelInfo(bindingModelInfo);
        return modelInfo;
    }

    // TODO: 10/13/2018 need to test code
    private static ItemBinderInfo getItemBinderInfo(TypeElement element) {
        List<? extends TypeMirror> interfaces = element.getInterfaces();

        boolean hasItemBinderInterface = false;
        List<String> overrideMethods = new ArrayList<>();
        List<TypeName> binders = new ArrayList<>();
        TypeName dataType;

        if (interfaces != null && !interfaces.isEmpty()) {
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