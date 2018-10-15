package com.bilibili.following.prv;

import com.bilibili.following.prv.binder.TextBinder$Impl;
import com.bilibili.following.prv.itembinder.TextItemBinder$Impl;
import com.bilibili.following.prv.model.ColorNamePrimitive;
import com.bilibili.following.prv.model.Primitive;
import com.bilibili.following.prvannotations.PrvAdapter;
import com.bilibili.following.prvlibrary.BasePrvAdapter;

@PrvAdapter
public class PrimitiveAdapter extends BasePrvAdapter<Primitive> {

    public PrimitiveAdapter() {
        register(ColorNamePrimitive.class, new TextItemBinder$Impl());
        mBinderInfo.put(R.layout.item_binding_button, new TextBinder$Impl());
    }

}
