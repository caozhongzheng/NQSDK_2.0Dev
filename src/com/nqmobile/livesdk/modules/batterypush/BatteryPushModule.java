package com.nqmobile.livesdk.modules.batterypush;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.banner.BannerManager;
import com.nqmobile.livesdk.modules.batterypush.features.BatterypushSwitchFeature;

public class BatteryPushModule extends AbsModule {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(BatteryPushModule.MODULE_NAME);
	
	public static final String MODULE_NAME = "BatteryPush";
	// ===========================================================
	// Fields
	// ===========================================================
	private List<IDataTable> mTables;
	private List<IFeature> features;
	
	private BatteryPushPreference mPreference;
	// ===========================================================
	// Constructors
	// ===========================================================
	public BatteryPushModule() {
		features = new ArrayList<IFeature>();
		features.add(new BatterypushSwitchFeature());
		
		mPreference = BatteryPushPreference.getInstance();
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
    public boolean canEnabled() {
        return mPreference.isBatteryPushEnable();
    }

	String tag = "handy BatteryPushModule";
	@Override
	protected void onEnabled(boolean enabled) {
		NqLog.i("enabled = "+ enabled);
		mPreference.setBatteryPushEnable(enabled);
		BatteryPushManger mgr = BatteryPushManger.getInstance(getContext());
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
	}
	public void init() {
		BatteryPushManger.getInstance(getContext()).init();
	}

	@Override
	public String getLogTag() {
		return MODULE_NAME;
	}



	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
