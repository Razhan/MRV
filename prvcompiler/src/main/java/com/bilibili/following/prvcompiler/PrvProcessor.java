package com.bilibili.following.prvcompiler;

import com.bilibili.following.prvannotations.Keep;
import com.bilibili.following.prvannotations.PrvAdapter;
import com.bilibili.following.prvannotations.PrvBinder;
import com.bilibili.following.prvannotations.PrvItemBinder;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class)
public class PrvProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private Elements elementUtil;

    private ClassName listClass;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        elementUtil = processingEnv.getElementUtils();

        listClass = ClassName.get(List.class);

        ProcessUtils.init(messager, processingEnvironment.getTypeUtils(), elementUtil);
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
            generateItemBinder(ProcessUtils.getItemBinderSet(roundEnvironment));
            generateBinder(ProcessUtils.getBinderSet(roundEnvironment));
            generateBinderModel(ProcessUtils.getBindingModelSet(roundEnvironment));


        } catch (RuntimeException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR, "Unexpected error in PrvProcessor: " + e);
        }

        return true;
    }

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
            implClassName = className + NameStore.AUTO_IMPL;

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
                TypeName binderName = itemBinder.binderList.get(i);
                methodBuilder.addCode("new $T()", ClassName.bestGuess(binderName.toString() + NameStore.AUTO_IMPL));

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

    private void generateBinder(Map<TypeElement, Integer> binderSet) {
        if (binderSet == null || binderSet.isEmpty()) {
            return;
        }

        ClassName dataBindingViewHolderClass = ClassName.bestGuess(NameStore.DATA_BINDING_VIEWHOLDER);

        String packageName;
        String className;
        String implClassName;
        ClassName binderDataTypeClass;
        ClassName binderModelTypeClass;

        for (Map.Entry<TypeElement, Integer> entry : binderSet.entrySet()) {
            packageName = ClassName.get(entry.getKey()).packageName();
            className = ClassName.get(entry.getKey()).simpleName();
            implClassName = className + NameStore.AUTO_IMPL;
            binderModelTypeClass = ClassName.bestGuess(entry.getKey().toString() + NameStore.MODEL);

            List<? extends TypeMirror> types = ProcessUtils.getClassGenericTypes(entry.getKey());
            if (types == null || types.isEmpty()) {
                messager.printMessage(Diagnostic.Kind.ERROR, "ItemBinder must has a corresponding model type");
                return;
            }

            binderDataTypeClass = ClassName.bestGuess(types.get(0).toString());

            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(implClassName)
                    .addModifiers(PUBLIC, FINAL)
                    .addAnnotation(Keep.class)
                    .superclass(ClassName.get(entry.getKey()))
                    .addMethod(MethodSpec.methodBuilder("getViewType")
                            .addModifiers(PUBLIC)
                            .returns(int.class)
                            .addAnnotation(Override.class)
                            .addStatement("return $L", entry.getValue())
                            .build())
                    .addMethod(MethodSpec.methodBuilder("create")
                            .addModifiers(PUBLIC)
                            .returns(dataBindingViewHolderClass)
                            .addAnnotation(Override.class)
                            .addParameter(ClassName.bestGuess(NameStore.VIEW_GROUP), "parent")
                            .addStatement("return new $T(buildView($N))", dataBindingViewHolderClass, "parent")
                            .build())
                    .addMethod(MethodSpec.methodBuilder("setDataBindingVariables")
                            .addModifiers(PUBLIC)
                            .returns(void.class)
                            .addAnnotation(Override.class)
                            .addParameter(binderDataTypeClass, "model")
                            .addParameter(ClassName.bestGuess(NameStore.VIEW_DATA_BINDING), "binding")
                            .addStatement("$T $N = prepareBindingModel($N)", binderModelTypeClass, "bindingModel", "model")
                            .addStatement("$N.setVariable($T.textRes, $N.getTextRes())", "binding",
                                    ClassName.get(ProcessUtils.getRootModuleString(entry.getKey()), NameStore.BR), "bindingModel")
                            .build());

            createFile(packageName, classBuilder);
        }
    }

    private void generateBinderModel(Map<TypeElement, Set<BindingModelInfo>> infoMap) {
        if (infoMap == null || infoMap.isEmpty()) {
            return;
        }

        for (Map.Entry<TypeElement, Set<BindingModelInfo>> entry : infoMap.entrySet()) {
            TypeElement element = entry.getKey();
            Set<BindingModelInfo> infos = entry.getValue();

            ClassName bindingModel = ClassName.get(element);
            String packageName = bindingModel.packageName();
            String modelName = bindingModel.simpleName().concat(NameStore.AUTO_IMPL);

            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(modelName)
                    .addModifiers(PUBLIC, FINAL)
                    .addAnnotation(Keep.class)
                    .superclass(bindingModel);

            for (BindingModelInfo modelInfo : infos) {
                TypeName attributeClass = ClassName.get(modelInfo.typeMirror);
                classBuilder.addField(attributeClass, modelInfo.fieldName, PRIVATE);

                MethodSpec setter = MethodSpec.methodBuilder(modelInfo.fieldName)
                        .addModifiers(PUBLIC)
                        .returns(void.class)
                        .addParameter(attributeClass, NameStore.VAL)
                        .addStatement("this.$N = $N", modelInfo.fieldName, NameStore.VAL)
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
                Keep.class.getCanonicalName()));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
