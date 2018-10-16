package com.bilibili.following.prv.binder;


import android.support.annotation.NonNull;

import com.bilibili.following.prv.R;
import com.bilibili.following.prv.model.ColorNamePrimitive;
import com.bilibili.following.prvannotations.PrvBinder;
import com.bilibili.following.prvlibrary.binder.DataBindingBinder;
import com.bilibili.following.prvlibrary.viewholder.DataBindingViewHolder;

@PrvBinder(R.layout.item_binding_button)
public abstract class TextBinder extends DataBindingBinder<ColorNamePrimitive, DataBindingViewHolder, ButtonBindingModel> {

    @NonNull
    @Override
    protected ButtonBindingModel prepareBindingModel(ColorNamePrimitive model) {
        // TODO: 10/16/18 重新setValue而不是new新对象
        return new ButtonBindingModel()
                .textRes(model.getString())
                .colorInt(model.getColor());
    }
}
