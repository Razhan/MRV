package com.bilibili.following.prvlibrary.viewholder;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.view.View;

public class DataBindingViewHolder extends ViewHolder {

    public DataBindingViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public ViewDataBinding getBinding() {
        return (ViewDataBinding) itemView.getTag();
    }

}
