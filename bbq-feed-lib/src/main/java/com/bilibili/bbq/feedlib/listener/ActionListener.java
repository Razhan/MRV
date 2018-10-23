package com.bilibili.bbq.feedlib.listener;

import android.view.View;

import com.bilibili.bbq.feedlib.binder.Binder;
import com.bilibili.bbq.feedlib.viewholder.ViewHolder;

import java.util.List;

public interface ActionListener<T, VH extends ViewHolder> {

    void act(View v, T model, VH holder, List<Binder<? super T, ? extends ViewHolder>> binders, int binderIndex);

}
