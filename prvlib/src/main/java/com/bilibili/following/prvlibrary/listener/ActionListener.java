package com.bilibili.following.prvlibrary.listener;

import android.view.View;

import com.bilibili.following.prvlibrary.binder.Binder;
import com.bilibili.following.prvlibrary.viewholder.ViewHolder;

import java.util.List;

public interface ActionListener<T, VH extends ViewHolder> {

    void act(View v, T model, VH holder, List<Binder<? super T, ? extends ViewHolder>> binders, int binderIndex);

}
