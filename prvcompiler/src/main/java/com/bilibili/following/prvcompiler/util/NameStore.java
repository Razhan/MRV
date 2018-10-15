package com.bilibili.following.prvcompiler.util;

public class NameStore {
    private NameStore() {
    }

    public static final String BASE = "Base";

    public static final String AUTO_IMPL_SUFFIX = "$Impl";
    public static final String IMPL_SUFFIX = "Impl";

    public static final String MODEL = "Model";
    public static final String BR = "BR";

    public static final String VAL = "val";
    public static final String BINDING_SUFFIX = "Binding";
    public static final String BINDING_MODEL_SUFFIX = "BindingModel";
    public static final String DATA_BINDING = "databinding";


    //lib
    public static final String LIB = "com.bilibili.following.prvlibrary";
    public static final String VIEWHOLDER = LIB + ".viewholder.ViewHolder";
    public static final String DATA_BINDING_VIEWHOLDER = LIB + ".viewholder.DataBindingViewHolder";
    public static final String BINDER = LIB + ".binder.Binder";
    public static final String ITEM_BINDER = LIB + ".binder.ItemBinder";
    public static final String BINGDING_MODEL = LIB + ".binder.BindingModel";


    //Android
    public static final String VIEW_GROUP = "android.view.ViewGroup";
    public static final String NONNULL = "android.support.annotation.NonNull";
    public static final String VIEW_DATA_BINDING = "android.databinding.ViewDataBinding";

}
