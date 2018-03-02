package com.nqmobile.livesdk.modules.categoryfolder;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.utils.NetworkUtils;

/**
 * Created by Rainbow on 2014/12/11.
 */
public class CategoryFolderConfig {

    // ===========================================================
    // Constants
    // ===========================================================

    public static final int RECOMMEND_STATE_DISABLE = 0;
    public static final int RECOMMEND_STATE_WIFI = 1;
    public static final int RECOMMEND_STATE_3G = 2;

    // ===========================================================
    // Fields
    // ===========================================================

    private CategoryFolderPreference mPreference;

    // ===========================================================
    // Constructors
    // ===========================================================

    public CategoryFolderConfig(){
        mPreference = CategoryFolderPreference.getInstance();
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

    public int supportRecommendApps(){
        boolean enable = mPreference.getBooleanValue(CategoryFolderPreference.KEY_BOTTOM_RECOMMEND_SWITCH);
        if(enable){
            if(NetworkUtils.isWifi(ApplicationContext.getContext())){
                return RECOMMEND_STATE_WIFI;
            }else{
                return RECOMMEND_STATE_3G;
            }
        }
        return RECOMMEND_STATE_DISABLE;
    }

    public boolean supportAppStub(){
        return mPreference.getBooleanValue(CategoryFolderPreference.KEY_APP_STUB_SWITCH);
    }

    public boolean supportGetMoreApps(){
        return mPreference.getBooleanValue(CategoryFolderPreference.KEY_GET_MORE_SWITCH);
    }

    public boolean supportRecentInstall(){
        return mPreference.getBooleanValue(CategoryFolderPreference.KEY_RECENT_INSTALL_SWITCH);
    }
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
