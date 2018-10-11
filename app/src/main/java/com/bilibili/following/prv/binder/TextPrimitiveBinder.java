package com.bilibili.following.prv.binder;


import com.bilibili.following.prv.R;
import com.bilibili.following.prv.model.ColorNamePrimitive;
import com.bilibili.following.prvannotations.PrvBinder;
import com.bilibili.following.prvlibrary.binder.BindingModel;
import com.bilibili.following.prvlibrary.binder.DataBindingBinder;

@PrvBinder(R.layout.item_binding_button)
public abstract class TextPrimitiveBinder extends DataBindingBinder<ColorNamePrimitive, TextPrimitiveViewHolder, TextPrimitiveBindingModel> {

    @Override
    protected TextPrimitiveBindingModel prepareBindingModel(ColorNamePrimitive model) {
        return new TextPrimitiveBindingModel.Builder()
                .text(model.getString()).build();
    }
}
