package com.nqmobile.livesdk.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

public class GpUtils {
	public static void viewDetail(Context context, String url){
		if (TextUtils.isEmpty(url) || context == null){
			return;
		}
		
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
}
