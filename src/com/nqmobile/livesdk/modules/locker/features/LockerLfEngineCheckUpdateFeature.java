/**
 * 
 */
package com.nqmobile.livesdk.modules.locker.features;

import java.util.Map;

import android.content.Context;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.moduleframework.AbsFeature;
import com.nqmobile.livesdk.commons.moduleframework.AbsUpdateActionHandler;
import com.nqmobile.livesdk.commons.moduleframework.IUpdateActionHandler;
import com.nqmobile.livesdk.utils.PreferenceDataHelper;

/**
 * 拉风锁屏引擎版本更新
 * 
 * @author HouKangxi
 *
 */
public class LockerLfEngineCheckUpdateFeature extends AbsFeature {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final int FEATURE = 1011;

	// ===========================================================
	// Fields
	// ===========================================================
	private UpdateActionHandler mHandler;

	// ===========================================================
	// Constructors
	// ===========================================================
	public LockerLfEngineCheckUpdateFeature() {
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
		return false;
	}

	@Override
	public String getVersionTag() {
		Context mContext = ApplicationContext.getContext();
		String versionTag = PreferenceDataHelper.getInstance(mContext)
				.getStringValue(PreferenceDataHelper.KEY_ENGINE_LIB_LF_VERSION);
		return versionTag;
	}

	@Override
	public int getStatus() {
		return 0;
	}

	@Override
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
			// TODO 更新拉风锁屏引擎
			super.upgradeAssets(keyValues);
		}

	}
}
