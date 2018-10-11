package com.bilibili.following.prv.itembinder;

import com.bilibili.following.prv.binder.TextPrimitiveBinder;
import com.bilibili.following.prv.model.ColorNamePrimitive;
import com.bilibili.following.prvannotations.PrvItemBinder;
import com.bilibili.following.prvlibrary.binder.Binder;
import com.bilibili.following.prvlibrary.binder.ItemBinder;
import com.bilibili.following.prvlibrary.viewholder.ViewHolder;

@PrvItemBinder(TextPrimitiveBinder.class)
public abstract class ColorNamePrimitiveItemBinder
        implements ItemBinder<ColorNamePrimitive, Binder<ColorNamePrimitive, ? extends ViewHolder>> {
}
