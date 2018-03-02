package com.nqmobile.livesdk.modules.browserbandge;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.browserbandge.features.BrowserBandgeCheckUpdateFeature;
import com.nqmobile.livesdk.modules.browserbandge.features.BrowserBandgeSwitchFeature;

public class BrowserBandgeModule extends AbsModule {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String MODULE_NAME = "BrowserBandge";
	// ===========================================================
	// Fields
	// ===========================================================
	private List<IDataTable> mTables;
	private List<IFeature> features;
    private BrowserBandgePreference mPreference;

	// ===========================================================
	// Constructors
	// ===========================================================
	public BrowserBandgeModule() {
		features = new ArrayList<IFeature>();
		features.add(new BrowserBandgeSwitchFeature());
		features.add(new BrowserBandgeCheckUpdateFeature());
		
		mPreference = BrowserBandgePreference.getInstance();
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
	    mPreference.setBandgeEnable(enabled);
		BandgeManager mgr = BandgeManager.getInstance(getContext());
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
	}
	public void init() {
	   BandgeManager.getInstance(getContext()).init();
	}
    @Override
    public boolean canEnabled() {
        return mPreference.isBandgeEnable();
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
