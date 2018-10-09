package com.bilibili.following.prv.model;

import android.support.annotation.ColorRes;

public interface Primitive {

	interface Text extends Primitive {
		String getString();
	}

	interface Color extends Primitive {
		@ColorRes
		int getColor();
	}

	class Header implements Primitive {
		// Dummy marker class
	}
}
