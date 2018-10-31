package com.bilibili.bbq.feed.itembinder;

import com.bilibili.bbq.feed.binder.TestABinder;
import com.bilibili.bbq.feed.binder.TextBinder;
import com.bilibili.bbq.feed.model.TestPrimitive;
import com.bilibili.bbq.feedannotations.PrvItemBinder;

@PrvItemBinder(binder = {TextBinder.class, TestABinder.class}, type = TestPrimitive.class)
public abstract class TextItemBinder {
}
