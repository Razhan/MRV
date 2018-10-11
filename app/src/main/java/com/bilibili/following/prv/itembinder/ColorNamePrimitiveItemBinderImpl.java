package com.bilibili.following.prv.itembinder;

import android.support.annotation.NonNull;

import com.bilibili.following.prv.binder.TextPrimitiveBinderImpl;
import com.bilibili.following.prv.model.ColorNamePrimitive;
import com.bilibili.following.prvlibrary.binder.Binder;
import com.bilibili.following.prvlibrary.viewholder.ViewHolder;

import java.util.Collections;
import java.util.List;

public class ColorNamePrimitiveItemBinderImpl extends ColorNamePrimitiveItemBinder {

    @NonNull
    @Override
    public List<? extends Binder<ColorNamePrimitive, ? extends ViewHolder>> getBinderList(@NonNull ColorNamePrimitive model, int position) {
        return Collections.singletonList(new TextPrimitiveBinderImpl());
    }
}
