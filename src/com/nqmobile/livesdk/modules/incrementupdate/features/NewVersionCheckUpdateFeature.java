package com.nqmobile.livesdk.modules.incrementupdate.features;

import android.content.Context;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.moduleframework.AbsFeature;
import com.nqmobile.livesdk.commons.moduleframework.AbsUpdateActionHandler;
import com.nqmobile.livesdk.commons.moduleframework.IUpdateActionHandler;
import com.nqmobile.livesdk.modules.incrementupdate.IncrementUpdateManager;

/**
 * 新版本检查更新
 * 
 * @author HouKangxi
 *
 */
public class NewVersionCheckUpdateFeature extends AbsFeature {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final int FEATURE = 1013;
	// ===========================================================
	// Fields
	// ===========================================================
	private UpdateActionHandler mHandler;

	// ===========================================================
	// Constructors
	// ===========================================================
	public NewVersionCheckUpdateFeature() {
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
		return true;
	}

	@Override
	public int getStatus() {
		return 1;
	}

	@Override
	public String getVersionTag() {
		return "1.0";
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
	private static class UpdateActionHandler extends AbsUpdateActionHandler {

		@Override
		public void hasUpdate() {
			final Context context = ApplicationContext.getContext();
			IncrementUpdateManager.getInstance(context).getUpdate(null);
		}
	}
}
