package com.nqmobile.livesdk.modules.regularupdate;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.push.PushManager;
import com.nqmobile.livesdk.modules.regularupdate.features.RegularupdateUpdateFeature;

public class RegularUpdateModule extends AbsModule {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String MODULE_NAME = "RegularUpdate";
	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================
	private List<IFeature> features;

	public RegularUpdateModule() {
		features = new ArrayList<IFeature>(5);
		features.add(new RegularupdateUpdateFeature());
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
		AbsManager mgr = RegularUpdateManager.getInstance();
		if (enabled) {
			mgr.init();
		} else {
			// do nothing. won't disable regularUpdate
		}
	}
	public void init() {
		RegularUpdateManager.getInstance().init();
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
		return features;
	}

	@Override
	public List<IDataTable> getTables() {
		return null;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
