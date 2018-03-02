package com.nqmobile.livesdk.commons.prefetch;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.commons.prefetch.features.PrefetchFeature;
import com.nqmobile.livesdk.commons.prefetch.table.PrefetchTable;

public class PrefetchModule extends AbsModule {
	public static final String MODULE_NAME = "Prefetch";
	private PrefetchPreference mPreference;
	private static final ILogger logger = LoggerFactory.getLogger(PrefetchModule.MODULE_NAME);
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	public PrefetchModule() {
		mPreference = PrefetchPreference.getInstance();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public boolean canEnabled() {
		return true; 
	}
	
    public void init(){
    	PrefetchManager mgr = PrefetchManager.getInstance(getContext());
    	mgr.init();
    }
    
	@Override
	protected void onEnabled(boolean enabled) {
		logger.d("onEnabled( "+enabled+" )");
//		mPreference.setPrefetchEnable(enabled);
		PrefetchManager mgr = PrefetchManager.getInstance(getContext());
//		if (enabled) {
			mgr.init();
//		} else {
//			mgr.onDisable();
//		}
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public List<IFeature> getFeatures() {
		List<IFeature> result = new ArrayList<IFeature>(1);
		result.add(new PrefetchFeature());

		return result;
	}

	@Override
	public List<IDataTable> getTables() {
		List<IDataTable> tables = new ArrayList<IDataTable>(1);
		tables.add(new PrefetchTable());
		return tables;
	}
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
