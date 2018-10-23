package com.bilibili.following.prvlibrary.binder;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.bilibili.following.prvlibrary.listener.ActionListener;
import com.bilibili.following.prvlibrary.viewholder.BaseViewHolder;
import com.bilibili.following.prvlibrary.viewholder.ViewHolder;

import java.util.List;

public abstract class BaseBinder<T, VH extends BaseViewHolder> implements Binder<T, VH> {

    @Nullable
    private ActionListener<T, VH> mListener;

    public BaseBinder() {
        mListener = getListener();
    }

    protected abstract VH create(ViewGroup parent, ActionListener<T, VH> listener);

    @Override
    public VH create(ViewGroup parent) {
        return create(parent, mListener);
    }

    @Override
    @CallSuper
    public void bind(@NonNull T model, @NonNull VH holder, @NonNull List<Binder<? super T, ? extends ViewHolder>> binders, int binderIndex, @NonNull List<Object> payloads) {
        updateListener(model, holder, binders, binderIndex);
    }

    @SuppressWarnings("unchecked")
    private void updateListener(T model, VH holder, List<Binder<? super T, ? extends ViewHolder>> binders, int binderIndex) {
        holder.getListenerDelegate().update(model, holder, binders, binderIndex);
    }

}
