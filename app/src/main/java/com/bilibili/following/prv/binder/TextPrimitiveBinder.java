package com.bilibili.following.prv.binder;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.bilibili.following.prv.R;
import com.bilibili.following.prv.model.ColorNamePrimitive;
import com.bilibili.following.prv.viewholder.TextPrimitiveViewHolder;
import com.bilibili.following.prvlibrary.Binder;
import com.bilibili.following.prvlibrary.ViewHolder;

import java.util.List;

public class TextPrimitiveBinder implements Binder<ColorNamePrimitive, TextPrimitiveViewHolder> {

    @Override
    public int getViewType() {
        return R.layout.item_text;
    }

    @Override
    public TextPrimitiveViewHolder create(ViewGroup parent) {
        return null;
    }

    @Override
    public void prepare(@NonNull ColorNamePrimitive model, List<Binder<? super ColorNamePrimitive, ? extends ViewHolder>> binders, int binderIndex) {

    }

    @Override
    public void bind(@NonNull ColorNamePrimitive model, @NonNull TextPrimitiveViewHolder holder, @NonNull List<Binder<? super ColorNamePrimitive, ? extends ViewHolder>> binders, int binderIndex) {

    }

    @Override
    public void unbind(@NonNull TextPrimitiveViewHolder holder) {

    }
}
