package com.nqmobile.livesdk.modules.update;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

/**
 * Created by Rainbow on 2014/11/20.
 */
public class UpdatePreference extends SettingsPreference{

    // ===========================================================
    // Constants
    // ===========================================================

    public static final String KEY_LAST_CHECK_UPDATE ="last_check_update";
    public static final String KEY_HAVE_UPDATE = "have_update";
    public static final String KEY_UPDATE_FILE_NAME = "update_file_name";
    public static final String KEY_DOWNLOAD_URL = "download_url";
    public static final String KEY_DOWNLOAD_FINISH = "download_finish";
    public static final String KEY_UPDATE_FILE_PATH = "update_file_path";
    public static final String KEY_DOWNLOAD_REFER = "download_refer";
    public static final String KEY_BACK_DOWNLOAD_REFER = "back_download_refer";
    public static final String KEY_UPDATE_SUBTITLE = "subtitle";

    // ===========================================================
    // Fields
    // ===========================================================

    private static UpdatePreference sInstance;

    // ===========================================================
    // Constructors
    // ===========================================================

    private UpdatePreference() {
    }

    public static UpdatePreference getInstance(){
        if(sInstance == null){
            sInstance = new UpdatePreference();
        }

        return sInstance;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
	public long getLastCheckUpdate(){
	    return getLongValue(KEY_LAST_CHECK_UPDATE);
	}
	public void setLastCheckUpdate(long time){
        setLongValue(KEY_LAST_CHECK_UPDATE, time);
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
