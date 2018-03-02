package com.nqmobile.livesdk.modules.push;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.points.PointsManager;
import com.nqmobile.livesdk.modules.push.features.PushFeature;
import com.nqmobile.livesdk.modules.push.table.PushCacheTable;

public class PushModule extends AbsModule {

	// ===========================================================
	// Constants
	// ===========================================================
	public static final String MODULE_NAME = "Push";
	// ===========================================================
	// Fields
	// ===========================================================
	private List<IDataTable> mTables;
	private List<IFeature> features;
    private PushPreference mPreference;

	// ===========================================================
	// Constructors
	// ===========================================================
	public PushModule() {
		mTables = new ArrayList<IDataTable>();
		mTables.add(new PushCacheTable());

		features = new ArrayList<IFeature>();
		features.add(new PushFeature());
		
		mPreference = PushPreference.getInstance();
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
	    mPreference.setPushEnable(enabled);
		AbsManager mgr = PushManager.getInstance();
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
	}
	public void init() {
		PushManager.getInstance().init();
	}
    @Override
    public boolean canEnabled() {
        return mPreference.isPushEnable();
    }
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
