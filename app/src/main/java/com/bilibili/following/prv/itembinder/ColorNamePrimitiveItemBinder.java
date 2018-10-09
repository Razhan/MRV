package com.bilibili.following.prv.itembinder;

import android.support.annotation.NonNull;

import com.bilibili.following.prv.binder.TextPrimitiveBinder;
import com.bilibili.following.prv.model.ColorNamePrimitive;
import com.bilibili.following.prvlibrary.Binder;
import com.bilibili.following.prvlibrary.ItemBinder;
import com.bilibili.following.prvlibrary.ViewHolder;

import java.util.Collections;
import java.util.List;

public class ColorNamePrimitiveItemBinder
        implements ItemBinder<ColorNamePrimitive, Binder<ColorNamePrimitive, ? extends ViewHolder>> {

    @NonNull
    @Override
    public List<? extends Binder<ColorNamePrimitive, ? extends ViewHolder>> getBinderList(@NonNull ColorNamePrimitive model, int position) {
        return Collections.singletonList(new TextPrimitiveBinder());
    }

}
