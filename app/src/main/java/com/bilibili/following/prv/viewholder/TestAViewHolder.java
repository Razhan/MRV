package com.bilibili.following.prv.viewholder;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bilibili.following.prv.R;
import com.bilibili.following.prvlibrary.viewholder.BaseViewHolder;

import butterknife.BindView;

public class TestAViewHolder extends BaseViewHolder {
    @BindView(R.id.button)
    Button button;

    public TestAViewHolder(@NonNull View itemView) {
        super(itemView);
    }

}
