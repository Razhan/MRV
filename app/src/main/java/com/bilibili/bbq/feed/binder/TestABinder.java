package com.bilibili.bbq.feed.binder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.bilibili.bbq.feed.R;
import com.bilibili.bbq.feed.model.TestPrimitive;
import com.bilibili.bbq.feed.viewholder.TestAViewHolder;
import com.bilibili.bbq.feedannotations.PrvBinder;
import com.bilibili.bbq.feedannotations.PrvOnClick;
import com.bilibili.bbq.feedlib.binder.BaseBinder;
import com.bilibili.bbq.feedlib.binder.Binder;
import com.bilibili.bbq.feedlib.listener.ActionListener;
import com.bilibili.bbq.feedlib.viewholder.ViewHolder;

import java.util.List;

@PrvBinder(R.layout.item_test_a)
public abstract class TestABinder extends BaseBinder<TestPrimitive, TestAViewHolder> {

    @Override
    public void bind(@NonNull TestPrimitive model, @NonNull TestAViewHolder holder, @NonNull List<Binder<? super TestPrimitive, ? extends ViewHolder>> binders, int binderIndex, @NonNull List<Object> payloads) {
        super.bind(model, holder, binders, binderIndex, payloads);
    }

    @Nullable
    @Override
    @PrvOnClick({R.id.button})
    public ActionListener<TestPrimitive, TestAViewHolder> getListener() {
        return new ActionListener<TestPrimitive, TestAViewHolder>() {
            @Override
            public void act(View v, TestPrimitive model, TestAViewHolder holder, List<Binder<? super TestPrimitive, ? extends ViewHolder>> binders, int binderIndex) {
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
