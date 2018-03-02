package com.nqmobile.livesdk.modules.association;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class AssociationPreference extends SettingsPreference {
	// ===========================================================
	// Constants
	// ===========================================================
    public static final String KEY_ASSOCIATION_ENABLE = "association_enable";//关联推荐客户端开关
	public static final String KEY_HAVE_ASSOCIATION = "have_association";//有新的关联推荐信息
	
    public static final String KEY_MAX_ASSOCIATION_SHOWTIMES = "association_max_show_times";//关联推荐最大展示次数
    public static final String KEY_MAX_ASSOCIATION_SHOW_INTERVAL = "association_show_interval";//关联推荐展示间隔天数
    public static final String KEY_MAX_ASSOCIATION_LAST_VERSIONTAG = "association_last_version_tag";//关联推荐展示上一次服务器下发的时间戳
    public static final String KEY_ASSOCIATION_LAST_SHOWTIME = "association_last_show_time";//关联推荐展示上一次展示间戳
	// ===========================================================
	// Fields
	// ===========================================================
    private static AssociationPreference sInstance;

	// ===========================================================
	// Constructors
	// ===========================================================
	private AssociationPreference() {
	}
	
	public static AssociationPreference getInstance(){
		if(sInstance == null){
			sInstance = new AssociationPreference();
		}
		
		return sInstance;
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================
    public boolean isAssociationEnable(){
        return getBooleanValue(KEY_ASSOCIATION_ENABLE);
    }
    public void setAssociationEnable(boolean enable){
        setBooleanValue(KEY_ASSOCIATION_ENABLE, enable);
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