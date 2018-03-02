package com.nqmobile.livesdk.modules.categoryfolder;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

/**
 * Created by Rainbow on 2014/12/11.
 */
public class CategoryFolderPreference extends SettingsPreference {

    // ===========================================================
    // Constants
    // ===========================================================
    public static final String KEY_LAST_GET_RECOMMEND_APP_TIME = "last_get_recommend_time";

    public static final String KEY_EMPTY_FOLDER_APPSTUB_SIZE = "empty_folder_appstub_size";

    public static final String KEY_RECENT_FOLDER_SIZE = "recent_folder_size";
    
    public static final String KEY_CATEGORY_FOLDER_ENABLE = "category_folder_enable";
    
    public static final String KEY_APP_STUB_SWITCH = "categoryfolder_app_stub_switch";
    
    public static final String KEY_BOTTOM_RECOMMEND_SWITCH = "categoryfolder_bottom_recommend_switch";
    
    public static final String KEY_GET_MORE_SWITCH = "categoryfolder_get_more_switch";
    
    public static final String KEY_RECENT_INSTALL_SWITCH = "categoryfolder_recent_install_switch";

    // ===========================================================
    // Fields
    // ===========================================================

    private static CategoryFolderPreference sInstance;

    // ===========================================================
    // Constructors
    // ===========================================================

    private CategoryFolderPreference() {
    }

    public static CategoryFolderPreference getInstance(){
        if(sInstance == null){
            sInstance = new CategoryFolderPreference();
        }

        return sInstance;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================
    public void setCategoryFolderEnable(boolean enable){
        setBooleanValue(KEY_CATEGORY_FOLDER_ENABLE, enable);
    }
    
    public boolean isCategoryFolderEnable(){
        return getBooleanValue(KEY_CATEGORY_FOLDER_ENABLE);
    }
    
    public boolean isAppStubEnable(){
        return getBooleanValue(KEY_APP_STUB_SWITCH);
    }
    
    public void setAppStubEnable(boolean enable){
        setBooleanValue(KEY_APP_STUB_SWITCH, enable);
    }
    
    public boolean isBottomRecommendEnable(){
        return getBooleanValue(KEY_BOTTOM_RECOMMEND_SWITCH);
    }
    
    public void setBottomRecommendEnable(boolean enable){
        setBooleanValue(KEY_BOTTOM_RECOMMEND_SWITCH, enable);
    }
    
    public boolean isGetmoreEnable(){
        return getBooleanValue(KEY_GET_MORE_SWITCH);
    }
    
    public void setGetMoreEnable(boolean enable){
        setBooleanValue(KEY_GET_MORE_SWITCH, enable);
    }
    
    public boolean isRecentInstallEnable(){
        return getBooleanValue(KEY_RECENT_INSTALL_SWITCH);
    }
    
    public void setRecentInstallEnable(boolean enable){
        setBooleanValue(KEY_RECENT_INSTALL_SWITCH, enable);
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
