package com.bilibili.following.prvcompiler;

import com.bilibili.following.prvannotations.Keep;
import com.bilibili.following.prvannotations.PrvAdapter;
import com.bilibili.following.prvannotations.PrvAttribute;
import com.bilibili.following.prvannotations.PrvBinder;
import com.bilibili.following.prvannotations.PrvItemBinder;
import com.bilibili.following.prvcompiler.info.AdapterInfo;
import com.bilibili.following.prvcompiler.info.BindingModelInfo;
import com.bilibili.following.prvcompiler.info.GeneratedModelInfo;
import com.bilibili.following.prvcompiler.info.ItemBinderInfo;
import com.bilibili.following.prvcompiler.util.NameStore;
import com.bilibili.following.prvcompiler.util.ProcessUtils;
import com.bilibili.following.prvcompiler.util.ResourceUtils;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class)
public class PrvProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private Elements elements;
    private Types types;

    private ClassName listClass;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        elements = processingEnv.getElementUtils();
        types = processingEnvironment.getTypeUtils();

        listClass = ClassName.get(List.class);

        ProcessUtils.init(messager, types, elements);
        ResourceUtils.init(processingEnvironment, elements, types);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (roundEnvironment.processingOver()) {
            if (!set.isEmpty()) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "Unexpected processing state: annotations still available after processing over");
                return false;
            }
        }

        if (set.isEmpty()) {
            return false;
        }

        try {
            Set<ItemBinderInfo> itemBinderInfoSet = ProcessUtils.getItemBinderSet(roundEnvironment);
            Map<TypeElement, Integer> binderMap = ProcessUtils.getBinderMap(roundEnvironment);
            Map<TypeElement, TypeElement> adapterList = ProcessUtils.getAdapterList(roundEnvironment);

            generateItemBinder(itemBinderInfoSet);
            generateBinder(binderMap);
            generateAdapter(adapterList, itemBinderInfoSet);

            generateBinderModel(ProcessUtils.getBindingModelSet(roundEnvironment));
        } catch (RuntimeException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR, "Unexpected error in PrvProcessor: " + e);
        }

        return true;
    }

    //生成Adapter实现基类
    private void generateAdapter(Map<TypeElement, TypeElement> adapterList, Set<ItemBinderInfo> itemBinderInfoSet) {
        if (adapterList == null || adapterList.size() <= 0 || itemBinderInfoSet == null || itemBinderInfoSet.size() <= 0) {
            return;
        }

        List<AdapterInfo> adapterInfoList = ProcessUtils.getAdapterInfoList(adapterList, itemBinderInfoSet);

        for (AdapterInfo adapterInfo : adapterInfoList) {
            String packageName = ClassName.get(adapterInfo.adapter).packageName();
            String baseTypeName = adapterInfo.dataType.getSimpleName().toString();
            String className = String.format(NameStore.ADAPTER_NAME, baseTypeName);
            ClassName baseAdapterClass = ClassName.bestGuess(NameStore.ADAPTER);

            TypeName superAdapter = ParameterizedTypeName.get(baseAdapterClass, ClassName.get(adapterInfo.dataType));

            MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                    .addModifiers(PUBLIC)
                    .addStatement("super()")
                    .addCode("\n");

            for (ItemBinderInfo itemBinderInfo : adapterInfo.itemBinderInfoList) {
                ClassName implItemBinder = ClassName.bestGuess(itemBinderInfo.itemBinder.toString().concat(NameStore.AUTO_IMPL_SUFFIX));
                builder.addStatement("registerItemBinder($T.class, new $T())", itemBinderInfo.dataType, implItemBinder);
            }

            builder.addCode("\n");

            for (TypeElement binder : adapterInfo.binderList) {
                ClassName implBinder = ClassName.bestGuess(binder.toString().concat(NameStore.AUTO_IMPL_SUFFIX));
                builder.addStatement("registerBinder($L, $T.getInstance())", binder.getAnnotation(PrvBinder.class).value(), implBinder);
            }

            //生成的抽象类命为BaseXXXXPrvAdapter, XXXX为数据基类类型
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                    .addModifiers(PUBLIC, ABSTRACT)
                    .addAnnotation(Keep.class)
                    .superclass(superAdapter)
                    .addMethod(builder.build());


            createFile(packageName, classBuilder);
        }
    }

    //生成ItemBinder实现类
    private void generateItemBinder(Set<ItemBinderInfo> itemBinderSet) {
        if (itemBinderSet == null || itemBinderSet.isEmpty()) {
            return;
        }

        ClassName NonNull = ClassName.get("android.support.annotation", "NonNull");
        ClassName viewHolderClass = ClassName.bestGuess(NameStore.VIEWHOLDER);
        ClassName binderClass = ClassName.bestGuess(NameStore.BINDER);
        ClassName itemBinderClass = ClassName.bestGuess(NameStore.ITEM_BINDER);

        String packageName;
        String className;
        String implClassName;
        ClassName itemBinderDataTypeClass;

        for (ItemBinderInfo itemBinder : itemBinderSet) {
            packageName = ClassName.get(itemBinder.itemBinder).packageName();
            className = ClassName.get(itemBinder.itemBinder).simpleName();
            implClassName = className + NameStore.AUTO_IMPL_SUFFIX;

            itemBinderDataTypeClass = ClassName.bestGuess(itemBinder.dataType.toString());
            WildcardTypeName viewHolderWildcardTypeName = WildcardTypeName.subtypeOf(viewHolderClass);
            TypeName binderTypeName = ParameterizedTypeName.get(binderClass, itemBinderDataTypeClass, viewHolderWildcardTypeName);

            WildcardTypeName binderWildcardTypeName = WildcardTypeName.subtypeOf(binderTypeName);
            TypeName resTypeName = ParameterizedTypeName.get(listClass, binderWildcardTypeName);

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getBinderList")
                    .addModifiers(PUBLIC)
                    .returns(resTypeName)
                    .addAnnotation(AnnotationSpec.builder(NonNull).build())
                    .addAnnotation(Override.class)
                    .addParameter(itemBinderDataTypeClass, "model")
                    .addParameter(int.class, "position")
                    .addCode("return new $T<>($T.asList(", ArrayList.class, Arrays.class);

            for (int i = 0; i < itemBinder.binderList.size(); i++) {
                TypeElement binderName = itemBinder.binderList.get(i);
                methodBuilder.addCode("$T.getInstance()", ClassName.bestGuess(binderName.toString() + NameStore.AUTO_IMPL_SUFFIX));

                if (i != itemBinder.binderList.size() - 1) {
                    methodBuilder.addCode(", ");
                } else {
                    methodBuilder.addCode("))").addCode(";");
                }
            }

            methodBuilder.addCode("\n");

            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(implClassName)
                    .addModifiers(PUBLIC, FINAL)
                    .addAnnotation(Keep.class)
                    .superclass(ClassName.get(itemBinder.itemBinder));

            if (!itemBinder.hasImplemented) {
                TypeName interfaceTypeName = ParameterizedTypeName.get(itemBinderClass, itemBinderDataTypeClass, binderTypeName);
                classBuilder.addSuperinterface(interfaceTypeName);
                classBuilder.addMethod(methodBuilder.build());
            }

            createFile(packageName, classBuilder);
        }
    }

    //生成Binder实现类 分2种情况
    private void generateBinder(Map<TypeElement, Integer> binderMap) {
        if (binderMap == null || binderMap.size() <= 0) {
            return;
        }

        for (Map.Entry<TypeElement, Integer> entry : binderMap.entrySet()) {
            if (ProcessUtils.isDataBindingBinder(entry.getKey())) {
                generateDataBindingBinder(entry.getKey(), entry.getValue());
            } else {
                generateNormalBinder(entry.getKey(), entry.getValue());
            }
        }
    }

    //生成DataBindingBinder实现类
    private void generateDataBindingBinder(TypeElement element, int resource) {
        ClassName dataBindingViewHolderClass = ClassName.bestGuess(NameStore.DATA_BINDING_VIEWHOLDER);

        String packageName;
        String className;
        String implClassName;
        ClassName binderDataTypeClass;
        ClassName binderModelTypeClass;

        packageName = ClassName.get(element).packageName();
        className = ClassName.get(element).simpleName();
        implClassName = className + NameStore.AUTO_IMPL_SUFFIX;
        String rootPackage = ProcessUtils.getRootModuleString(element);
        ClassName currentClass = ClassName.get(packageName, implClassName);

        GeneratedModelInfo modelInfo = ProcessUtils.getGeneratedModelInfo(element, ResourceUtils.getLayoutsInAnnotation(element, PrvBinder.class), rootPackage);

        if (modelInfo == null) {
            return;
        }

        binderModelTypeClass =  ClassName.get(modelInfo.packageName, modelInfo.className);
        generateAnnotatedBinderModel(modelInfo);

        List<? extends TypeMirror> types = ProcessUtils.getClassGenericTypes(element);
        if (types == null || types.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.ERROR, "ItemBinder must has a corresponding model type");
            return;
        }

        binderDataTypeClass = ClassName.bestGuess(types.get(0).toString());

        MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("setDataBindingVariables")
                .addModifiers(PUBLIC)
                .returns(void.class)
                .addAnnotation(Override.class)
                .addParameter(binderDataTypeClass, "model")
                .addParameter(ClassName.bestGuess(NameStore.VIEW_DATA_BINDING), "binding")
                .addStatement("$T $N = prepareBindingModel($N)", binderModelTypeClass, "bindingModel", "model")
                .addCode("\n");

        for (BindingModelInfo info: modelInfo.bindingModelInfo) {
            String field = info.fieldName;
            methodSpecBuilder.addStatement("$N.setVariable($T.$N, $N.$N())", "binding",
                    ClassName.get(rootPackage, NameStore.BR), field, "bindingModel", field);
        }

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(implClassName)
                .addModifiers(PUBLIC, FINAL)
                .addAnnotation(Keep.class)
                .superclass(ClassName.get(element))
                .addField(currentClass, "instance", Modifier.PRIVATE, Modifier.STATIC, Modifier.VOLATILE)
                .addMethod(singletonMethod(currentClass).build())
                .addMethod(MethodSpec.methodBuilder("getViewType")
                        .addModifiers(PUBLIC)
                        .returns(int.class)
                        .addAnnotation(Override.class)
                        .addStatement("return $L", resource)
                        .build())
                .addMethod(MethodSpec.methodBuilder("create")
                        .addModifiers(PUBLIC)
                        .returns(dataBindingViewHolderClass)
                        .addAnnotation(Override.class)
                        .addParameter(ClassName.bestGuess(NameStore.VIEW_GROUP), "parent")
                        .addStatement("return new $T(buildView($N))", dataBindingViewHolderClass, "parent")
                        .build())
                .addMethod(methodSpecBuilder.build());

        createFile(packageName, classBuilder);
    }

    //生成自定义Binder实现类
    private void generateNormalBinder(TypeElement element, int resource) {
        String packageName = ClassName.get(element).packageName();
        String className = ClassName.get(element).simpleName();
        String implClassName = className + NameStore.AUTO_IMPL_SUFFIX;

        ClassName binderModelTypeClass;
        ClassName viewHolderClass;
        ClassName NonNullClass = ClassName.bestGuess(NameStore.NONNULL);
        ClassName currentClass = ClassName.get(packageName, implClassName);
        ClassName binderClass = ClassName.bestGuess(NameStore.BINDER);

        List<? extends TypeMirror> genericTypes = ProcessUtils.getClassGenericTypes(element);
        if (genericTypes == null || genericTypes.size() < 2) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Binder should at least has  " +
                    "2 Generic Types for data and viewholder type");
            return;
        }

        binderModelTypeClass = ClassName.bestGuess(genericTypes.get(0).toString());
        viewHolderClass = ClassName.bestGuess(genericTypes.get(1).toString());

        List<String> unOverridedMethods = ProcessUtils.getUnOverrideMethodNames(elements.getTypeElement(NameStore.BINDER), element);

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(implClassName)
                .addModifiers(PUBLIC, FINAL)
                .addAnnotation(Keep.class)
                .superclass(ClassName.get(element))
                .addField(currentClass, "instance", Modifier.PRIVATE,  Modifier.STATIC, Modifier.VOLATILE)
                .addMethod(singletonMethod(currentClass).build())
                .addMethod(MethodSpec.methodBuilder("getViewType")
                        .addModifiers(PUBLIC)
                        .returns(int.class)
                        .addAnnotation(Override.class)
                        .addStatement("return $L", resource)
                        .build())
                .addMethod(MethodSpec.methodBuilder("create")
                        .addModifiers(PUBLIC)
                        .returns(viewHolderClass)
                        .addAnnotation(Override.class)
                        .addParameter(ClassName.bestGuess(NameStore.VIEW_GROUP), "parent")
                        .addStatement("return new $T($T.from($N.getContext()).inflate($L, $N, $L))",
                                viewHolderClass, ClassName.bestGuess(NameStore.LAYOUTINFLATER), "parent",
                                resource, "parent", false)
                        .build());


        WildcardTypeName binderModelTypeWildcardTypeName = WildcardTypeName.supertypeOf(binderModelTypeClass);
        WildcardTypeName viewHolderWildcardTypeName = WildcardTypeName.subtypeOf(ClassName.bestGuess(NameStore.VIEWHOLDER));

        TypeName binderTypeName = ParameterizedTypeName.get(binderClass, binderModelTypeWildcardTypeName, viewHolderWildcardTypeName);
        TypeName listBinderTypeName = ParameterizedTypeName.get(listClass, binderTypeName);

        //实现基类未实现的接口方法
        for (String method : unOverridedMethods) {
            MethodSpec.Builder builder = null;
            switch (method) {
                case "prepare":
                    builder = MethodSpec.methodBuilder("prepare")
                            .addAnnotation(Override.class)
                            .returns(void.class)
                            .addModifiers(PUBLIC)
                            .addParameter(ParameterSpec.builder(binderModelTypeClass, "model")
                                    .addAnnotation(AnnotationSpec.builder(NonNullClass).build())
                                    .build())
                            .addParameter(ParameterSpec.builder(listBinderTypeName, "binders")
                                    .addAnnotation(AnnotationSpec.builder(NonNullClass).build())
                                    .build())
                            .addParameter(int.class, "binderIndex");
                    break;
                case "bind":
                    builder = MethodSpec.methodBuilder("bind")
                        .addAnnotation(Override.class)
                        .returns(void.class)
                        .addModifiers(PUBLIC)
                        .addParameter(ParameterSpec.builder(binderModelTypeClass, "model")
                                .addAnnotation(AnnotationSpec.builder(NonNullClass).build())
                                .build())
                        .addParameter(ParameterSpec.builder(viewHolderClass, "holder")
                                .addAnnotation(AnnotationSpec.builder(NonNullClass).build())
                                .build())
                        .addParameter(ParameterSpec.builder(listBinderTypeName, "binders")
                                .addAnnotation(AnnotationSpec.builder(NonNullClass).build())
                                .build())
                        .addParameter(int.class, "binderIndex");
                    break;
                case "unbind":
                    builder = MethodSpec.methodBuilder("unbind")
                            .addAnnotation(Override.class)
                            .returns(void.class)
                            .addModifiers(PUBLIC)
                            .addParameter(ParameterSpec.builder(viewHolderClass, "holder")
                                    .addAnnotation(AnnotationSpec.builder(NonNullClass).build())
                                    .build());
                    break;
                default:
                    break;
            }

            if (builder != null) {
                classBuilder.addMethod(builder.build());
            }
        }

        createFile(packageName, classBuilder);
    }

    //根据R.layout文件生成抽象绑定数据类
    private void generateAnnotatedBinderModel(GeneratedModelInfo modelInfo) {
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(NameStore.BASE + modelInfo.className)
                .addModifiers(PUBLIC, ABSTRACT)
                .addAnnotation(Keep.class)
                .addSuperinterface(ClassName.bestGuess(NameStore.BINDING_MODEL));

        for (BindingModelInfo info : modelInfo.bindingModelInfo) {
            classBuilder.addField(FieldSpec.builder(ClassName.get(info.typeMirror), info.fieldName, PUBLIC)
                    .addAnnotation(PrvAttribute.class)
                    .build());
        }

        createFile(modelInfo.packageName, classBuilder);
    }

    //生成具体绑定数据类
    private void generateBinderModel(Map<TypeElement, Set<BindingModelInfo>> infoMap) {
        if (infoMap == null || infoMap.isEmpty()) {
            return;
        }

        for (Map.Entry<TypeElement, Set<BindingModelInfo>> entry : infoMap.entrySet()) {
            TypeElement element = entry.getKey();
            Set<BindingModelInfo> infoSet = entry.getValue();

            ClassName bindingModel = ClassName.get(element);
            String packageName = bindingModel.packageName();
            String modelName = bindingModel.simpleName().replaceFirst(NameStore.BASE, "");

            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(modelName)
                    .addModifiers(PUBLIC, FINAL)
                    .addAnnotation(Keep.class)
                    .superclass(bindingModel);

            for (BindingModelInfo modelInfo : infoSet) {
                TypeName attributeClass = ClassName.get(modelInfo.typeMirror);
                classBuilder.addField(attributeClass, modelInfo.fieldName, PRIVATE);

                MethodSpec setter = MethodSpec.methodBuilder(modelInfo.fieldName)
                        .addModifiers(PUBLIC)
                        .returns(ClassName.get(packageName, modelName))
                        .addParameter(attributeClass, NameStore.VAL)
                        .addStatement("this.$N = $N", modelInfo.fieldName, NameStore.VAL)
                        .addStatement("return this")
                        .build();

                MethodSpec getter = MethodSpec.methodBuilder(modelInfo.fieldName)
                        .addModifiers(PUBLIC)
                        .returns(attributeClass)
                        .addStatement("return $N", modelInfo.fieldName)
                        .build();

                classBuilder.addMethod(setter)
                        .addMethod(getter);
            }

            createFile(packageName, classBuilder);
        }
    }

    //单例方法
    private MethodSpec.Builder singletonMethod(ClassName type) {
        return MethodSpec
                .methodBuilder("getInstance")
                .addModifiers(PUBLIC, Modifier.STATIC)
                .returns(type)
                .beginControlFlow("if ($N == $S)", "instance", null)
                .beginControlFlow("synchronized ($T.class)", type)
                .beginControlFlow("if ($N == $S)", "instance", null)
                .addStatement("$N = new $T()", "instance", type)
                .endControlFlow()
                .endControlFlow()
                .endControlFlow()
                .addCode("\n")
                .addStatement("return $N", "instance");
    }

    //生成.java文件
    private void createFile(String generatedPackageName, TypeSpec.Builder classBuilder) {
        try {
            JavaFile.builder(generatedPackageName,
                    classBuilder.build())
                    .build()
                    .writeTo(filer);
        } catch (IOException e) {
            messager.printMessage(ERROR, e.toString());
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new TreeSet<>(Arrays.asList(
                PrvItemBinder.class.getCanonicalName(),
                PrvAdapter.class.getCanonicalName(),
                PrvBinder.class.getCanonicalName(),
                PrvAttribute.class.getCanonicalName(),
                Keep.class.getCanonicalName()));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
