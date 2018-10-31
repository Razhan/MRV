package com.bilibili.bbq.feedannotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface PrvItemBinder {
    //约定如果没有显性定义，隐性使用父类第一项泛型类型
    Class type() default None.class;

    //顺序很重要
    Class[] binder() default {};

}