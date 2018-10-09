package com.bilibili.following.prvlibrary;

import android.support.annotation.NonNull;

import java.util.List;

public interface ItemBinder<T, B extends Binder<T, ? extends ViewHolder>> {

    @NonNull
    List<? extends B> getBinderList(@NonNull T model, int position);
}
