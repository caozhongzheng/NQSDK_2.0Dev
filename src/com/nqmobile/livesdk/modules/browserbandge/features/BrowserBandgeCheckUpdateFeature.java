
package com.nqmobile.livesdk.modules.browserbandge.features;

import android.content.Context;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.moduleframework.AbsFeature;
import com.nqmobile.livesdk.commons.moduleframework.AbsUpdateActionHandler;
import com.nqmobile.livesdk.commons.moduleframework.IUpdateActionHandler;
import com.nqmobile.livesdk.modules.browserbandge.BandgeManager;
import com.nqmobile.livesdk.utils.PreferenceDataHelper;

/**
 * 浏览器角标更新
 * 
 * @author HouKangxi
 *
 */
public class BrowserBandgeCheckUpdateFeature extends AbsFeature {

	// ===========================================================
	// Constants
	// ===========================================================
	private static final int FEATURE = 1002;
	private static final String SWITCH_KEY = PreferenceDataHelper.KEY_BANDGE_ENABLE;
	// ===========================================================
	// Fields
	// ===========================================================
	private UpdateHandler mHandler;
	private PreferenceDataHelper mHelper;

	// ===========================================================
	// Constructors
	// ===========================================================

	public BrowserBandgeCheckUpdateFeature() {
		mHelper = PreferenceDataHelper.getInstance(ApplicationContext
				.getContext());
		mHandler = new UpdateHandler();
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
		return mHelper.getBooleanValue(SWITCH_KEY);
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

	private class UpdateHandler extends AbsUpdateActionHandler {

		@Override
		public void hasUpdate() {
			final Context mContext = ApplicationContext.getContext();
			BandgeManager.getInstance(mContext).getBandge();

		}
	}
}