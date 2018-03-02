package com.nqmobile.livesdk.modules.categoryfolder;

import android.text.format.DateUtils;
import android.util.Log;
import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.log.NqLog;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.categoryfolder.features.CategoryFolderAppStubSwitchFeature;
import com.nqmobile.livesdk.modules.categoryfolder.features.CategoryFolderBottomSwitchFeature;
import com.nqmobile.livesdk.modules.categoryfolder.features.CategoryFolderGetMoreSwitchFeature;
import com.nqmobile.livesdk.modules.categoryfolder.features.CategoryFolderRecentInstallSwitchFeature;
import com.nqmobile.livesdk.modules.categoryfolder.table.RecommendAppTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rainbow on 2014/12/11.
 */
public class CategoryFolderModule extends AbsModule{

    // ===========================================================
    // Constants
    // ===========================================================

    public static final String MODULE_NAME = "CategoryFolder";

    // ===========================================================
    // Fields
    // ===========================================================

    private List<IDataTable> mTables;
    private List<IFeature> mFeatures;
    private CategoryFolderPreference mPreference;
    // ===========================================================
    // Constructors
    // ===========================================================

    public CategoryFolderModule(){
    	mPreference = CategoryFolderPreference.getInstance();
        mTables = new ArrayList<IDataTable>();
        mTables.add(new RecommendAppTable());
        
		mFeatures = new ArrayList<IFeature>();
		mFeatures.add(new CategoryFolderAppStubSwitchFeature());
		mFeatures.add(new CategoryFolderBottomSwitchFeature());		
		mFeatures.add(new CategoryFolderGetMoreSwitchFeature());
		mFeatures.add(new CategoryFolderRecentInstallSwitchFeature());
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    protected void onEnabled(boolean enabled) {
    	mPreference.setCategoryFolderEnable(enabled);
		CategoryFolderManager mgr = CategoryFolderManager.getInstance();
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
    }
	@Override
	public void init() {
		CategoryFolderManager mgr = CategoryFolderManager.getInstance();
		mgr.init();
	}
    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public boolean canEnabled() {
        return mPreference.isCategoryFolderEnable();
    }

    @Override
    public List<IFeature> getFeatures() {
        return mFeatures;
    }

    @Override
    public List<IDataTable> getTables() {
        return mTables;
    }

    @Override
    public void onAppFirstInit(boolean enabled) {
        Log.i("CategoryFolder","onAppFirstInit!");
        //设置最后一次获取资源的时间为昨天+ 半小时 这样半小时以后就会去获取资源
        long time = System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS + DateUtils.MINUTE_IN_MILLIS * 30;
        mPreference.setLongValue(CategoryFolderPreference.KEY_LAST_GET_RECOMMEND_APP_TIME,time);
    }


    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
