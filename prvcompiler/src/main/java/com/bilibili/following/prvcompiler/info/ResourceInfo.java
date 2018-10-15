package com.bilibili.following.prvcompiler.info;

import com.squareup.javapoet.ClassName;

public class ResourceInfo {

    public ClassName className;
    public String resourceName;

    public ResourceInfo(ClassName className, String name) {
        this.className = className;
        this.resourceName = name;
    }
}
