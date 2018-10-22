package com.bilibili.following.prvlibrary.listener;

import android.view.View;

import com.bilibili.following.prvlibrary.binder.Binder;
import com.bilibili.following.prvlibrary.viewholder.ViewHolder;

import java.util.List;

public class ActionListenerDelegate<T, VH extends ViewHolder> implements View.OnClickListener {

    private T model;
    private VH holder;
    private List<Binder<? super T, ? extends ViewHolder>> binders;
    private int binderIndex;
    private ActionListener<T, VH> actionListener;

    public void update(T model, VH holder, List<Binder<? super T, ? extends ViewHolder>> binders, int binderIndex) {
        this.model = model;
        this.holder = holder;
        this.binders = binders;
        this.binderIndex = binderIndex;
    }

    @Override
    public void onClick(View v) {
        if (actionListener != null) {
            actionListener.act(v, model, holder, binders, binderIndex);
        }
    }

    public void setActionListener(ActionListener<T, VH> actionListener) {
        this.actionListener = actionListener;
    }
}