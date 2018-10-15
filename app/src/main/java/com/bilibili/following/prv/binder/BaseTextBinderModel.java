package com.bilibili.following.prv.binder;

import android.view.View;

import com.bilibili.following.prvannotations.PrvAttribute;
import com.bilibili.following.prvlibrary.binder.BindingModel;

public abstract class BaseTextBinderModel implements BindingModel {

    @PrvAttribute
    private String textRes;

    @PrvAttribute
    private View.OnClickListener listener;


//    private BaseTextBinderModel(Builder builder) {
//        setTextRes(builder.textRes);
//    }
//
//    public String getTextRes() {
//        return textRes;
//    }
//
//    public void setTextRes(String textRes) {
//        this.textRes = textRes;
//    }

//    public static final class Builder {
//        private String textRes;
//
//        public Builder() {
//        }
//
//        public Builder text(String val) {
//            textRes = val;
//            return this;
//        }
//
//        public BaseTextBinderModel build() {
//            return new BaseTextBinderModel(this);
//        }
//    }
}
