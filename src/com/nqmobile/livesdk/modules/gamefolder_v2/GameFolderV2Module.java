package com.nqmobile.livesdk.modules.gamefolder_v2;

import java.util.ArrayList;
import java.util.List;

import com.nqmobile.livesdk.commons.db.IDataTable;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsModule;
import com.nqmobile.livesdk.commons.moduleframework.IFeature;
import com.nqmobile.livesdk.modules.apptype.features.ApplibFeature;
import com.nqmobile.livesdk.modules.gamefolder_v2.features.GameFolderV2Feature;
import com.nqmobile.livesdk.modules.gamefolder_v2.table.GameCacheTable;
import com.nqmobile.livesdk.utils.MResource;

public class GameFolderV2Module extends AbsModule {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(GameFolderV2Module.MODULE_NAME);
	public static final String MODULE_NAME = "GameFolderV2";
	// ===========================================================
	// Fields
	// ===========================================================
	private List<IDataTable> mTables;
	private List<IFeature> mFeatures;
    private GameFolderV2Preference mPreference;

	// ===========================================================
	// Constructors
	// ===========================================================
	public GameFolderV2Module() {
		mTables = new ArrayList<IDataTable>();
		mTables.add(new GameCacheTable());

		mFeatures = new ArrayList<IFeature>();
		mFeatures.add(new GameFolderV2Feature());
		
		mPreference = GameFolderV2Preference.getInstance();
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
	    mPreference.setGameFolderEnabled(enabled);
		GameManager mgr = GameManager.getInstance();
		if (enabled) {
			mgr.init();
		} else {
			mgr.onDisable();
		}
	}
	public void init() {
		GameManager.getInstance().init();
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
		return mTables;
	}
	// ===========================================================
	// Methods
	// ===========================================================

    @Override
	public void onAppFirstInit(boolean enabled) {
    	String appLibVersion = MResource.getString(getContext(), "nq_app_lib_ver");
    	mPreference.setAppLibVersion(appLibVersion);
    	NqLog.i("app_lib_ver: "+appLibVersion);
    	mPreference.setGameFolderStatus(GameManager.GAME_FOLDER_STATUS_NONE);
	}

	@Override
    public boolean canEnabled() {
        return mPreference.isGameFolderEnabled();
    }

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
