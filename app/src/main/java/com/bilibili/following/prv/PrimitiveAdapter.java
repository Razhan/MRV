package com.bilibili.following.prv;

import com.bilibili.following.prv.binder.TextPrimitiveBinderImpl;
import com.bilibili.following.prv.itembinder.ColorNamePrimitiveItemBinderImpl;
import com.bilibili.following.prv.model.ColorNamePrimitive;
import com.bilibili.following.prv.model.Primitive;
import com.bilibili.following.prvlibrary.PrvAdapter;

public class PrimitiveAdapter extends PrvAdapter<Primitive> {

    public PrimitiveAdapter() {
        register(ColorNamePrimitive.class, new ColorNamePrimitiveItemBinderImpl());
        mBinderInfo.put(R.layout.item_binding_button, new TextPrimitiveBinderImpl());
    }
}
