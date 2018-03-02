package com.nqmobile.livesdk.commons.preference;


/**
 *  配置管理基类
 *
 */
public abstract class AbsPreference {
	private PreferenceService mPreference;

    public AbsPreference(String name){
        mPreference = PreferenceServiceFactory.getService(name);
	}
	public String getStringValue(String key){
		synchronized (mPreference) {
			return mPreference.getStringValue(key);
		}
	}
	
	public void setStringValue(String key,String value){
		synchronized (mPreference) {
			mPreference.setStringValue(key, value);
		}
	}
	
	public boolean getBooleanValue(String key){
		synchronized (mPreference) {
			return mPreference.getBooleanValue(key);
		}
	}
	
	public void setBooleanValue(String key,boolean value){
		synchronized (mPreference) {
			mPreference.setBooleanValue(key, value);
		}
	}
	
	public long getLongValue(String key){
		synchronized (mPreference) {
			return mPreference.getLongValue(key);
		}
	}
	
	public void setLongValue(String key,long value){
		synchronized (mPreference) {
			mPreference.setLongValue(key, value);
		}
	}
	
	public int getIntValue(String key){
		synchronized (mPreference) {
			return mPreference.getIntValue(key);
		}
	}
	
	public void setIntValue(String key,int value){
		synchronized (mPreference) {
			mPreference.setIntValue(key, value);
		}
	}
}
