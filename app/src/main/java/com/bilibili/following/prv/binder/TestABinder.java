package com.bilibili.following.prv.binder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.bilibili.following.prv.R;
import com.bilibili.following.prv.model.ColorNamePrimitive;
import com.bilibili.following.prv.viewholder.TestAViewHolder;
import com.bilibili.following.prvannotations.PrvBinder;
import com.bilibili.following.prvannotations.PrvOnClick;
import com.bilibili.following.prvlibrary.binder.BaseBinder;
import com.bilibili.following.prvlibrary.binder.Binder;
import com.bilibili.following.prvlibrary.listener.ActionListener;
import com.bilibili.following.prvlibrary.viewholder.ViewHolder;

import java.util.List;

@PrvBinder(R.layout.item_test_a)
public abstract class TestABinder extends BaseBinder<ColorNamePrimitive, TestAViewHolder> {

    @Override
    public void bind(@NonNull ColorNamePrimitive model, @NonNull TestAViewHolder holder, @NonNull List<Binder<? super ColorNamePrimitive, ? extends ViewHolder>> binders, int binderIndex, @NonNull List<Object> payloads) {
        super.bind(model, holder, binders, binderIndex, payloads);
    }

    @Nullable
    @Override
    @PrvOnClick({R.id.button})
    public ActionListener<ColorNamePrimitive, TestAViewHolder> getListener() {
        return new ActionListener<ColorNamePrimitive, TestAViewHolder>() {
            @Override
            public void act(View v, ColorNamePrimitive model, TestAViewHolder holder, List<Binder<? super ColorNamePrimitive, ? extends ViewHolder>> binders, int binderIndex) {
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
