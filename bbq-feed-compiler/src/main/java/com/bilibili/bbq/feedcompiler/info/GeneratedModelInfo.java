package com.bilibili.bbq.feedcompiler.info;

import com.squareup.javapoet.ClassName;

import java.util.List;

public class GeneratedModelInfo {
    public String packageName;
    public String className;
    public ClassName dataBindingClassName;
    public List<BindingModelInfo> bindingModelInfo;

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setBindingModelInfo(List<BindingModelInfo> bindingModelInfo) {
        this.bindingModelInfo = bindingModelInfo;
    }

    public void setDataBindingClassName(ClassName dataBindingClassName) {
        this.dataBindingClassName = dataBindingClassName;
    }
}
