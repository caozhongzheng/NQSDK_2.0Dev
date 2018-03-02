package com.nqmobile.livesdk.utils;

import com.nqmobile.livesdk.commons.preference.PreferenceService;
import com.nqmobile.livesdk.commons.preference.PreferenceServiceFactory;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;

/**
 *  配置管理类
 * @author Rainbow
 *
 */
public class PreferenceDataHelper {
	
	private static PreferenceDataHelper mInstance;
	
	private static final String KEY_PREFERENCE = "LiveSDKSettings";
	
	public static final String KEY_CURRENT_VERSION = "sdk_current_version";
	public static final String KEY_LAST_VERSION = "sdk_last_version";
	/**true:首次安装后启动过定期联网*/
	public static final String KEY_FIRST_INSTALL = "sdk_first_install";
    public static final String KEY_INIT_FINISH = "init_finish";
    public static final String KEY_UPLOAD_PACKAGE_FINISH = "upload_package_finish";
	public static final String KEY_UID = "uid";
	public static final String KEY_NEED_FORE_ACTIVE = "need_fore_active";
	public static final String KEY_FORE_ACTIVE_SUCC = "fore_active_succ";
	
	public static final String KEY_SCORE = "score";
	//public static final String KEY_CURRENT_THEME = "current_theme";
	//public static final String KEY_CURRENT_WALLPAPER = "current_wallpaper";
	public static final String KEY_CURRENT_LOCKER = "current_locker";

	public static final String KEY_LAST_CHECK_UPDATE ="last_check_update";
    public static final String KEY_LAST_UPLOAD_LOG = "last_upload_log";
	public static final String KEY_HAVE_UPDATE = "have_update";
	public static final String KEY_UPDATE_FILE_NAME = "update_file_name";
	public static final String KEY_DOWNLOAD_URL = "download_url";
	public static final String KEY_DOWNLOAD_FINISH = "download_finish";
	public static final String KEY_UPDATE_FILE_PATH = "update_file_path";
	public static final String KEY_DOWNLOAD_REFER = "download_refer";
	public static final String KEY_BACK_DOWNLOAD_REFER = "back_download_refer";
	public static final String KEY_UPDATE_SUBTITLE = "subtitle";
	//public static final String KEY_DAILY_DISPLAY_SEQ = "daily_display_seq";
	//public static final String KEY_DAILY_COUNT = "daily_count";
	public static final String KEY_SERVERREGION = "serverregion";

    public static final String KEY_START_STORE = "start_store";
    public static final String KEY_SDCARD_PATH = "sdcard_path";
    
    /**客户端是否需要联网更新游戏文件夹中广告资源开关*/
    public static final String KEY_LAST_REGULAR_UPDATE_TIME = "last_regular_update_time";
    
    public static final String KEY_LAST_INCREMENT_UPDATE_VERSION = "last_increment_update_version";//上一次增量升级的版本号
    public static final String KEY_LAST_INCREMENT_UPDATE_PROMPT_TIME = "last_increment_update_time";//上一次增量升级的弹框的时间 yyyy-MM-dd
    public static final String KEY_LAST_INCREMENT_UPDATE_PROMPT_COUNT = "last_increment_update_count";//上一次增量升级的弹框的次数
    public static final String KEY_INCREMENT_UPDATE_DOWNLOAD_FULLPACK_PATH = "increment_update_download_fullpack_path";//增量升级下载全量包的路径
    public static final String KEY_INCREMENT_UPDATE_DOWNLOAD_FULLPACK_ID = "increment_update_download_fullpack_id";//增量升级下载全量包的id
    public static final String KEY_INCREMENT_UPDATE_DOWNLOAD_PATCH_ID = "increment_update_download_patch_id";//增量升级下载增量包的id
    public static final String KEY_INCREMENT_UPDATE_DOWNLOAD_STATUS = "increment_update_download_status";//增量升级下载的状态
    public static final String KEY_MAX_ASSOCIATION_SHOWTIMES = "association_max_show_times";//关联推荐最大展示次数
    public static final String KEY_MAX_ASSOCIATION_SHOW_INTERVAL = "association_show_interval";//关联推荐展示间隔天数
    public static final String KEY_MAX_ASSOCIATION_LAST_VERSIONTAG = "association_last_version_tag";//关联推荐展示上一次服务器下发的时间戳
    public static final String KEY_ASSOCIATION_LAST_SHOWTIME = "association_last_show_time";//关联推荐展示上一次展示间戳
    public static final String KEY_MUST_INSTALL_TIP_SHOW = "must_install_tip_show"; //进入桌面装机必备通知栏提示是否显示过
    public static final String KEY_MUST_INSTALL_TIP_INSTALLAPP_SHOW = "must_Install_tip_installapp_show"; //安装新应用装机必备通知栏提示是否显示过
    public static final String KEY_MUSI_INSTALL_ICON_CREATE = "must_install_icon_create"; //装机必备快捷方法是否创建过
    public static final String KEY_MUST_INSTALL_ENTERED = "must_install_entered"; //是否进入过装机必备页面

//    public static final String KEY_LAST_GET_PUSH_TIME = "last_get_push_time";
	
    public static final String KEY_APP_ENABLE = "app_enable";
    public static final String KEY_THEME_ENABLE = "theme_enable";
    public static final String KEY_WALLPAPER_ENABLE = "wallpaper_enable";
    public static final String KEY_LOCKER_ENABLE = "locker_enable";
    public static final String KEY_BANDGE_ENABLE = "bandge_enable";
    public static final String KEY_BANDGE_HAS_UPDATE = "bandge_has_update";
    public static final String KEY_HAS_NEW_VERSION = "has_new_version";
    public static final String KEY_ASSOCIATION_ENABLE = "association_enable";
	public static final String KEY_HAVE_ASSOCIATION = "have_association";
	
    public static final String KEY_MUST_INSTALL_ENABLE = "must_install_enable"; //装机必备开关
//	public static final String KEY_PUSH_ENABLE = "push_enable";// 服务器下发的是否允许推送开关
//	public static final String KEY_PUSH_FREQ_WIFI = "push_freq_wifi";// Push Notification wifi下联网频率，单位分钟
//    public static final String KEY_PUSH_FREQ_3G = "push_freq_3g";// Push Notification 非wifi下联网频率，单位分钟
 
    public static final String KEY_WEATHER_FREQ_WIFI = "weather_freq_wifi";// 天气，wifi下联网频率，单位分钟
    public static final String KEY_WEATHER_FREQ_3G = "weather_freq_3g";// 天气，非wifi下联网频率，单位分钟
    public static final String KEY_REGULAR_UPDATE_FREQ_WIFI = "regular_update_freq_wifi";// 定期联网接口，wifi下联网频率，单位分钟
    public static final String KEY_REGULAR_UPDATE_FREQ_3G = "regular_update_freq_3g";// 定期联网，3g下联网频率，单位分钟
    
    public static final String KEY_ENGINE_LIB_LD_FILE_NAME = "engine_lib_ld_file_name";// 灵动锁屏引擎库本地FILE_NAME
    public static final String KEY_ENGINE_LIB_LD_FILE = "engine_lib_ld_file";// 灵动锁屏引擎库
    public static final String KEY_ENGINE_LIB_LD_VERSION = "engine_lib_ld_ver";// 灵动锁屏引擎库本地版本号
    public static final String KEY_ENGINE_LIB_LD_SERVER_VERSION = "engine_lib_ld_ser_ver";// 灵动锁屏引擎库服务器版本号
    public static final String KEY_ENGINE_LIB_LD_SERVER_URL = "engine_lib_ld_ser_url";// 灵动锁屏引擎库服务器URL
    public static final String KEY_ENGINE_LIB_LD_SERVER_MD5 = "engine_lib_ld_ser_md5";// 灵动锁屏引擎库服务器MD5
    public static final String KEY_ENGINE_LIB_LD_SERVER_SIZE = "engine_lib_ld_ser_size";// 灵动锁屏引擎库服务器SIZE
    public static final String KEY_ENGINE_LIB_LD_SERVER_WIFI = "engine_lib_ld_ser_need_wifi";// 灵动锁屏引擎库服务器NET是否是wifi下才下载
    
    public static final String KEY_ENGINE_LIB_LF_FILE_NAME = "engine_lib_lf_file_name";// 拉风锁屏引擎库本地FILE_NAME
    public static final String KEY_ENGINE_LIB_LF_FILE = "engine_lib_lf_file";// 拉风锁屏引擎库
    public static final String KEY_ENGINE_LIB_LF_VERSION = "engine_lib_lf_ver";// 拉风锁屏引擎库本地版本号
    public static final String KEY_ENGINE_LIB_LF_SERVER_VERSION = "engine_lib_lf_ser_ver";// 拉风锁屏引擎库服务器版本号
    public static final String KEY_ENGINE_LIB_LF_SERVER_URL = "engine_lib_lf_ser_url";// 拉风锁屏引擎库服务器URL
    public static final String KEY_ENGINE_LIB_LF_SERVER_MD5 = "engine_lib_lf_ser_md5";// 拉风锁屏引擎库服务器MD5
    public static final String KEY_ENGINE_LIB_LF_SERVER_SIZE = "engine_lib_lf_ser_size";// 拉风锁屏引擎库服务器SIZE
    public static final String KEY_ENGINE_LIB_LF_SERVER_WIFI = "engine_lib_lf_ser_need_wifi";// 拉风锁屏引擎库服务器NET是否是wifi下才下载
    
    public static final String KEY_APP_LIB_FILE = "app_lib_file";// 本地预置库
    public static final String KEY_APP_LIB_VERSION = "app_lib_ver";// 应用类别预置库本地版本号
    public static final String KEY_APP_LIB_SERVER_VERSION = "app_lib_ser_ver";// 应用类别预置库服务器版本号
    public static final String KEY_APP_LIB_SERVER_URL = "app_lib_ser_url";// 应用类别预置库服务器URL
    public static final String KEY_APP_LIB_SERVER_MD5 = "app_lib_ser_md5";// 应用类别预置库服务器MD5
    public static final String KEY_APP_LIB_FILE_NAME = "app_lib_file_name";// 应用类别预置库本地FILE_NAME
    public static final String KEY_APP_LIB_START = "app_lib_down_start";// 应用类别预置库服务器资源下载处理中
    
    /**服务器下发的是否客户端允许创建游戏文件夹开关*/
    public static final String KEY_GAME_FOLDER_ENABLE = "game_folder_enable";
    /**客户端游戏文件夹状态 0:未创建 1:已创建 2:已删除*/
    public static final String KEY_GAME_FOLDER_STATUS = "game_folder_status";
    
    public static final String KEY_GAME_FREQ_WIFI = "game_freq_wifi";// 游戏文件夹，wifi下联网频率，单位分钟
    public static final String KEY_GAME_FREQ_3G = "game_freq_3g";// 游戏文件夹，非wifi下联网频率，单位分钟
    
    public static final String KEY_NEWGAME_SHOWNUM = "newgame_shownum";// 全部推荐游戏展示次数配置，单位次数
    public static final String KEY_NEWGAME_CACHEVALID = "newgame_cachevalid";// 游戏文件夹缓存时间配置，单位分钟

    
    /** 定期联网频率配置文件版本 */
    public static final String KEY_REGULARUPD_VERSION = "regular_update_ver";// 定期联网频率配置文件版本
    /** 锁屏SDK功能是否开启 */
    public static final String KEY_LOCKER_SDK_ENABLE = "locker_sdk_enable";
    /** 拉风锁屏引擎版本 */
    public static final String KEY_LOCKER_ENGINE_LF = "lafengVersion";
    /** 灵动锁屏引擎版本 */
    public static final String KEY_LOCKER_ENGINE_LD = "lingdongVersion";

    public static final String KEY_APP_STUB_ENABLE = "app_stub_enable";//虚框应用开关
    public static final String KEY_APP_STUB_HAS_UPDATE = "app_stub_has_update";//虚框应用是否有更新
    
    public static final String KEY_STORE_ENTRY_ENABLE = "store_entry_enable";//Store入口开关
    public static final String KEY_STORE_ENTRY_CREATED = "store_entry_created";//Store入口创建过
    /**虚框文件夹开关*/
    public static final String KEY_STUB_FOLDER_ENABLE = "stub_folder_enable";//虚框文件夹开关
    public static final String KEY_STUB_FOLDER_SHOW_COUNT = "stub_folder_show_count";//虚框文件夹内应用展示次数
    public static final String KEY_STUB_FOLDER_OPRATABLE = "stub_folder_opratable";//虚框文件夹能否编辑名字，内部资源顺序等
    public static final String KEY_STUB_FOLDER_DELETABLE = "stub_folder_deletable";//虚框文件夹以及内部资源能否被删除
    public static final String KEY_STUB_FOLDER_FREQ_WIFI = "stub_folder_freq_wifi";
    public static final String KEY_STUB_FOLDER_FREQ_3G = "stub_folder_freq_3g";
    public static final String KEY_LAST_GET_STUB_FOLDER_TIME = "last_get_stub_folder_time";
    public static final String KEY_LAST_PRELOAD_STUB_FOLDER_TIME = "last_preload_stub_folder_time";//虚框文件夹ICON前回获取时间
    public static final String KEY_LAST_PRELOAD_STUB_FOLDER_APP_TIME = "last_preload_stub_folder_app_time";//虚框文件夹内APP的ICON前回获取时间
    public static final String KEY_STUB_FOLDER_DELETE_FOLDER = "stub_folder_delete_folderIDs";//虚框文件夹被删除的文件ID
    public static final String KEY_STUB_FOLDER_CREATE_FOLDER = "stub_folder_create_folderIDs";//虚框文件夹已创建的文件ID
	/** long类型 */
	//public static final String[] KEY_APP_LIST_CACHE_TIME = {"store_app_list_cache_time0", "store_app_list_cache_time1", "store_app_list_cache_time2"};
	/** long类型 */
	//public static final String[] KEY_THEME_LIST_CACHE_TIME = {"store_theme_list_cache_time0", "store_theme_list_cache_time1"};
	/** long类型 */
	//public static final String[] KEY_WALLPAPER_LIST_CACHE_TIME = {"store_wallpaper_list_cache_time0", "store_wallpaper_list_cache_time1"};
	/** long类型 */
	//public static final String KEY_BANNER_LIST_CACHE_TIME = "store_banner_list_cache_time";
	/** long类型 */
	//public static final String KEY_DAILY_LIST_CACHE_TIME = "store_daily_list_cache_time";
	/** long类型 */
	public static final String[] KEY_LOCKER_LIST_CACHE_TIME = {"store_locker_list_cache_time0", "store_locker_list_cache_time1"};

    public static final String KEY_DOWNLOAD_AVAILABLE = "key_download_available";
    public static final String KEY_DOWNLOAD_AVAILABLE_VALUE1 = "key_download_value1";
    public static final String KEY_DOWNLOAD_AVAILABLE_VALUE2 = "key_download_value2";
    
    private Context mContext;
	private PreferenceService mSettings;
	
	private PreferenceDataHelper(Context context){
        mContext = context;
    	mSettings = PreferenceServiceFactory.getService(PreferenceServiceFactory.LIVE_SDK_SETTINGS_PREFERENCE);
	}

	public static PreferenceDataHelper getInstance(Context context){
		if(mInstance == null){
			mInstance = new PreferenceDataHelper(context);
		}
		
		return mInstance;
	}
	
	public String getStringValue(String key){
		return mSettings.getStringValue(key);
	}
	
	public void setStringValue(String key,String value){
		if(StringUtil.isNullOrEmpty(key))
			return;
		mSettings.setStringValue(key, value);
	}
	
	public boolean getBooleanValue(String key){
		return mSettings.getBooleanValue(key);
	}
	
	public void setBooleanValue(String key,boolean value){
		if(StringUtil.isNullOrEmpty(key))
			return;
		mSettings.setBooleanValue(key, value);;
	}
	
	public long getLongValue(String key){
		return mSettings.getLongValue(key);
	}
	
	public void setLongValue(String key,long value){
		if(StringUtil.isNullOrEmpty(key))
			return;
		mSettings.setLongValue(key, value);
	}
	
	public int getIntValue(String key){
		return mSettings.getIntValue(key);
	}
	
	public void setIntValue(String key,int value){
		if(StringUtil.isNullOrEmpty(key))
			return;
		mSettings.setIntValue(key, value);
	}
}
