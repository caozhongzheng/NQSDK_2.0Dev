package com.nqmobile.livesdk.modules.categoryfolder.features;

import com.nqmobile.livesdk.commons.moduleframework.AbsSwitchFeature;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;
import com.nqmobile.livesdk.modules.categoryfolder.CategoryFolderModule;
import com.nqmobile.livesdk.modules.categoryfolder.CategoryFolderPreference;

/**
 * 分类文件夹虚框开关
 * 
 * @author liujiancheng
 *
 */
public class CategoryFolderAppStubSwitchFeature extends AbsSwitchFeature {

	// ===========================================================
	// Constants
	// ===========================================================
	private static final int FEATURE = 121;
	
    // ===========================================================
    // Fields
    // ===========================================================
    private CategoryFolderPreference mPreference;
    
    // ===========================================================
    // Constructors
    // ===========================================================
    public CategoryFolderAppStubSwitchFeature() {
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
		return mPreference.isAppStubEnable();
	}
	
	@Override
	public void enableFeature() {
		mPreference.setAppStubEnable(true);
	}
	
	@Override
	public void disableFeature() {
		mPreference.setAppStubEnable(false);		
	}
}
