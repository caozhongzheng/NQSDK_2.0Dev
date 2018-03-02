package com.nqmobile.livesdk.modules.appstubfolder;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.appstub.AppStubManager;
import com.nqmobile.livesdk.modules.appstubfolder.features.AppStubFolderSwitchFeature;
import com.nqmobile.livesdk.modules.appstubfolder.table.AppStubFolderAppCacheTable;
import com.nqmobile.livesdk.modules.appstubfolder.table.AppStubFolderCacheTable;
import com.nqmobile.livesdk.modules.appstubfolder.table.AppStubFolderResTable;

public class AppStubFolderModule extends AbsModule {

	// ===========================================================
	// Constants
	// ===========================================================
	public static final String MODULE_NAME = "AppStubFolder";
	// ===========================================================
	// Fields
	// ===========================================================
	private List<IDataTable> mTables;
	private List<IFeature> features;
    private AppStubFolderPreference mPreference;

	// ===========================================================
	// Constructors
	// ===========================================================
	public AppStubFolderModule() {
		mTables = new ArrayList<IDataTable>();
		mTables.add(new AppStubFolderAppCacheTable());
		mTables.add(new AppStubFolderCacheTable());
		mTables.add(new AppStubFolderResTable());

		features = new ArrayList<IFeature>();
		features.add(new AppStubFolderSwitchFeature());
		
		mPreference = AppStubFolderPreference.getInstance();
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
        mPreference.setStubFolderEnable(enabled);
		AppStubFolderManager mgr = AppStubFolderManager.getInstance(getContext());
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
	}
	public void init() {
		AbsManager mgr = AppStubFolderManager.getInstance(getContext());
		mgr.init();
	}
    @Override
    public boolean canEnabled() {
        return mPreference.isStubFolderEnable();
    }

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
