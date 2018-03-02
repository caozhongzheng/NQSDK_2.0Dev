package com.nqmobile.livesdk.commons.preference;


public interface PreferenceService {
	
	public String getStringValue(String key);
	
	public void setStringValue(String key, String value);
	
	public boolean getBooleanValue(String key);
	
	public void setBooleanValue(String key, boolean value);
	
	public long getLongValue(String key);
	
	public void setLongValue(String key, long value);
	
	public int getIntValue(String key);
	
	public void setIntValue(String key, int value);
}
