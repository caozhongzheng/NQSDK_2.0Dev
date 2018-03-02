package com.nqmobile.livesdk.modules.storeentry;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;

public class StoreEntryModule extends AbsModule {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String MODULE_NAME = "StoreEntry";
	// ===========================================================
	// Fields
	// ===========================================================
	private List<IDataTable> mTables;
	private List<IFeature> features;
    private StoreEntryPreference mPreference;

	// ===========================================================
	// Constructors
	// ===========================================================
	public StoreEntryModule() {
		features = new ArrayList<IFeature>();
		features.add(new StoreEntryFeature());
		
		mPreference = StoreEntryPreference.getInstance();
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
	protected void onEnabled(boolean enabled) {
	    mPreference.setStoreEntryEnable(enabled);
	}
	public void init() {}
    @Override
    public boolean canEnabled() {
        return mPreference.isStoreEntryEnable();
    }

	@Override
	public String getLogTag() {
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
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
