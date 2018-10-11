package com.bilibili.following.prv.binder;

import com.bilibili.following.prvlibrary.binder.BindingModel;

public class TextBinderModel implements BindingModel {

    private String textRes;

    private TextBinderModel(Builder builder) {
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

        public TextBinderModel build() {
            return new TextBinderModel(this);
        }
    }
}
