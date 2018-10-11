package com.bilibili.following.prv.binder;

import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.bilibili.following.prv.BR;
import com.bilibili.following.prv.R;
import com.bilibili.following.prv.model.ColorNamePrimitive;
import com.bilibili.following.prvlibrary.binder.Binder;
import com.bilibili.following.prvlibrary.viewholder.ViewHolder;

import java.util.List;

public class TextPrimitiveBinderImpl extends TextPrimitiveBinder {

    @Override
    public int getViewType() {
        return R.layout.item_binding_button;
    }

    @Override
    public TextPrimitiveViewHolder create(ViewGroup parent) {
        return new TextPrimitiveViewHolder(buildView(parent));
    }

    @Override
    public void prepare(@NonNull ColorNamePrimitive model, List <Binder<? super ColorNamePrimitive, ? extends ViewHolder>> binders, int binderIndex) {
        
    }



    @Override
    protected void setDataBindingVariables(ColorNamePrimitive model, ViewDataBinding binding) {
        TextPrimitiveBindingModel bindingModel = prepareBindingModel(model);
        binding.setVariable(BR.textRes, bindingModel.getTextRes());
    }

    @Override
    public void unbind(@NonNull TextPrimitiveViewHolder holder) {

    }
}
