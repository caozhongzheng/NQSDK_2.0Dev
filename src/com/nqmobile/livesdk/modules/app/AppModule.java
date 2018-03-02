package com.nqmobile.livesdk.modules.app;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.app.features.AppSwitchFeature;
import com.nqmobile.livesdk.modules.app.table.AppCacheTable;
import com.nqmobile.livesdk.modules.app.table.AppLocalTable;

public class AppModule extends AbsModule {

	// ===========================================================
	// Constants
	// ===========================================================
	public static final String MODULE_NAME = "App";
	// ===========================================================
	// Fields
	// ===========================================================
	private List<IDataTable> mTables;
	private List<IFeature> features;
    private AppPreference mPreference;
	// ===========================================================
	// Constructors
	// ===========================================================
	public AppModule() {
		mTables = new ArrayList<IDataTable>();
		mTables.add(new AppCacheTable());
		mTables.add(new AppLocalTable());

		features = new ArrayList<IFeature>();
		features.add(new AppSwitchFeature());
		
		mPreference = AppPreference.getInstance();
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
    public boolean canEnabled() {
        return mPreference.isAppEnable();
    }

	@Override
	protected void onEnabled(boolean enabled) {
	    mPreference.setAppEnable(enabled);
		AppManager mgr = AppManager.getInstance(getContext());
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
	}

	public void init() {
		AbsManager mgr = AppManager.getInstance(getContext());
		mgr.init();
	}
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
