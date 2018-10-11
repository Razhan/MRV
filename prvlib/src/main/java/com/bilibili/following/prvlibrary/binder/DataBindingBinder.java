package com.bilibili.following.prvlibrary.binder;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bilibili.following.prvlibrary.viewholder.DataBindingViewHolder;
import com.bilibili.following.prvlibrary.viewholder.ViewHolder;

import java.util.List;

public abstract class DataBindingBinder<T, VH extends DataBindingViewHolder, BM extends BindingModel> implements Binder<T, VH> {

    protected View buildView(@NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding = DataBindingUtil.inflate(layoutInflater, getViewType(), parent, false);
        View view = binding.getRoot();
        view.setTag(binding);
        return view;
    }

    @Override
    public void bind(@NonNull T model, @NonNull VH holder, @NonNull List<Binder<? super T, ? extends ViewHolder>> binders, int binderIndex) {
        ViewDataBinding dataBinding = holder.getBinding();

        if (dataBinding != null) {
            setDataBindingVariables(model, dataBinding);
            dataBinding.executePendingBindings();
        }
    }

    protected abstract BM prepareBindingModel(T model);

    protected abstract void setDataBindingVariables(T model, ViewDataBinding binding);

}