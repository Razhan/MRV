package com.bilibili.following.prvcompiler;

import com.squareup.javapoet.TypeName;

import java.util.List;

import javax.lang.model.element.TypeElement;

public class ItemBinderInfo {
    //当前类
    public TypeElement itemBinder;
    //对应Binder类型
    public List<TypeName> binderList;
    //对应绑定数据类型
    public TypeName dataType;
    //是否继承自父类
    public boolean hasImplemented;
    //父类已实现ItemBinder接口中的方法名
    public List<String> implementedMethodList;

    public ItemBinderInfo(TypeElement itemBinder, TypeName dataType, List<TypeName> binderList,
                          boolean hasParentClass, List<String> implementedMethodList) {
        this.itemBinder = itemBinder;
        this.binderList = binderList;
        this.dataType = dataType;
        this.hasImplemented = hasParentClass;
        this.implementedMethodList = implementedMethodList;
    }

}
