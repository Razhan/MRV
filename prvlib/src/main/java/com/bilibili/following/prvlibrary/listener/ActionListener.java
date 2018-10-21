package com.bilibili.following.prvlibrary.listener;

import android.view.View;

import com.bilibili.following.prvlibrary.binder.Binder;
import com.bilibili.following.prvlibrary.viewholder.ViewHolder;

import java.util.List;

public abstract class ActionListener<T, VH extends ViewHolder> implements View.OnClickListener {

    private T model;
    private VH holder;
    private List<Binder<? super T, ? extends ViewHolder>> binders;
    private int binderIndex;

    public void update(T model, VH holder, List<Binder<? super T, ? extends ViewHolder>> binders, int binderIndex) {
        this.model = model;
        this.holder = holder;
        this.binders = binders;
        this.binderIndex = binderIndex;
    }

    protected abstract void act(View v, T model, VH holder, List<Binder<? super T, ? extends ViewHolder>> binders, int binderIndex);

    @Override
    public void onClick(View v) {
        act(v, model, holder, binders, binderIndex);
    }

}
