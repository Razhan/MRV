package com.bilibili.following.prvlibrary;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.List;

public interface ActionListener<U, V extends ViewHolder, W extends V> {

    void act(@NonNull U model, @NonNull W holder, @NonNull View v,
             @NonNull List<Binder<? super U, ? extends V>> binderList,
             int binderIndex, @Nullable Object obj);
}
