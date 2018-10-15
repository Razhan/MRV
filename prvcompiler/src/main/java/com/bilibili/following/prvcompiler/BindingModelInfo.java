package com.bilibili.following.prvcompiler;

import javax.lang.model.type.TypeMirror;

public class BindingModelInfo {

    public String fieldName;
    public TypeMirror typeMirror;
    public String modelName;
    public String packageName;

    public BindingModelInfo(String fieldName, TypeMirror typeMirror, String modelName, String packageName) {
        this.fieldName = fieldName;
        this.typeMirror = typeMirror;
        this.modelName = modelName;
        this.packageName = packageName;
    }
}
