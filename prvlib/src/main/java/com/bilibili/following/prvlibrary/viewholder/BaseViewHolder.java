package com.bilibili.following.prvlibrary.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.bilibili.following.prvlibrary.listener.ActionListener;
import com.bilibili.following.prvlibrary.listener.ActionListenerDelegate;

import butterknife.ButterKnife;

public class BaseViewHolder extends ViewHolder {

    protected ActionListenerDelegate listenerDelegate;

    @SuppressWarnings("unchecked")
    public BaseViewHolder(@NonNull View itemView, @Nullable ActionListener mClickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);

        listenerDelegate = new ActionListenerDelegate();
        listenerDelegate.setActionListener(mClickListener);
    }

    public ActionListenerDelegate getListenerDelegate() {
        return listenerDelegate;
    }
}
