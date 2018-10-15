package com.bilibili.following.prvannotations;

import android.support.annotation.LayoutRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface PrvBinder {

    //Best Practice 布局文件一般以item_binding开头 (R.layout.item_binding_button)
    @LayoutRes int value() default -1;

}
