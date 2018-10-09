package com.bilibili.following.prv.viewholder;

import android.view.View;
import android.widget.TextView;

import com.bilibili.following.prv.R;
import com.bilibili.following.prvlibrary.ViewHolder;

public class TextPrimitiveViewHolder extends ViewHolder {
    private final TextView textView;

    public TextPrimitiveViewHolder(final View itemView) {
        super(itemView);
        textView = (TextView) itemView.findViewById(R.id.text);
    }

    public TextView getTextView() {
        return textView;
    }
}