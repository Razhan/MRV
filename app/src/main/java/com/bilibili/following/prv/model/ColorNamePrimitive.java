package com.bilibili.following.prv.model;

import android.support.annotation.ColorRes;

public class ColorNamePrimitive extends Primitive {
	@ColorRes
	private int color;

	private final String string;

	public ColorNamePrimitive(final int color, final String string) {
		this.color = color;
		this.string = string;
	}

	public void setColor(@ColorRes final int color) {
		this.color = color;
	}

	@ColorRes
	public int getColor() {
		return color;
	}

	public String getString() {
		return string;
	}
}
