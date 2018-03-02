package com.nqmobile.livesdk.commons.mydownloadmanager;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

/**
 * 
 * @author liujiancheng
 *
 */
public class MyDownloadPreference extends SettingsPreference{

    // ===========================================================
    // Constants
    // ===========================================================

    public static final String KEY_LAST_CHECK_DOWNLOAD ="last_check_download";
    public static final String KEY_MYDOWNLOAD_ENABLE = "mydownload_enable";

    // ===========================================================
    // Fields
    // ===========================================================

    private static MyDownloadPreference sInstance;

    // ===========================================================
    // Constructors
    // ===========================================================

    private MyDownloadPreference() {
    }

    public static MyDownloadPreference getInstance(){
        if(sInstance == null){
            sInstance = new MyDownloadPreference();
        }

        return sInstance;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
	public long getLastCheckDownload(){
	    return getLongValue(KEY_LAST_CHECK_DOWNLOAD);
	}
	
	public void setLastCheckDownload(long time){
        setLongValue(KEY_LAST_CHECK_DOWNLOAD, time);
    }
	
    public void setMyDownloadEnable(boolean enable){
        setBooleanValue(KEY_MYDOWNLOAD_ENABLE, enable);
    }
    
    public boolean isMyDownloadEnable(){
        return getBooleanValue(KEY_MYDOWNLOAD_ENABLE);
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
