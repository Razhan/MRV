package com.bilibili.following.prv.binder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.bilibili.following.prv.R;
import com.bilibili.following.prv.model.ColorNamePrimitive;
import com.bilibili.following.prvannotations.PrvBinder;
import com.bilibili.following.prvannotations.PrvOnClick;
import com.bilibili.following.prvlibrary.binder.Binder;
import com.bilibili.following.prvlibrary.binder.DataBindingBinder;
import com.bilibili.following.prvlibrary.listener.ActionListener;
import com.bilibili.following.prvlibrary.viewholder.DataBindingViewHolder;
import com.bilibili.following.prvlibrary.viewholder.ViewHolder;

import java.util.List;

@PrvBinder(R.layout.item_binding_button)
public abstract class TextBinder extends DataBindingBinder<ColorNamePrimitive, DataBindingViewHolder, ButtonBindingModel> {

    @NonNull
    @Override
    protected ButtonBindingModel prepareBindingModel(ColorNamePrimitive model) {
        return new ButtonBindingModel()
                .textRes(model.getString())
                .colorInt(model.getColor());
    }

    @Nullable
    @Override
    @PrvOnClick({R.id.button})
    public ActionListener<ColorNamePrimitive, DataBindingViewHolder> getListener() {
        return new ActionListener<ColorNamePrimitive, DataBindingViewHolder>() {
            @Override
            public void act(View v, ColorNamePrimitive model, DataBindingViewHolder holder, List<Binder<? super ColorNamePrimitive, ? extends ViewHolder>> binders, int binderIndex) {
                switch (v.getId()) {
                    case R.id.button:
                        Toast.makeText(v.getContext(), model.getString(), Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        };
    }
}
