package com.nqmobile.livesdk.commons.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TypefaceLayoutInflater extends LayoutInflater {

	private LayoutInflater mOriginal;

	public TypefaceLayoutInflater(final LayoutInflater original, final Context context) {
		super(original, context);
		mOriginal = original;
	}

	@Override
	public LayoutInflater cloneInContext(final Context newContext) {
		return new TypefaceLayoutInflater(this, newContext);
	}

	public void setFactory(Factory factory){
		if (factory!=null && mOriginal.getFactory() == null) {
			mOriginal.setFactory(factory);
		}
	}

	@Override
	public View inflate(int resource, ViewGroup root, boolean attachToRoot){
		View view = mOriginal.inflate(resource, root, attachToRoot);

		Typeface typeface = FontFactory.getDefaultTypeface();
		if (view instanceof TextView) {
			TextView tv = (TextView) view;
			replaceTypeface(tv, typeface);
		}
		else if (view instanceof ViewGroup) {
			setTypeFace(typeface, (ViewGroup)view);
		}

		return view;
	}

	public static void setTypeFace(Typeface typeface, ViewGroup parent) {
		for (int i = 0; i < parent.getChildCount(); i++) {
			View v = parent.getChildAt(i);
			if (v instanceof ViewGroup) {
				setTypeFace(typeface, (ViewGroup) v);
			} else if (v instanceof TextView) {
				TextView tv = (TextView) v;
				replaceTypeface(tv, typeface);
			}
		}
	}

	public static void replaceTypeface(TextView tv, Typeface typeface) {
		int style = 0;
		Typeface oldTypeface = tv.getTypeface();
		TextPaint textPaint = tv.getPaint();
		if (oldTypeface != null) {
			style = oldTypeface.getStyle();
		}
		if (textPaint != null) {
			if (textPaint.isFakeBoldText()) {
				style = style | Typeface.BOLD;
			}
			if (textPaint.getTextSkewX() != 0) {
				style = style | Typeface.ITALIC;
			}
		}

		tv.setTypeface(typeface, style);
	}



}