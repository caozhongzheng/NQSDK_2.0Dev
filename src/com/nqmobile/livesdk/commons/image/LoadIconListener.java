package com.nqmobile.livesdk.commons.image;

import android.graphics.Bitmap;

import com.nqmobile.livesdk.commons.net.Listener;

/**
 * 获取图片监听
 * @time 2013-12-5 下午5:28:56
 */
public interface LoadIconListener extends Listener {
	public void onLoadComplete(Bitmap result);
}
