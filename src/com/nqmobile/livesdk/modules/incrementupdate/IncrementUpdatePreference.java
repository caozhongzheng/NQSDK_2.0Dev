package com.nqmobile.livesdk.modules.incrementupdate;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class IncrementUpdatePreference extends SettingsPreference {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String KEY_HAVE_UPDATE = "have_update";
    public static final String KEY_HAS_NEW_VERSION = "has_new_version";
    
    public static final String KEY_LAST_INCREMENT_UPDATE_VERSION = "last_increment_update_version";//上一次增量升级的版本号
    public static final String KEY_LAST_INCREMENT_UPDATE_PROMPT_TIME = "last_increment_update_time";//上一次增量升级的弹框的时间 yyyy-MM-dd
    public static final String KEY_LAST_INCREMENT_UPDATE_PROMPT_COUNT = "last_increment_update_count";//上一次增量升级的弹框的次数
    public static final String KEY_INCREMENT_UPDATE_DOWNLOAD_FULLPACK_PATH = "increment_update_download_fullpack_path";//增量升级下载全量包的路径
    public static final String KEY_INCREMENT_UPDATE_DOWNLOAD_FULLPACK_ID = "increment_update_download_fullpack_id";//增量升级下载全量包的id
    public static final String KEY_INCREMENT_UPDATE_DOWNLOAD_PATCH_ID = "increment_update_download_patch_id";//增量升级下载增量包的id
    public static final String KEY_INCREMENT_UPDATE_DOWNLOAD_STATUS = "increment_update_download_status";//增量升级下载的状态

	// ===========================================================
	// Fields
	// ===========================================================
    private static IncrementUpdatePreference sInstance;

	// ===========================================================
	// Constructors
	// ===========================================================
	private IncrementUpdatePreference() {
	}
	
	public static IncrementUpdatePreference getInstance(){
		if(sInstance == null){
			sInstance = new IncrementUpdatePreference();
		}
		
		return sInstance;
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

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

