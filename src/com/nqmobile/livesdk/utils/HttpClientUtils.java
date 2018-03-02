package com.nqmobile.livesdk.utils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.text.TextUtils;

import com.nqmobile.livesdk.commons.log.NqLog;

public class HttpClientUtils {
	private static final int CONNECT_TIMEOUT = 10 * 1000;
	private static final int READ_TIMEOUT = 10 * 1000;
	
	public static boolean downloadUrl(String url, String outputFileName){
		if (TextUtils.isEmpty(url)){
			return false;
		}
		
		boolean result = false;
		HttpURLConnection conn = null;
		InputStream is = null;
		try {
			URL targetUrl = new URL(url);
			conn = (HttpURLConnection) targetUrl.openConnection();
			conn.setConnectTimeout(CONNECT_TIMEOUT);
			conn.setReadTimeout(READ_TIMEOUT);
			conn.setInstanceFollowRedirects(true);
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				is = conn.getInputStream();
				result = FileUtil.writeStreamToFile(outputFileName, is);
			}
		} catch (Exception e) {
			NqLog.e(e);
		} finally {
			FileUtil.closeStream(is);
			if (conn != null) {
				conn.disconnect();
			}
		}
		
		return result;
	}
}
