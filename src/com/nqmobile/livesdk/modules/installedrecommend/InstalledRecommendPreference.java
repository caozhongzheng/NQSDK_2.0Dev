package com.nqmobile.livesdk.modules.installedrecommend;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class InstalledRecommendPreference extends SettingsPreference {
	// ===========================================================
	// Constants
	// ===========================================================
    public static final String KEY_INSTALLED_RECOMMEND_ENABLE = "installed_recommend_enable";//安装后关联推荐客户端开关key
    public static final String KEY_LAST_INSTALLED_RECOMMEND_PACKAGE = "last_installed_recommend_package";//上一次取安装后关联推荐传入的包
    public static final String KEY_LAST_INSTALLED_RECOMMEND_TIME = "last_installed_recommend_time";//上一次取安装后关联推荐的时间
	// ===========================================================
	// Fields
	// ===========================================================
    private static InstalledRecommendPreference sInstance;

	// ===========================================================
	// Constructors
	// ===========================================================
	private InstalledRecommendPreference() {
	}
	
	public static InstalledRecommendPreference getInstance(){
		if(sInstance == null){
			sInstance = new InstalledRecommendPreference();
		}
		
		return sInstance;
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================
    public boolean isInstalledRecommendEnable(){
        return getBooleanValue(KEY_INSTALLED_RECOMMEND_ENABLE);
    }
    
    public void setInstalledRecommendEnable(boolean enable){
        setBooleanValue(KEY_INSTALLED_RECOMMEND_ENABLE, enable);
    }
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}