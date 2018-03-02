package com.nqmobile.livesdk.commons.ui;

import android.graphics.Typeface;

public class FontFactory {
	private static Typeface sDefault;

	public static void setDefaultTypeface(Typeface defaultTypeface) {
		sDefault = defaultTypeface;
	}

	public static Typeface getDefaultTypeface() {
		if (sDefault != null) {
			return sDefault;
		} else {
			return Typeface.DEFAULT;
		}
	}

	public static boolean hasSetDefaultTypeface() {
		return sDefault != null;
	}
	
}
