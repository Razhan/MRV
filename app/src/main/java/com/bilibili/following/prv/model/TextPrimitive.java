package com.bilibili.following.prv.model;

public class TextPrimitive implements Primitive.Text {
	private final String string;

	public TextPrimitive(final String string) {
		this.string = string;
	}

	@Override
	public String getString() {
		return string;
	}
}
