package com.bilibili.following.prvlibrary.binder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.bilibili.following.prvlibrary.listener.ActionListener;
import com.bilibili.following.prvlibrary.viewholder.ViewHolder;

import java.util.List;

public interface Binder<T, VH extends ViewHolder> {

    int getViewType();

    @Nullable
    ActionListener<T, VH> getListener();

    VH create(ViewGroup parent);

    void prepare(@NonNull T model, List<Binder<? super T, ? extends ViewHolder>> binderList, int binderIndex);

    void bind(@NonNull T model, @NonNull VH holder, @NonNull List<Binder<? super T, ? extends ViewHolder>> binderList,
              int binderIndex);

    void unbind(@NonNull VH holder);

}
