package com.nqmobile.livesdk.modules.banner;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.association.AssociationManager;
import com.nqmobile.livesdk.modules.banner.table.BannerCacheTable;

public class BannerModule extends AbsModule {

	// ===========================================================
	// Constants
	// ===========================================================
	public static final String MODULE_NAME = "Banner";
	// ===========================================================
	// Fields
	// ===========================================================
	private List<IDataTable> mTables;

	// ===========================================================
	// Constructors
	// ===========================================================
	public BannerModule() {
		mTables = new ArrayList<IDataTable>();
		mTables.add(new BannerCacheTable());
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
		BannerManager mgr = BannerManager.getInstance(getContext());
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
	}
	public void init() {
		AbsManager mgr = BannerManager.getInstance(getContext());
		mgr.init();
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
