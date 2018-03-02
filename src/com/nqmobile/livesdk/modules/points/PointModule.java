package com.nqmobile.livesdk.modules.points;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.mustinstall.MustInstallManager;
import com.nqmobile.livesdk.modules.points.features.PointSwitchFeature;
import com.nqmobile.livesdk.modules.points.table.PointResourceTable;
import com.nqmobile.livesdk.modules.points.table.RewardPointsTable;

/**
 * Created by Rainbow on 2014/11/18.
 */
public class PointModule extends AbsModule {

	public static final String MODULE_NAME = "Points";

	private List<IDataTable> mTables;
    private List<IFeature> mFeatures;

    private PointsPreference mPreference;

	public PointModule() {
		mTables = new ArrayList<IDataTable>();
		mTables.add(new PointResourceTable());
		mTables.add(new RewardPointsTable());
		
		mPreference = PointsPreference.getInstance();
        mFeatures = new ArrayList<IFeature>();
        mFeatures.add(new PointSwitchFeature());
	}

	@Override
	protected void onEnabled(boolean enabled) {
	    mPreference.setPointCenterEnable(enabled);
		AbsManager mgr = PointsManager.getInstance(getContext());
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
	}
	public void init() {
		PointsManager.getInstance(getContext()).init();
	}
    @Override
    public boolean canEnabled() {
        return mPreference.isPointCenterEnable();
    }

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public List<IFeature> getFeatures() {
		return mFeatures;
	}

	@Override
	public List<IDataTable> getTables() {
		return mTables;
	}

}
