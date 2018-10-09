package com.bilibili.following.prvlibrary;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import java.util.List;

public interface Binder<T, VH extends ViewHolder> {

    int getViewType();

    VH create(ViewGroup parent);

    void prepare(@NonNull T model, List<Binder<? super T, ? extends ViewHolder>> binderList, int binderIndex);

    void bind(@NonNull T model, @NonNull VH holder, @NonNull List<Binder<? super T, ? extends ViewHolder>> binderList,
              int binderIndex);

    void unbind(@NonNull VH holder);

}
