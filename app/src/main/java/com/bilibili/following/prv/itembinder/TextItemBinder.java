package com.bilibili.following.prv.itembinder;

import com.bilibili.following.prv.binder.TestABinder;
import com.bilibili.following.prv.binder.TextBinder;
import com.bilibili.following.prv.model.ColorNamePrimitive;
import com.bilibili.following.prvannotations.PrvItemBinder;

@PrvItemBinder(binder = {TextBinder.class, TestABinder.class}, type = ColorNamePrimitive.class)
public abstract class TextItemBinder {
}
