package com.bilibili.bbq.feedlib.listener;

import android.support.v7.widget.LinearLayoutManager;

import com.bilibili.bbq.feedlib.BasePrvAdapter;

public abstract class PrvEndlessScrollListener extends EndlessScrollListener {

    private BasePrvAdapter adapter;

    public PrvEndlessScrollListener(BasePrvAdapter adapter, LinearLayoutManager layoutManager) {
        super(layoutManager);
        this.adapter = adapter;
    }

    @Override
    protected int getTotalCount() {
        return adapter.getItems().size();
    }

    @Override
    protected int getLastVisibleItemPosition() {
        int lastVisibleViewHolderPosition = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();

        return adapter.getItemPosition(lastVisibleViewHolderPosition);
    }
}
