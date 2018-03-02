package com.nqmobile.livesdk.modules.gamefolder_v2;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class GameFolderV2Preference extends SettingsPreference {
	// ===========================================================
	// Constants
	// ===========================================================
    /**服务器下发的是否客户端允许创建游戏文件夹开关*/
    public static final String KEY_GAME_FOLDER_ENABLE = "game_folder_enable";
    /**客户端游戏文件夹状态 0:未创建 1:已创建 2:已删除*/
    public static final String KEY_GAME_FOLDER_STATUS = "game_folder_status";
    
    public static final String KEY_GAME_FREQ_WIFI = "game_freq_wifi";// 游戏文件夹，wifi下联网频率，单位分钟
    public static final String KEY_GAME_FREQ_3G = "game_freq_3g";// 游戏文件夹，非wifi下联网频率，单位分钟
    
    /**上次游戏文件夹广告更新成功时间 */
    private static final String KEY_LAST_GET_GAME_AD_TIME = "last_get_game_ad_time";
    /**上次游戏文件夹广告预加载开始时间 */
    public static final String KEY_LAST_PRELOAD_GAME_TIME = "last_preload_game_time";
    /**上次游戏文件夹广告显示顺序 */
    public static final String KEY_LAST_GAME_AD_SHOW_POS = "last_game_ad_show_pos";
    
    private static final String KEY_APP_LIB_VERSION = "app_lib_ver";// 应用类别预置库本地版本号
    private static final String KEY_APP_LIB_SERVER_VERSION = "app_lib_ser_ver";// 应用类别预置库服务器版本号
    private static final String KEY_APP_LIB_SERVER_URL = "app_lib_ser_url";// 应用类别预置库服务器URL
    private static final String KEY_APP_LIB_SERVER_MD5 = "app_lib_ser_md5";// 应用类别预置库服务器MD5
    public static final String KEY_APP_LIB_START = "app_lib_down_start";// 应用类别预置库服务器资源下载处理中
    
    public static final String KEY_NEWGAME_SHOWNUM = "newgame_shownum";// 全部推荐游戏展示次数配置，单位次数
    public static final String KEY_NEWGAME_CACHEVALID = "newgame_cachevalid";// 游戏文件夹缓存时间配置，单位分钟
	// ===========================================================
	// Fields
	// ===========================================================
    private static GameFolderV2Preference sInstance = new GameFolderV2Preference();

	// ===========================================================
	// Constructors
	// ===========================================================
	private GameFolderV2Preference() {
	}
	
	public static GameFolderV2Preference getInstance(){
		return sInstance;
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================
    public int getGameFolderStatus() {
        return getIntValue(KEY_GAME_FOLDER_STATUS);
    }

    public void setGameFolderStatus(int type) {
        setIntValue(KEY_GAME_FOLDER_STATUS, type);
    }
    
    public boolean isGameFolderEnabled() {
        return getBooleanValue(KEY_GAME_FOLDER_ENABLE);
    }

    public void setGameFolderEnabled(boolean enabled) {
        setBooleanValue(KEY_GAME_FOLDER_ENABLE, enabled);
    }
    
    public String getAppLibVersion() {
        return getStringValue(KEY_APP_LIB_VERSION);
    }

    public void setAppLibVersion(String version) {
        setStringValue(KEY_APP_LIB_VERSION, version);
    }
    
    public String getAppLibServerVersion() {
        return getStringValue(KEY_APP_LIB_SERVER_VERSION);
    }

    public void setAppLibServerVersion(String version) {
        setStringValue(KEY_APP_LIB_SERVER_VERSION, version);
    }
    
    public String getAppLibServerUrl() {
        return getStringValue(KEY_APP_LIB_SERVER_URL);
    }

    public void setAppLibServerUrl(String url) {
        setStringValue(KEY_APP_LIB_SERVER_URL, url);
    }
    
    public String getAppLibServerMd5() {
        return getStringValue(KEY_APP_LIB_SERVER_MD5);
    }

    public void setAppLibServerMd5(String url) {
        setStringValue(KEY_APP_LIB_SERVER_MD5, url);
    }
    
    public long getLastGetGameAdTime() {
        return getLongValue(KEY_LAST_GET_GAME_AD_TIME);
    }

    public void setLastGetGameAdTime(long time) {
        setLongValue(KEY_LAST_GET_GAME_AD_TIME, time);
    }
    
    public long getGameFreqWifiInMinutes() {
        return getLongValue(KEY_GAME_FREQ_WIFI);
    }

    public void setGameFreqWifiInMinutes(long time) {
        setLongValue(KEY_GAME_FREQ_WIFI, time);
    }
    
    public long getGameFreq3GInMinutes() {
        return getLongValue(KEY_GAME_FREQ_3G);
    }

    public void setGameFreq3GInMinutes(long time) {
        setLongValue(KEY_GAME_FREQ_3G, time);
    }
    
    public long getGameShowNum() {
        return getLongValue(KEY_NEWGAME_SHOWNUM);
    }
    public void setGameShowNum(long num) {
        setLongValue(KEY_NEWGAME_SHOWNUM, num);
    }
    
    public long getGameCacheValidTimeInMinutes() {
        return getLongValue(KEY_NEWGAME_CACHEVALID);
    }
    public void setGameCacheValidTimeInMinutes(long num) {
        setLongValue(KEY_NEWGAME_CACHEVALID, num);
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
