package com.nqmobile.livesdk.modules.installedrecommend;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.gamefolder_v2.GameManager;
import com.nqmobile.livesdk.modules.incrementupdate.IncrementUpdateManager;
import com.nqmobile.livesdk.modules.installedrecommend.feature.InstalledRecommendSwitchFeature;


public class InstalledRecommendModule extends AbsModule {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(InstalledRecommendModule.MODULE_NAME);
	
	public static final String MODULE_NAME = "InstalledRecommend";
	// ===========================================================
	// Fields
	// ===========================================================
	private List<IFeature> mFeatures;
    private InstalledRecommendPreference mPreference;

	// ===========================================================
	// Constructors
	// ===========================================================
	public InstalledRecommendModule() {
		mFeatures = new ArrayList<IFeature>();
		mFeatures.add(new InstalledRecommendSwitchFeature());
		
		mPreference = InstalledRecommendPreference.getInstance();
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
	    mPreference.setInstalledRecommendEnable(enabled);
		InstalledRecommendManager mgr = InstalledRecommendManager.getInstance(getContext());
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
	}
	public void init() {
		InstalledRecommendManager.getInstance(getContext()).init();
	}
    @Override
    public boolean isEnabled() {
        return mPreference.isInstalledRecommendEnable();
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
		return null;
	}
	// ===========================================================
	// Methods
	// ===========================================================

	@Override
	public boolean canEnabled() {
		// TODO Auto-generated method stub
        return mPreference.isInstalledRecommendEnable();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
