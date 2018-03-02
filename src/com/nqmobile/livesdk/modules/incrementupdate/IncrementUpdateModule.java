package com.nqmobile.livesdk.modules.incrementupdate;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.gamefolder_v2.GameManager;
import com.nqmobile.livesdk.modules.incrementupdate.features.NewVersionCheckUpdateFeature;
import com.nqmobile.livesdk.modules.incrementupdate.table.NewVersionTable;

public class IncrementUpdateModule extends AbsModule {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String MODULE_NAME = "IncrementUpdate";
	// ===========================================================
	// Fields
	// ===========================================================
	private List<IDataTable> mTables;
	private List<IFeature> mFeatures;
	// ===========================================================
	// Constructors
	// ===========================================================
	public IncrementUpdateModule() {
		mTables = new ArrayList<IDataTable>();
		mTables.add(new NewVersionTable());
		
		mFeatures = new ArrayList<IFeature>();
		mFeatures.add(new NewVersionCheckUpdateFeature());
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
		IncrementUpdateManager mgr = IncrementUpdateManager
				.getInstance(getContext());
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
	}
	public void init() {
		IncrementUpdateManager.getInstance(getContext()).init();
	}
    @Override
    public boolean canEnabled() {
        return true;
    }

	@Override
	public String getLogTag() {
		return MODULE_NAME;
	}

	@Override
	public List<IFeature> getFeatures() {
		return mFeatures;
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
