package com.bilibili.following.prvlibrary.binder;

import android.support.annotation.NonNull;

import com.bilibili.following.prvlibrary.viewholder.ViewHolder;

import java.util.List;

public interface ItemBinder<T, B extends Binder<T, ? extends ViewHolder>> {

    @NonNull
    List<? extends B> getBinderList(@NonNull T model, int position);
}
