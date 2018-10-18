package com.bilibili.following.prv.binder;

import android.support.annotation.NonNull;

import com.bilibili.following.prv.R;
import com.bilibili.following.prv.model.ColorNamePrimitive;
import com.bilibili.following.prv.viewholder.TestAViewHolder;
import com.bilibili.following.prvannotations.PrvBinder;
import com.bilibili.following.prvlibrary.binder.Binder;
import com.bilibili.following.prvlibrary.viewholder.ViewHolder;

import java.util.List;

@PrvBinder(R.layout.item_test_a)
public abstract class TestABinder implements Binder<ColorNamePrimitive, TestAViewHolder> {

//    @Override
//    public void prepare(@NonNull ColorNamePrimitive model, List<Binder<? super ColorNamePrimitive, ? extends ViewHolder>> binders, int binderIndex) {
//
//    }

    @Override
    public void bind(@NonNull ColorNamePrimitive model, @NonNull TestAViewHolder holder, @NonNull List<Binder<? super ColorNamePrimitive, ? extends ViewHolder>> binders, int binderIndex) {

    }

//    @Override
//    public void unbind(@NonNull TestAViewHolder holder) {
//
//    }
}
