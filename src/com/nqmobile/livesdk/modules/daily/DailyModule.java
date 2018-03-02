package com.nqmobile.livesdk.modules.daily;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.browserbandge.BandgeManager;
import com.nqmobile.livesdk.modules.daily.table.DailyCacheTable;

public class DailyModule extends AbsModule {

	// ===========================================================
	// Constants
	// ===========================================================
	public static final String MODULE_NAME = "Daily";
	// ===========================================================
	// Fields
	// ===========================================================
	private List<IDataTable> mTables;
	// ===========================================================
	// Constructors
	// ===========================================================
	public DailyModule(){
		mTables = new ArrayList<IDataTable>();
		mTables.add(new DailyCacheTable());
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
		return null;
	}

	@Override
	public List<IDataTable> getTables() {
		return mTables;
	}

	@Override
	protected void onEnabled(boolean enabled) {
		DailyManager mgr = DailyManager.getInstance(getContext());
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
	}

	public void init() {
		DailyManager.getInstance(getContext()).init();
	}
    @Override
    public boolean canEnabled() {
        return true;
    }
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
