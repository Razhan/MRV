package com.bilibili.following.prvlibrary.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.bilibili.following.prvlibrary.listener.ActionListener;

import butterknife.ButterKnife;

public class BaseViewHolder extends ViewHolder {

    @Nullable
    protected ActionListener mClickListener;

    public BaseViewHolder(@NonNull View itemView, @Nullable ActionListener mClickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        this.mClickListener = mClickListener;
    }

    public void setClickListener(ActionListener listener) {
        this.mClickListener = listener;
    }
}
