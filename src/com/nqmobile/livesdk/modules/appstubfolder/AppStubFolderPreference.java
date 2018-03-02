package com.nqmobile.livesdk.modules.appstubfolder;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class AppStubFolderPreference extends SettingsPreference {
	// ===========================================================
	// Constants
	// ===========================================================
	/** 虚框文件夹开关 */
	public static final String KEY_STUB_FOLDER_ENABLE = "stub_folder_enable";// 虚框文件夹开关
	public static final String KEY_STUB_FOLDER_FIRST_ENABLED = "stub_folder_first_enabled";// 虚框文件夹首次成功获取
	public static final String KEY_STUB_FOLDER_SHOW_COUNT = "stub_folder_show_count";// 虚框文件夹内应用展示次数
	public static final String KEY_STUB_FOLDER_OPRATABLE = "stub_folder_opratable";// 虚框文件夹能否编辑删除
	public static final String KEY_STUB_FOLDER_RES_DELETABLE = "stub_folder_res_deletable";// 虚框文件夹内虚框资源能否被删除
	public static final String KEY_STUB_FOLDER_FREQ_WIFI = "stub_folder_freq_wifi";
	public static final String KEY_STUB_FOLDER_FREQ_3G = "stub_folder_freq_3g";
	public static final String KEY_LAST_GET_STUB_FOLDER_TIME = "last_get_stub_folder_time";
	public static final String KEY_LAST_PRELOAD_STUB_FOLDER_TIME = "last_preload_stub_folder_time";// 虚框文件夹ICON前回获取时间
	public static final String KEY_LAST_PRELOAD_STUB_FOLDER_APP_TIME = "last_preload_stub_folder_app_time";// 虚框文件夹内APP的ICON前回获取时间
	public static final String KEY_STUB_FOLDER_DELETE_FOLDER = "stub_folder_delete_folderIDs";// 虚框文件夹被删除的文件ID
	public static final String KEY_STUB_FOLDER_CREATE_FOLDER = "stub_folder_create_folderIDs";// 虚框文件夹已创建的文件ID
	// ===========================================================
	// Fields
	// ===========================================================
    private static AppStubFolderPreference sInstance;

	// ===========================================================
	// Constructors
	// ===========================================================
	private AppStubFolderPreference() {
	}
	
	public static AppStubFolderPreference getInstance(){
		if(sInstance == null){
			sInstance = new AppStubFolderPreference();
		}
		
		return sInstance;
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================
    public boolean isStubFolderEnable(){
        return getBooleanValue(KEY_STUB_FOLDER_ENABLE);
    }
    public void setStubFolderEnable(boolean enable){
        setBooleanValue(KEY_STUB_FOLDER_ENABLE, enable);
    }
    
    public boolean isStubFolderFirstEnabled(){
        return getBooleanValue(KEY_STUB_FOLDER_FIRST_ENABLED);
    }
    public void setStubFolderFirstEnabled(boolean enable){
        setBooleanValue(KEY_STUB_FOLDER_FIRST_ENABLED, enable);
    }
    
    public long getStubFolderFreqWifiInMinutes() {
        return getLongValue(KEY_STUB_FOLDER_FREQ_WIFI);
    }
    public void setStubFolderFreqWifiInMinutes(long time) {
        setLongValue(KEY_STUB_FOLDER_FREQ_WIFI, time);
    }
    
    public long getStubFolderFreq3GInMinutes() {
        return getLongValue(KEY_STUB_FOLDER_FREQ_3G);
    }
    public void setStubFolderFreq3GInMinutes(long time) {
        setLongValue(KEY_STUB_FOLDER_FREQ_3G, time);
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
