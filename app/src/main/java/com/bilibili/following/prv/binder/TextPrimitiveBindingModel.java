package com.bilibili.following.prv.binder;

import com.bilibili.following.prvlibrary.binder.BindingModel;

public class TextPrimitiveBindingModel implements BindingModel {

    private String textRes;

    private TextPrimitiveBindingModel(Builder builder) {
        setTextRes(builder.textRes);
    }

    public String getTextRes() {
        return textRes;
    }

    public void setTextRes(String textRes) {
        this.textRes = textRes;
    }


    public static final class Builder {
        private String textRes;

        public Builder() {
        }

        public Builder text(String val) {
            textRes = val;
            return this;
        }

        public TextPrimitiveBindingModel build() {
            return new TextPrimitiveBindingModel(this);
        }
    }
}
