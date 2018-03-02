package com.nqmobile.livesdk.commons.preference;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.db.DataProvider;
import com.nqmobile.livesdk.utils.StringUtil;

/**
 *  配置管理类
 *
 */
public class AndroidPreferenceService implements PreferenceService {
	private static Map<String, AndroidPreferenceService> sInstanceMap = new HashMap<String, AndroidPreferenceService>();
    private Context mContext;
	private String mName;
	
	public synchronized static AndroidPreferenceService getInstance(String name) {
		AndroidPreferenceService ins = sInstanceMap.get(name);
		if (ins == null) {
			sInstanceMap.put(name, ins = new AndroidPreferenceService(name));
		}
		return ins;
	}
    
    private AndroidPreferenceService(String name){
    	mContext = ApplicationContext.getContext();
    	mName = name;
    }
    
    private Bundle get(String methodName, String key){
    	String[] projection = new String[]{mName, methodName, key};
    	Cursor cursor = mContext.getContentResolver().query(DataProvider.TABLE_PREF_SERVICE_URI, projection, null, null, null);
    	
    	Bundle result = null;
    	if (cursor == null){
    		result = new Bundle();
    	} else {
    		result = cursor.getExtras();
    		cursor.close();
    	}
    	
    	return result;
    }
    
    @Override
	public String getStringValue(String key){
    	Bundle rs = get("getStringValue", key);
		return rs.getString(key);
	}
	
    @Override
	public void setStringValue(String key,String value){
		if(StringUtil.isNullOrEmpty(key))
			return;
    	ContentValues values = new ContentValues();
    	values.put(DataProvider.KEY_PREF_FILE_NAME, mName);
    	values.put(DataProvider.KEY_PREF_METHOD, "setStringValue");
    	values.put(DataProvider.KEY_PREF_KEY, key);
    	values.put(DataProvider.KEY_PREF_VAL,  (value == null ? "" : value));
    	mContext.getContentResolver().update(DataProvider.TABLE_PREF_SERVICE_URI, values, null, null);
	}
    
    @Override
	public boolean getBooleanValue(String key){
    	Bundle rs = get("getBooleanValue", key);
		return rs.getBoolean(key);
	}
	
    @Override
	public void setBooleanValue(String key,boolean value){
		if(StringUtil.isNullOrEmpty(key))
			return;
    	ContentValues values = new ContentValues();
    	values.put(DataProvider.KEY_PREF_FILE_NAME, mName);
    	values.put(DataProvider.KEY_PREF_METHOD, "setBooleanValue");
    	values.put(DataProvider.KEY_PREF_KEY, key);
    	values.put(DataProvider.KEY_PREF_VAL, value);
    	mContext.getContentResolver().update(DataProvider.TABLE_PREF_SERVICE_URI, values, null, null);
	}
	
    @Override
	public long getLongValue(String key){
    	Bundle rs = get("getLongValue", key);
		return rs.getLong(key, 0);
	}
	
	@Override
	public void setLongValue(String key, long value) {
		if (StringUtil.isNullOrEmpty(key))
			return;
    	ContentValues values = new ContentValues();
    	values.put(DataProvider.KEY_PREF_FILE_NAME, mName);
    	values.put(DataProvider.KEY_PREF_METHOD, "setLongValue");
    	values.put(DataProvider.KEY_PREF_KEY, key);
    	values.put(DataProvider.KEY_PREF_VAL, value);
    	mContext.getContentResolver().update(DataProvider.TABLE_PREF_SERVICE_URI, values, null, null);
	}
	
    @Override
	public int getIntValue(String key){
    	Bundle rs = get("getIntValue", key);
		return rs.getInt(key, 0);
	}
	
    @Override
	public void setIntValue(String key,int value){
		if(StringUtil.isNullOrEmpty(key))
			return;
    	ContentValues values = new ContentValues();
    	values.put(DataProvider.KEY_PREF_FILE_NAME, mName);
    	values.put(DataProvider.KEY_PREF_METHOD, "setIntValue");
    	values.put(DataProvider.KEY_PREF_KEY, key);
    	values.put(DataProvider.KEY_PREF_VAL, value);
    	mContext.getContentResolver().update(DataProvider.TABLE_PREF_SERVICE_URI, values, null, null);

	}
}
