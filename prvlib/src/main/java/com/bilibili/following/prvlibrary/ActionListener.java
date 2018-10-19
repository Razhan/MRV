package com.bilibili.following.prvlibrary;

import android.view.View;

import com.bilibili.following.prvlibrary.binder.Binder;
import com.bilibili.following.prvlibrary.viewholder.ViewHolder;

import java.util.List;

public abstract class ActionListener<T, VH extends ViewHolder> implements View.OnClickListener {

    public T model;
    public VH holder;
    public List<Binder<? super T, ? extends VH>> binders;
    public int binderIndex;

    public void update(T model, VH holder, List<Binder<? super T, ? extends VH>> binders, int binderIndex) {
        this.model = model;
        this.holder = holder;
        this.binders = binders;
        this.binderIndex = binderIndex;
    }

    public abstract void act(View v, T model, VH holder, List<Binder<? super T, ? extends VH>> binders, int binderIndex);

    @Override
    public void onClick(View v) {
        act(v, model, holder, binders, binderIndex);
    }

}
