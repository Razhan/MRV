package com.bilibili.following.prv.itembinder;

import android.support.annotation.NonNull;

import com.bilibili.following.prv.binder.TextBinder;
import com.bilibili.following.prv.model.ColorNamePrimitive;
import com.bilibili.following.prvannotations.PrvItemBinder;
import com.bilibili.following.prvlibrary.binder.Binder;
import com.bilibili.following.prvlibrary.binder.ItemBinder;
import com.bilibili.following.prvlibrary.viewholder.ViewHolder;

import java.util.List;

@PrvItemBinder(binder = {TextBinder.class}, type = ColorNamePrimitive.class)
public abstract class TextItemBinder {
}
