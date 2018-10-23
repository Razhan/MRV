package com.bilibili.bbq.feedcompiler.info;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.TypeElement;

public class AdapterInfo {

    public TypeElement adapter;
    public TypeElement dataType;
    public Set<ItemBinderInfo> itemBinderInfoList;
    public List<TypeElement> binderList;

    public AdapterInfo(TypeElement adapter, TypeElement dataType, Set<ItemBinderInfo> itemBinderInfoSet) {
        this.adapter = adapter;
        this.dataType = dataType;
        this.itemBinderInfoList = itemBinderInfoSet;
        this.binderList = new ArrayList<>();

        for (ItemBinderInfo info : itemBinderInfoSet) {
            binderList.addAll(info.binderList);
        }
    }

}
