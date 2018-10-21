package com.bilibili.following.prvlibrary.viewholder;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.bilibili.following.prvlibrary.listener.ActionListener;

public class DataBindingViewHolder extends BaseViewHolder {

    public DataBindingViewHolder(@NonNull View itemView, @Nullable ActionListener mClickListener) {
        super(itemView, mClickListener);
    }

    public ViewDataBinding getBinding() {
        return (ViewDataBinding) itemView.getTag();
    }

}
