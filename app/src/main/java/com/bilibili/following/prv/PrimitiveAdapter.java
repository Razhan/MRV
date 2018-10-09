package com.bilibili.following.prv;

import com.bilibili.following.prv.binder.TextPrimitiveBinder;
import com.bilibili.following.prv.itembinder.ColorNamePrimitiveItemBinder;
import com.bilibili.following.prv.model.ColorNamePrimitive;
import com.bilibili.following.prv.model.Primitive;
import com.bilibili.following.prvlibrary.PRVAdapter;

public class PrimitiveAdapter extends PRVAdapter<Primitive> {

    public PrimitiveAdapter() {
        register(ColorNamePrimitive.class, new ColorNamePrimitiveItemBinder());
        mBinderInfo.put(R.layout.item_text, new TextPrimitiveBinder());
    }
}
