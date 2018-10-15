package com.bilibili.following.prvcompiler.info;

import javax.lang.model.type.TypeMirror;

public class BindingModelInfo {

    public String fieldName;
    public TypeMirror typeMirror;

    public BindingModelInfo(String fieldName, TypeMirror typeMirror) {
        this.fieldName = fieldName;
        this.typeMirror = typeMirror;
    }
}
