package com.bilibili.bbq.feed.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.bilibili.bbq.feed.R;
import com.bilibili.bbq.feedlib.listener.ActionListener;
import com.bilibili.bbq.feedlib.viewholder.BaseViewHolder;

import butterknife.BindView;
import butterknife.OnClick;

public class TestAViewHolder extends BaseViewHolder {
    @BindView(R.id.button)
    Button button;

    public TestAViewHolder(@NonNull View itemView, @Nullable ActionListener mClickListener) {
        super(itemView, mClickListener);
    }

    @OnClick({R.id.button})
    public void onClick(View view) {
        if (listenerDelegate != null) {
            listenerDelegate.onClick(view);
        }
    }

}
