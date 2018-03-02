package com.nqmobile.livesdk.modules.stat;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

/**
 * Created by Rainbow on 2014/11/22.
 */
public class StatPreference extends SettingsPreference{

    // ===========================================================
    // Constants
    // ===========================================================

    private static final String KEY_UPLOAD_PACKAGE_FINISH = "upload_package_finish";
    private static final String KEY_LAST_UPLOAD_LOG = "last_upload_log";

    private static final String KEY_ACTION_LOG_ENABLE = "action_log_enable";//行为日志开关
    private static final String KEY_PACKAGE_LOG_ENABLE = "package_log_enable";//安装包信息上传开关
    // ===========================================================
    // Fields
    // ===========================================================

    private static StatPreference sInstance;

    // ===========================================================
    // Constructors
    // ===========================================================

    private StatPreference() {
    }

    public static StatPreference getInstance(){
        if(sInstance == null){
            sInstance = new StatPreference();
        }

        return sInstance;
    }


    // ===========================================================
    // Getter & Setter
    // ===========================================================
	public boolean getUploadPackageFinish(){
	    return getBooleanValue(KEY_UPLOAD_PACKAGE_FINISH);
	}
	public void setUploadPackageFinish(boolean finish){
        setBooleanValue(KEY_UPLOAD_PACKAGE_FINISH, finish);
    }
	
	public long getLastUploadLog(){
	    return getLongValue(KEY_LAST_UPLOAD_LOG);
	}
	public void setLastUploadLog(long time){
        setLongValue(KEY_LAST_UPLOAD_LOG, time);
    }
	
	public boolean isUploadLogEnabled(){
	    return getBooleanValue(KEY_ACTION_LOG_ENABLE);
	}
	public void setUploadLogEnabled(boolean value){
        setBooleanValue(KEY_ACTION_LOG_ENABLE, value);
    }
	
	public boolean isUploadPackageEnabled(){
	    return getBooleanValue(KEY_PACKAGE_LOG_ENABLE);
	}
	public void setUploadPackageEnabled(boolean value){
        setBooleanValue(KEY_PACKAGE_LOG_ENABLE, value);
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
