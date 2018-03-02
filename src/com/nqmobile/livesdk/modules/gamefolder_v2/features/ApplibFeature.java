package com.nqmobile.livesdk.modules.gamefolder_v2.features;

import static com.nqmobile.livesdk.modules.gamefolder_v2.GameFolderV2Preference.KEY_GAME_FOLDER_ENABLE;

import java.util.Map;

import com.nqmobile.livesdk.commons.moduleframework.AbsFeature;
import com.nqmobile.livesdk.commons.moduleframework.AbsUpdateActionHandler;
import com.nqmobile.livesdk.commons.moduleframework.IUpdateActionHandler;
import com.nqmobile.livesdk.modules.apptype.AppTypeManager;
import com.nqmobile.livesdk.modules.gamefolder_v2.GameFolderV2Preference;
import com.nqmobile.livesdk.modules.gamefolder_v2.GameManager;

public class ApplibFeature extends AbsFeature {
	// ===========================================================
	// Constants
	// ===========================================================
	private final int FEATURE = 1000;
	// ===========================================================
	// Fields
	// ===========================================================
	private GameFolderV2Preference mPreference;
	private UpdateActionHandler mHandler;

	// ===========================================================
	// Constructors
	// ===========================================================
	public ApplibFeature() {
		mPreference = GameFolderV2Preference.getInstance();
		mHandler = new UpdateActionHandler();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public int getFeatureId() {
		return FEATURE;
	}

	@Override
	public boolean isEnabled() {
		boolean enabled = mPreference.getBooleanValue(KEY_GAME_FOLDER_ENABLE);
		return enabled;
	}

	@Override
	public String getVersionTag() {
		return mPreference.getAppLibVersion();
	}

	@Override
	public int getStatus() {
		return mPreference.getGameFolderStatus();
	}

	public IUpdateActionHandler getHandler() {
		return mHandler;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	private class UpdateActionHandler extends AbsUpdateActionHandler {

		@Override
		public void upgradeAssets(Map<String, String> keyValues) {
			String version = keyValues.get("version");
			String url = keyValues.get("url");
			String md5 = keyValues.get("md5");
			// String size = keyValues.get("size");
			// String net = keyValues.get("net");
			mPreference.setAppLibServerVersion(version);
			mPreference.setAppLibServerUrl(url);
			mPreference.setAppLibServerMd5(md5);
			AppTypeManager.getInstance().upgradeLibFile();
		}

	}

}
