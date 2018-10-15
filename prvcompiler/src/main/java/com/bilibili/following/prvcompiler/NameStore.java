package com.bilibili.following.prvcompiler;

public class NameStore {
    private NameStore() {
    }

    static final String AUTO_IMPL = "$Impl";
    static final String IMPL = "Impl";

    static final String MODEL = "Model";
    static final String BR = "BR";

    static final String VAL = "val";

    //lib
    static final String LIB = "com.bilibili.following.prvlibrary";
    static final String VIEWHOLDER = LIB + ".viewholder.ViewHolder";
    static final String DATA_BINDING_VIEWHOLDER = LIB + ".viewholder.DataBindingViewHolder";
    static final String BINDER = LIB + ".binder.Binder";
    static final String ITEM_BINDER = LIB + ".binder.ItemBinder";

    //Android
    static final String VIEW_GROUP = "android.view.ViewGroup";
    static final String NONNULL = "android.support.annotation.NonNull";
    static final String VIEW_DATA_BINDING = "android.databinding.ViewDataBinding";

}
