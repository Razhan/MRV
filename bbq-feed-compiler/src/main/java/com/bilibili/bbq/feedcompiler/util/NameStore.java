package com.bilibili.bbq.feedcompiler.util;

public class NameStore {
    private NameStore() {
    }

    public static final String BASE = "Base";

    public static final String AUTO_IMPL_SUFFIX = "Impl";

    public static final String BR = "BR";
    public static final String VIEWHOLDER_STRING = "ViewHolder";

    public static final String VAL = "val";
    public static final String BINDING_SUFFIX = "Binding";
    public static final String BINDING_MODEL_SUFFIX = "BindingModel";
    public static final String DATA_BINDING = "databinding";
    public static final String ADAPTER_NAME = "Base%sPrvAdapter";

    //lib
    public static final String LIB = "com.bilibili.bbq.feedlib";
    public static final String VIEWHOLDER = LIB + ".viewholder.ViewHolder";
    public static final String DATA_BINDING_VIEWHOLDER = LIB + ".viewholder.DataBindingViewHolder";
    public static final String BINDER = LIB + ".binder.Binder";
    public static final String ITEM_BINDER = LIB + ".binder.ItemBinder";
    public static final String BINDING_MODEL = LIB + ".binder.BindingModel";
    public static final String DATABINDING_BINDER = LIB + ".binder.DataBindingBinder";
    public static final String ADAPTER = LIB + ".BasePrvAdapter";
    public static final String ACTION_LISTENER = LIB + ".listener.ActionListener";
    public static final String BASE_BINDER = LIB + ".binder.BaseBinder";

    //Android
    public static final String VIEW_GROUP = "android.view.ViewGroup";
    public static final String NONNULL = "android.support.annotation.NonNull";
    public static final String NULLABLE = "android.support.annotation.Nullable";
    public static final String VIEW_DATA_BINDING = "android.databinding.ViewDataBinding";
    public static final String LAYOUT_INFLATER = "android.view.LayoutInflater";
    public static final String VIEW = "android.view.View";

    //ButterKnife
    public static final String ONCLICK = "butterknife.OnClick";

}
