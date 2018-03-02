package com.nqmobile.livesdk.modules.categoryfolder.features;

import com.nqmobile.livesdk.commons.moduleframework.AbsSwitchFeature;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;
import com.nqmobile.livesdk.modules.categoryfolder.CategoryFolderModule;
import com.nqmobile.livesdk.modules.categoryfolder.CategoryFolderPreference;

/**
 * 分类文件夹底部推荐开关
 * 
 * @author liujiancheng
 *
 */
public class CategoryFolderBottomSwitchFeature extends AbsSwitchFeature {

	// ===========================================================
	// Constants
	// ===========================================================
	private static final int FEATURE = 122;

    // ===========================================================
    // Fields
    // ===========================================================
    private CategoryFolderPreference mPreference;
	
    // ===========================================================
    // Constructors
    // ===========================================================
    public CategoryFolderBottomSwitchFeature() {
    	mPreference = CategoryFolderPreference.getInstance();
    }
    
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public int getFeatureId() {
		return FEATURE;
	}

	@Override
	protected IModule getModule() {
		return ModuleContainer.getInstance().getModuleByName(CategoryFolderModule.MODULE_NAME);
	}

	@Override
	public boolean isEnabled() {
		return mPreference.isBottomRecommendEnable();
	}
	
	@Override
	public void enableFeature() {
		mPreference.setBottomRecommendEnable(true);
	}
	
	@Override
	public void disableFeature() {
		mPreference.setBottomRecommendEnable(false);
	}
}
