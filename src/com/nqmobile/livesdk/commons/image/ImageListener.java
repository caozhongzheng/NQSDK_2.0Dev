package com.nqmobile.livesdk.commons.image;

import android.graphics.drawable.BitmapDrawable;

import com.nqmobile.livesdk.commons.net.Listener;

public interface ImageListener extends Listener{
	
	public void getImageSucc(String url, BitmapDrawable drawable);
}
