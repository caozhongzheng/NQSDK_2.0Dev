package com.nqmobile.livesdk.modules.feedback;

import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.daily.DailyManager;

public class FeedbackModule extends AbsModule {

	// ===========================================================
	// Constants
	// ===========================================================
	public static final String MODULE_NAME = "Feedback";
	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================
	public FeedbackModule() {
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
		return null;
	}

	@Override
	protected void onEnabled(boolean enabled) {
		FeedBackManager mgr = FeedBackManager.getInstance();
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
	}
	public void init() {
		FeedBackManager.getInstance().init();
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
