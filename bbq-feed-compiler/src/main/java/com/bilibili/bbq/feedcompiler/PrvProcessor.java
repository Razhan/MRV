package com.bilibili.bbq.feedcompiler;

import com.bilibili.bbq.feedannotations.Keep;
import com.bilibili.bbq.feedannotations.PrvAdapter;
import com.bilibili.bbq.feedannotations.PrvAttribute;
import com.bilibili.bbq.feedannotations.PrvBinder;
import com.bilibili.bbq.feedannotations.PrvItemBinder;
import com.bilibili.bbq.feedannotations.PrvOnClick;
import com.bilibili.bbq.feedcompiler.info.AdapterInfo;
import com.bilibili.bbq.feedcompiler.info.BindingModelInfo;
import com.bilibili.bbq.feedcompiler.info.GeneratedModelInfo;
import com.bilibili.bbq.feedcompiler.info.ItemBinderInfo;
import com.bilibili.bbq.feedcompiler.util.NameStore;
import com.bilibili.bbq.feedcompiler.util.ProcessUtils;
import com.bilibili.bbq.feedcompiler.util.ResourceUtils;
import com.bilibili.bbq.feedcompiler.util.StringUtils;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import static javax.lang.model.element.Modifier.STATIC;
import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class)
public class PrvProcessor extends AbstractProcessor {

    private static final int FIRST_ROUND = 1;
    private static final int SECOND_ROUND = 2;
    private static final int THIRD_ROUND = 3;

    private Filer filer;
    private Messager messager;
    private Elements elements;
    private Types types;

    private ClassName listClass;

    private Set<ItemBinderInfo> itemBinderInfoSet;
    private Map<TypeElement, Integer> binderMap;
    private Map<TypeElement, TypeElement> adapterList;
    private Map<TypeElement, GeneratedModelInfo> binderInfoMap;

    private int roundCount = FIRST_ROUND;

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

    /**
     * 需要处理3轮
     * 第一轮： PrvItemBinder.class, PrvAdapter.class, PrvBinder.class
     * 第二轮： 解析dataBinding类，并生成抽象数据类
     * 第三轮： PrvAttribute.class, PrvOnClick.class, Keep.class
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (roundEnvironment.processingOver()) {
            if (!set.isEmpty()) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "Unexpected processing state: annotations still available after processing over");
                return false;
            }
        }

        try {
            switch (roundCount) {
                case FIRST_ROUND:
                    itemBinderInfoSet = ProcessUtils.getItemBinderSet(roundEnvironment);
                    binderMap = ProcessUtils.getBinderMap(roundEnvironment);
                    binderInfoMap = ProcessUtils.getBinderInfoMap(binderMap);
                    adapterList = ProcessUtils.getAdapterList(roundEnvironment);

                    roundCount++;
                    return true;
                case SECOND_ROUND:
                    if (!ProcessUtils.waitingForDataBinding(binderInfoMap.values())) {
                        ProcessUtils.attributeGeneratedModel(binderInfoMap.values());
                    }

                    generateItemBinder(itemBinderInfoSet);
                    generateBinder(binderMap);
                    generateAdapter(adapterList, itemBinderInfoSet);

                    roundCount++;
                    return false;
                case THIRD_ROUND:
                    generateBinderModel(ProcessUtils.getBindingModelSet(roundEnvironment));
                    return true;
                default:
                    return false;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR, "Unexpected error in PrvProcessor: " + e);
        }

        return false;
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

            for (TypeElement binder : adapterInfo.binderList) {
                ClassName implBinder = ClassName.bestGuess(binder.toString().concat(NameStore.AUTO_IMPL_SUFFIX));
                builder.addStatement("registerBinder($L, $T.getInstance().setAdapter(this))", binder.getAnnotation(PrvBinder.class).value(), implBinder);
            }

            builder.addCode("\n");

            for (ItemBinderInfo itemBinderInfo : adapterInfo.itemBinderInfoList) {
                ClassName implItemBinder = ClassName.bestGuess(itemBinderInfo.itemBinder.toString().concat(NameStore.AUTO_IMPL_SUFFIX));
                builder.addStatement("registerItemBinder($T.class, new $T())", itemBinderInfo.dataType, implItemBinder);
            }

            //生成的抽象类命为BaseXXXXPrvAdapter, XXXX为数据基类类型或接口名
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

        ClassName nonNullClass = ClassName.get("android.support.annotation", "NonNull");
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
                    .addAnnotation(AnnotationSpec.builder(nonNullClass).build())
                    .addAnnotation(Override.class)
                    .addParameter( ParameterSpec.builder(itemBinderDataTypeClass, "model")
                            .addAnnotation(AnnotationSpec.builder(nonNullClass).build())
                            .build())
                    .addParameter(int.class, "position");

            if (itemBinder.implementedMethodList.contains("getBinderList")) {
                methodBuilder.addStatement("super.getBinderList($N, $N)", "model", "position");
            }

            methodBuilder.addCode("return new $T($T.asList(", ArrayList.class, Arrays.class);

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
        ClassName viewHolderClass;

        packageName = ClassName.get(element).packageName();
        className = ClassName.get(element).simpleName();
        implClassName = className + NameStore.AUTO_IMPL_SUFFIX;
        String rootPackage = ProcessUtils.getRootModuleString(element);
        ClassName currentClass = ClassName.get(packageName, implClassName);
        ClassName nonNullClass = ClassName.bestGuess(NameStore.NONNULL);

        GeneratedModelInfo modelInfo = binderInfoMap.get(element);

        if (modelInfo == null) {
            return;
        }

        binderModelTypeClass =  ClassName.get(modelInfo.packageName, modelInfo.className);
        viewHolderClass = ClassName.bestGuess(modelInfo.className.substring(0,
                modelInfo.className.lastIndexOf(NameStore.BINDING_MODEL_SUFFIX))
                .concat(NameStore.VIEWHOLDER_STRING));

        generateAnnotatedBinderModel(element, modelInfo);

        List<? extends TypeMirror> types = ProcessUtils.getClassGenericTypes(element);
        if (types == null || types.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.ERROR, "ItemBinder must has a corresponding model type");
            return;
        }

        binderDataTypeClass = ClassName.bestGuess(types.get(0).toString());

        MethodSpec.Builder setAllVariablesBuilder = MethodSpec.methodBuilder("setAllDataBindingVariables")
                .addModifiers(PUBLIC)
                .returns(void.class)
                .addAnnotation(Override.class)
                .addParameter(binderDataTypeClass, "model")
                .addParameter(ClassName.bestGuess(NameStore.VIEW_DATA_BINDING), "binding")
                .addStatement("$T $N = prepareBindingModel($N.getRoot().getContext(), $N)", binderModelTypeClass, "bindingModel", "binding", "model")
                .addCode("\n");

        for (BindingModelInfo info : modelInfo.bindingModelInfo) {
            String field = info.fieldName;
            setAllVariablesBuilder.addStatement("$N.setVariable($T.$N, $N.$N())", "binding",
                    ClassName.get(rootPackage, NameStore.BR), field, "bindingModel", field);
        }

        TypeName listTypeName = ParameterizedTypeName.get(listClass, ClassName.get(Object.class));

        MethodSpec.Builder setUpdatedVariablesBuilder = MethodSpec.methodBuilder("setUpdatedDataBindingVariables")
                .addModifiers(PUBLIC)
                .returns(void.class)
                .addAnnotation(Override.class)
                .addParameter(binderDataTypeClass, "model")
                .addParameter(ClassName.bestGuess(NameStore.VIEW_DATA_BINDING), "binding")
                .addParameter(ParameterSpec.builder(listTypeName, "payloads")
                        .addAnnotation(AnnotationSpec.builder(nonNullClass).build())
                        .build())
                .addStatement("$T $N = prepareBindingModel($N.getRoot().getContext(), $N)", binderModelTypeClass, "bindingModel", "binding", "model")
                .addCode("\n")
                .beginControlFlow("for ($T $N : $N)", Object.class, "payload", "payloads")
                .beginControlFlow("if ($N instanceof $T)", "payload", String.class)
                .beginControlFlow("switch (($T) $N)", String.class, "payload");

        for (BindingModelInfo info : modelInfo.bindingModelInfo) {
            String field = info.fieldName;

            setUpdatedVariablesBuilder.addCode("case $T.$N:", binderModelTypeClass,
                    StringUtils.toSnakeCase(info.fieldName).toUpperCase().toUpperCase())
                    .addCode("\n")
                    .addStatement("$>$N.setVariable($T.$N, $N.$N())", "binding",
                            ClassName.get(rootPackage, NameStore.BR), field, "bindingModel", field)
                    .addStatement("break")
            .addCode("$<");
        }

        setUpdatedVariablesBuilder.endControlFlow()
            .endControlFlow()
            .endControlFlow();


        TypeName listenerTypeName = ParameterizedTypeName.get(ClassName.bestGuess(NameStore.ACTION_LISTENER), binderDataTypeClass, dataBindingViewHolderClass);
        MethodSpec.Builder createBuilder = getDataBindingBinderCreateMethod(viewHolderClass, listenerTypeName);

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
                .addMethod(createBuilder.build())
                .addMethod(setAllVariablesBuilder.build())
                .addMethod(setUpdatedVariablesBuilder.build());

        createFile(packageName, classBuilder);
    }

    //生成DataBindingViewHolder
    private void generateDataBindingViewHolder(GeneratedModelInfo modelInfo, List<Integer> ids) {
        String viewHolderName = modelInfo.className.substring(0,
                modelInfo.className.lastIndexOf(NameStore.BINDING_MODEL_SUFFIX))
                .concat(NameStore.VIEWHOLDER_STRING);

        ClassName nonNullClass = ClassName.bestGuess(NameStore.NONNULL);
        ClassName nullAbleClass = ClassName.bestGuess(NameStore.NULLABLE);

        //有绑定listener，生成OnClick方法
        MethodSpec.Builder onClickBuilder = null;
        if (ids != null && ids.size() > 0) {
            onClickBuilder = MethodSpec.methodBuilder("OnClick")
                    .addModifiers(PUBLIC)
                    .addParameter(ClassName.bestGuess(NameStore.VIEW), "view")
                    .returns(void.class)
                    .addAnnotation(AnnotationSpec.builder(ClassName.bestGuess(NameStore.ONCLICK))
                            .addMember("value", "{$L}", StringUtils.getIdListString(ids)).build())
                    .beginControlFlow("if ($N != null)", "listenerDelegate")
                    .addStatement("listenerDelegate.onClick($N)", "view")
                    .endControlFlow();
        }

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(viewHolderName)
                .addModifiers(PUBLIC, FINAL)
                .addAnnotation(Keep.class)
                .superclass(ClassName.bestGuess(NameStore.DATA_BINDING_VIEWHOLDER))
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(ParameterSpec.builder(ClassName.bestGuess(NameStore.VIEW), "itemView")
                                .addAnnotation(AnnotationSpec.builder(nonNullClass).build())
                                .build())
                        .addParameter(ParameterSpec.builder(ClassName.bestGuess(NameStore.ACTION_LISTENER), "mClickListener")
                                .addAnnotation(AnnotationSpec.builder(nullAbleClass).build())
                                .build())
                        .addStatement("super($N, $N)", "itemView", "mClickListener")
                        .build());

        if (onClickBuilder != null) {
            classBuilder.addMethod(onClickBuilder.build());
        }

        createFile(modelInfo.packageName, classBuilder);
    }

    //生成自定义Binder实现类
    private void generateNormalBinder(TypeElement element, int resource) {
        String packageName = ClassName.get(element).packageName();
        String className = ClassName.get(element).simpleName();
        String implClassName = className + NameStore.AUTO_IMPL_SUFFIX;

        ClassName binderModelTypeClass;
        ClassName viewHolderClass;
        ClassName nonNullClass = ClassName.bestGuess(NameStore.NONNULL);
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
        TypeName listenerTypeName = ParameterizedTypeName.get(ClassName.bestGuess(NameStore.ACTION_LISTENER), binderModelTypeClass, viewHolderClass);

        List<String> unOverrideMethods = ProcessUtils.getUnOverrideMethodNames(elements.getTypeElement(NameStore.BINDER), element);

        MethodSpec.Builder createBuilder = getBinderCreateMethod(resource, viewHolderClass, listenerTypeName);

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
                .addMethod(createBuilder.build());

        WildcardTypeName binderModelTypeWildcardTypeName = WildcardTypeName.supertypeOf(binderModelTypeClass);
        WildcardTypeName viewHolderWildcardTypeName = WildcardTypeName.subtypeOf(ClassName.bestGuess(NameStore.VIEWHOLDER));

        TypeName binderTypeName = ParameterizedTypeName.get(binderClass, binderModelTypeWildcardTypeName, viewHolderWildcardTypeName);
        TypeName listBinderTypeName = ParameterizedTypeName.get(listClass, binderTypeName);

        //实现基类未实现的接口方法
        for (String method : unOverrideMethods) {
            MethodSpec.Builder builder = null;
            switch (method) {
                case "prepare":
                    builder = MethodSpec.methodBuilder("prepare")
                            .addAnnotation(Override.class)
                            .returns(void.class)
                            .addModifiers(PUBLIC)
                            .addParameter(ParameterSpec.builder(binderModelTypeClass, "model")
                                    .addAnnotation(AnnotationSpec.builder(nonNullClass).build())
                                    .build())
                            .addParameter(ParameterSpec.builder(listBinderTypeName, "binders")
                                    .addAnnotation(AnnotationSpec.builder(nonNullClass).build())
                                    .build())
                            .addParameter(int.class, "binderIndex");
                    break;
                case "bind":
                    TypeName listTypeName = ParameterizedTypeName.get(listClass, ClassName.get(Object.class));

                    builder = MethodSpec.methodBuilder("bind")
                            .addAnnotation(Override.class)
                            .returns(void.class)
                            .addModifiers(PUBLIC)
                            .addParameter(ParameterSpec.builder(binderModelTypeClass, "model")
                                    .addAnnotation(AnnotationSpec.builder(nonNullClass).build())
                                    .build())
                            .addParameter(ParameterSpec.builder(viewHolderClass, "holder")
                                    .addAnnotation(AnnotationSpec.builder(nonNullClass).build())
                                    .build())
                            .addParameter(ParameterSpec.builder(listBinderTypeName, "binders")
                                    .addAnnotation(AnnotationSpec.builder(nonNullClass).build())
                                    .build())
                            .addParameter(int.class, "binderIndex")
                            .addParameter(ParameterSpec.builder(listTypeName, "payloads")
                                    .addAnnotation(AnnotationSpec.builder(nonNullClass).build())
                                    .build())
                            .addStatement("super.bind($N, $N, $N, $N, $N)", "model", "holder", "binders", "binderIndex", "payloads");
                    break;
                case "unbind":
                    builder = MethodSpec.methodBuilder("unbind")
                            .addAnnotation(Override.class)
                            .returns(void.class)
                            .addModifiers(PUBLIC)
                            .addParameter(ParameterSpec.builder(viewHolderClass, "holder")
                                    .addAnnotation(AnnotationSpec.builder(nonNullClass).build())
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

    //生成自定义Binder的create方法
    private MethodSpec.Builder getBinderCreateMethod(int resource, ClassName viewHolderClass, TypeName listenerTypeName) {
        MethodSpec.Builder createBuilder = MethodSpec.methodBuilder("create")
                .addModifiers(PUBLIC)
                .returns(viewHolderClass)
                .addAnnotation(Override.class)
                .addParameter(ClassName.bestGuess(NameStore.VIEW_GROUP), "parent");

        createBuilder.addParameter(listenerTypeName, "listener")
                .addStatement("return new $T($T.from($N.getContext()).inflate($L, $N, $L), $N)",
                        viewHolderClass, ClassName.bestGuess(NameStore.LAYOUT_INFLATER), "parent",
                        resource, "parent", false, "listener");

        return createBuilder;
    }

    //生成DataBindingBinder的create方法
    private MethodSpec.Builder getDataBindingBinderCreateMethod(ClassName viewHolderClass, TypeName listenerTypeName) {
        MethodSpec.Builder createBuilder = MethodSpec.methodBuilder("create")
                .addModifiers(PUBLIC)
                .returns(viewHolderClass)
                .addAnnotation(Override.class)
                .addParameter(ClassName.bestGuess(NameStore.VIEW_GROUP), "parent");

        createBuilder.addParameter(listenerTypeName, "listener")
                .addStatement("return new $T($N($N), $N)", viewHolderClass, "buildView", "parent", "listener");

        return createBuilder;
    }

    //根据R.layout文件生成抽象绑定数据类
    private void generateAnnotatedBinderModel(TypeElement element, GeneratedModelInfo modelInfo) {
        if (modelInfo == null || modelInfo.bindingModelInfo == null || modelInfo.bindingModelInfo.isEmpty()) {
            return;
        }

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(NameStore.BASE + modelInfo.className)
                .addModifiers(PUBLIC, ABSTRACT)
                .addAnnotation(Keep.class)
                .addSuperinterface(ClassName.bestGuess(NameStore.BINDING_MODEL));

        for (BindingModelInfo info : modelInfo.bindingModelInfo) {
            if (info != null) {
                classBuilder.addField(FieldSpec.builder(ClassName.get(info.typeMirror), info.fieldName, PUBLIC)
                        .addAnnotation(PrvAttribute.class)
                        .build());
            }
        }

        generateDataBindingViewHolder(modelInfo, ProcessUtils.getOnClickIds(element));

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
                //静态字段
                FieldSpec staticField = FieldSpec.builder(String.class, StringUtils.toSnakeCase(modelInfo.fieldName).toUpperCase())
                        .addModifiers(PUBLIC, STATIC, FINAL)
                        .initializer("$S", modelName.concat(".").concat(modelInfo.fieldName))
                        .build();

                classBuilder.addField(staticField);
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
        return new HashSet<>(Arrays.asList(
                PrvItemBinder.class.getCanonicalName(),
                PrvAdapter.class.getCanonicalName(),
                PrvBinder.class.getCanonicalName(),
                PrvAttribute.class.getCanonicalName(),
                PrvOnClick.class.getCanonicalName(),
                Keep.class.getCanonicalName()));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
