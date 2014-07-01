package com.olyware.game3072.utils;

import android.content.Context;
import android.graphics.Typeface;

public class Typefaces {

	protected static Typefaces instance;
	public Typeface robotoLight;
	public Typeface robotoCondensed;

	protected Typefaces(Context context) {
		try {
			robotoLight = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.otf");
		} catch (Exception e) {
			// do nothing
		}
		try {
			robotoCondensed = Typeface.createFromAsset(context.getAssets(), "RobotoCondensed-Regular.ttf");
		} catch (Exception e) {
			// do nothing
		}
	}

	public static Typefaces getInstance(Context context) {
		if (instance == null) {
			instance = new Typefaces(context);
		}
		return instance;
	}
}
