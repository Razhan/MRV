package com.bilibili.following.prvlibrary.viewholder;

import android.support.annotation.NonNull;
import android.view.View;

import butterknife.ButterKnife;

public class BaseViewHolder extends ViewHolder {

    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
