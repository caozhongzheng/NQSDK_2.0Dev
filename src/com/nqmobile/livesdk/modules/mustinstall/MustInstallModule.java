package com.nqmobile.livesdk.modules.mustinstall;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.locker.LockerManager;
import com.nqmobile.livesdk.modules.mustinstall.features.MustInstallSwitchFeature;

/**
 * Created by Rainbow on 2014/11/19.
 */
public class MustInstallModule extends AbsModule {

	// ===========================================================
	// Constants
	// ===========================================================
    public static final String MODULE_NAME = "MustInstall";
	// ===========================================================
	// Fields
	// ===========================================================
	private List<IFeature> features;

    private MustInstallPreference mPreference;

	// ===========================================================
	// Constructors
	// ===========================================================
	public MustInstallModule() {
		features = new ArrayList<IFeature>(1);
		features.add(new MustInstallSwitchFeature());
		
		mPreference = MustInstallPreference.getInstance();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected void onEnabled(boolean enabled) {
		MustInstallManager mgr = MustInstallManager.getInstance(getContext());
		mPreference.setMustInstallEnable(enabled);
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
	}
	public void init() {
		MustInstallManager.getInstance(getContext()).init();
	}
    @Override
    public boolean canEnabled() {
        return mPreference.isMustInstallEnable();
    }

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
		return null;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
