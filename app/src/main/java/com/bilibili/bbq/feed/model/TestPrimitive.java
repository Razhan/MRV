package com.bilibili.bbq.feed.model;

import android.support.annotation.ColorRes;

public class TestPrimitive implements Primitive {
	private int color;

	private final String string;

	public TestPrimitive(final int color, final String string) {
		this.color = color;
		this.string = string;
	}

	public void setColor(final int color) {
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
