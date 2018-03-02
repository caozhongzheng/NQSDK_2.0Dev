/**
 * 
 */
package com.nqmobile.livesdk.modules.storeentry;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.moduleframework.AbsSwitchFeature;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;

/**
 * Store入口 开关
 * 
 * @author HouKangxi
 *
 */
public class StoreEntryFeature extends AbsSwitchFeature {

	// ===========================================================
	// Constants
	// ===========================================================
	private static final int FEATURE = 114;
	// ===========================================================
	// Fields
	// ===========================================================
	private StoreEntryPreference mHelper;

	// ===========================================================
	// Constructors
	// ===========================================================

	public StoreEntryFeature() {
		mHelper = StoreEntryPreference.getInstance();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public int getFeatureId() {
		return FEATURE;
	}

	@Override
	public int getStatus() {
		return 0;
	}

	@Override
	protected IModule getModule() {
		return ModuleContainer.getInstance().getModuleByName(
				StoreEntryModule.MODULE_NAME);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	@Override
	public void enableFeature() {
		super.enableFeature();
		boolean storeEntryCreated = mHelper
				.getBooleanValue(StoreEntryPreference.KEY_STORE_ENTRY_CREATED);
        if (!storeEntryCreated) {
			StoreEntry.getInstance(ApplicationContext.getContext())
					.createStoreEntry();
		}

        StoreEntry.getInstance(ApplicationContext.getContext()).createHotAppEntry();
	}
}
