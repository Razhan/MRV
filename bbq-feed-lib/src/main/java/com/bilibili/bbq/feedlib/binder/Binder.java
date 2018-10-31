package com.bilibili.bbq.feedlib.binder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.bilibili.bbq.feedlib.listener.ActionListener;
import com.bilibili.bbq.feedlib.viewholder.ViewHolder;

import java.util.List;

public interface Binder<T, VH extends ViewHolder> {

    int getViewType();

    VH create(ViewGroup parent);

    void prepare(@NonNull T model, List<Binder<? super T, ? extends ViewHolder>> binderList, int binderIndex);

    void bind(@NonNull T model, @NonNull VH holder, @NonNull List<Binder<? super T, ? extends ViewHolder>> binderList,
              int binderIndex, @NonNull List<Object> payloads);

    void unbind(@NonNull VH holder);

    void onViewAttachedToWindow(@NonNull ViewHolder holder);

    void onViewDetachedFromWindow(@NonNull ViewHolder holder);

}
