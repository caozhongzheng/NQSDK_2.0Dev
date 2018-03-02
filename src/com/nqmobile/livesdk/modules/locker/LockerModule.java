/**
 * 
 */
package com.nqmobile.livesdk.modules.locker;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.installedrecommend.InstalledRecommendManager;
import com.nqmobile.livesdk.modules.locker.features.LockerLfEngineCheckUpdateFeature;
import com.nqmobile.livesdk.modules.locker.features.LockerOwnEngineCheckUpdateFeature;
import com.nqmobile.livesdk.modules.locker.features.LockerSwitchFeature;
import com.nqmobile.livesdk.modules.locker.tables.LockerCacheTable;
import com.nqmobile.livesdk.modules.locker.tables.LockerLocalTable;

/**
 * @author HouKangxi
 *
 */
public class LockerModule extends AbsModule {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String MODULE_NAME = "Locker";
	// ===========================================================
	// Fields
	// ===========================================================
	private List<IDataTable> mTables;
	private List<IFeature> features;
    private LockerPreference mPreference;

	// ===========================================================
	// Constructors
	// ===========================================================
	public LockerModule() {
		features = new ArrayList<IFeature>(3);
		features.add(new LockerLfEngineCheckUpdateFeature());
		features.add(new LockerOwnEngineCheckUpdateFeature());
		features.add(new LockerSwitchFeature());
		// 註冊Tables
		mTables = new ArrayList<IDataTable>();
		mTables.add(new LockerLocalTable());
		mTables.add(new LockerCacheTable());
		
		mPreference = LockerPreference.getInstance();
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public List<IFeature> getFeatures() {
		return features;
	}

	@Override
	public List<IDataTable> getTables() {
		return mTables;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	protected void onEnabled(boolean enabled) {
	    mPreference.setLockerEnable(enabled);
		LockerManager mgr = LockerManager.getInstance(getContext());
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
	}
	public void init() {
		LockerManager.getInstance(getContext()).init();
	}
    @Override
    public boolean canEnabled() {
        return mPreference.isLockerEnable();
    }
    
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
