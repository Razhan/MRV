package com.bilibili.following.prvlibrary.binder;

import android.support.annotation.Nullable;

import com.bilibili.following.prvlibrary.viewholder.ViewHolder;

import java.util.List;

public class BinderResult<T> {

    @Nullable
    public final T item;

    private final int itemPosition;

    @Nullable
    public final List<Binder<? super T, ? extends ViewHolder>> binderList;

    public final int binderIndex;

    public BinderResult(@Nullable final T item,
                 final int itemPosition,
                 @Nullable final List<Binder<? super T, ? extends ViewHolder>> binderList,
                 final int binderIndex) {
        this.item = item;
        this.itemPosition = itemPosition;
        this.binderList = binderList;
        this.binderIndex = binderIndex;
    }

    @Nullable
    public Binder<? super T, ? extends ViewHolder> getBinder() {
        return binderList != null && binderIndex >= 0 && binderIndex < binderList.size()
                ? binderList.get(binderIndex) : null;
    }
}
