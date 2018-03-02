package com.nqmobile.livesdk.utils;

import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;

/*
 * 解析Uri的Query参数成键值对，比如a=3&b=5
 */
public class UriQueryUtils {
	public static Map<String, String> parse(String parameters){
		if (TextUtils.isEmpty(parameters)){
			return null;
		}
		
		String[] allParams = parameters.split("\\&");
		Map<String, String> result = new HashMap<String, String>(allParams.length);
		for(String keyValue : allParams){
			/*String[] kv = keyValue.split("=");
			String key = kv[0].trim();
			String value = kv.length > 1 ? kv[1].trim() : "";
			result.put(key, value);*/
			
			String[] kv = keyValue.split("=", 0);
			String key = kv[0].trim();
			StringBuffer value = new StringBuffer();
			if(kv.length > 2){
				for (int i = 1; i < kv.length; i++) {
					value.append(kv[i].trim());
					if(i != kv.length - 1){
						value.append("=");
					}
				}
			}else {
				value.append(kv.length > 1 ? kv[1].trim() : "");
			}
			result.put(key, value.toString());
		}
		
		return result;
	}
}
