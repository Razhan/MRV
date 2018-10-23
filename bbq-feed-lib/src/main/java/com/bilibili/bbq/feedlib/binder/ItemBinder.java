package com.bilibili.bbq.feedlib.binder;

import android.support.annotation.NonNull;

import com.bilibili.bbq.feedlib.viewholder.ViewHolder;

import java.util.List;

public interface ItemBinder<T, B extends Binder<T, ? extends ViewHolder>> {

    @NonNull
    List<? extends B> getBinderList(@NonNull T model, int position);
}
