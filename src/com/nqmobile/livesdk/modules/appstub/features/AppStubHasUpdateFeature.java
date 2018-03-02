/**
 * 
 */
package com.nqmobile.livesdk.modules.appstub.features;

import android.content.Context;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.moduleframework.AbsFeature;
import com.nqmobile.livesdk.commons.moduleframework.AbsUpdateActionHandler;
import com.nqmobile.livesdk.commons.moduleframework.IUpdateActionHandler;
import com.nqmobile.livesdk.modules.appstub.AppStubManager;

/**
 * 虚框应用更新 Feature
 * 
 * @author HouKangxi
 *
 */
public class AppStubHasUpdateFeature extends AbsFeature {
	// ===========================================================
	// Constants
	// ===========================================================
	private final int FEATURE = 1012;
	// ===========================================================
	// Fields
	// ===========================================================
	private UpdateActionHandler mHandler;
	private AppStubManager mManager;

	// ===========================================================
	// Constructors
	// ===========================================================
	public AppStubHasUpdateFeature() {
		mHandler = new UpdateActionHandler();
		Context context = ApplicationContext.getContext();
		mManager = AppStubManager.getInstance(context);
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
		return mManager.isAppStubEnabled();
	}

	@Override
	public int getStatus() {
		// TODO getStatus
		// return manager.getStatus();
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
		public void hasUpdate() {
			mManager.processGetAppStub();
		}

	}
}
