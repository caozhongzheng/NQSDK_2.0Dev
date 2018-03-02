package com.nqmobile.livesdk.modules.search;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.search.features.SearcherDateUpdateFeature;

public class SearcherModule extends AbsModule {

	// ===========================================================
	// Constants
	// ===========================================================
	public static final String MODULE_NAME = "Searcher";
	// ===========================================================
	// Fields
	// ===========================================================
	private List<IDataTable> mTables;
	private List<IFeature> features;

	// ===========================================================
	// Constructors
	// ===========================================================
	public SearcherModule() {

		mTables = new ArrayList<IDataTable>();

		features = new ArrayList<IFeature>();
		features.add(new SearcherDateUpdateFeature());
		
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
		AbsManager mgr = SearcherManager.getInstance(getContext());
		mgr.init();
//		if (enabled) {
//		} else {
//			mgr.onDisable();
//		}
	}
	public void init() {
		SearcherManager.getInstance(getContext()).init();
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
