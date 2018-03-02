package com.nqmobile.livesdk.modules.appstub;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.appactive.AppActiveManager;
import com.nqmobile.livesdk.modules.appstub.features.AppStubHasUpdateFeature;
import com.nqmobile.livesdk.modules.appstub.features.AppStubSwicthFeature;
import com.nqmobile.livesdk.modules.appstub.table.AppStubCacheTable;

public class AppStubModule extends AbsModule {

	// ===========================================================
	// Constants
	// ===========================================================
	public static final String MODULE_NAME = "AppStub";
	// ===========================================================
	// Fields
	// ===========================================================
	private List<IDataTable> mTables;
	private List<IFeature> features;
    private AppStubPreference mPreference;

	// ===========================================================
	// Constructors
	// ===========================================================
	public AppStubModule() {
		mTables = new ArrayList<IDataTable>();
		mTables.add(new AppStubCacheTable());

		features = new ArrayList<IFeature>();
		features.add(new AppStubSwicthFeature());
		features.add(new AppStubHasUpdateFeature());
		
		mPreference = AppStubPreference.getInstance();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
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

	@Override
	protected void onEnabled(boolean enabled) {
	    mPreference.setAppStubEnable(enabled);
		AppStubManager mgr = AppStubManager.getInstance(getContext());
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
	}

	public void init() {
		AbsManager mgr = AppStubManager.getInstance(getContext());
		mgr.init();
	}
    @Override
    public boolean canEnabled() {
        return mPreference.isAppStubEnable();
    }

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
