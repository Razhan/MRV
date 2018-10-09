package com.bilibili.following.prvlibrary;

import android.support.annotation.Nullable;

import java.util.List;

public class BinderResult<T> {

    @Nullable
    public final T item;

    private final int itemPosition;

    @Nullable
    final List<Binder<? super T, ? extends ViewHolder>> binderList;

    final int binderIndex;

    BinderResult(@Nullable final T item,
                 final int itemPosition,
                 @Nullable final List<Binder<? super T, ? extends ViewHolder>> binderList,
                 final int binderIndex) {
        this.item = item;
        this.itemPosition = itemPosition;
        this.binderList = binderList;
        this.binderIndex = binderIndex;
    }

    @Nullable
    Binder<? super T, ? extends ViewHolder> getBinder() {
        return binderList != null && binderIndex >= 0 && binderIndex < binderList.size()
                ? binderList.get(binderIndex) : null;
    }
}
