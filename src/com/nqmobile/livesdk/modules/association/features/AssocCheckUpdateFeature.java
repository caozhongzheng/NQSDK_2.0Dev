/**
 * 
 */
package com.nqmobile.livesdk.modules.association.features;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.moduleframework.AbsFeature;
import com.nqmobile.livesdk.commons.moduleframework.AbsUpdateActionHandler;
import com.nqmobile.livesdk.commons.moduleframework.IUpdateActionHandler;
import com.nqmobile.livesdk.modules.association.AssociationManager;
import com.nqmobile.livesdk.modules.association.AssociationPreference;

/**
 * 关联推荐更新
 * 
 * @author HouKangxi
 *
 */
public class AssocCheckUpdateFeature extends AbsFeature {

	// ===========================================================
	// Constants
	// ===========================================================
	private static final int FEATURE = 1115;
	private static final String SWITCH_KEY = AssociationPreference.KEY_ASSOCIATION_ENABLE;
	// ===========================================================
	// Fields
	// ===========================================================
	private UpdateHandler mHandler;
	private AssociationPreference mHelper;

	// ===========================================================
	// Constructors
	// ===========================================================

	public AssocCheckUpdateFeature() {
		mHelper = AssociationPreference.getInstance();
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
			AssociationManager.getInstance(ApplicationContext.getContext()).getAssciationInfoFromServer();
		}
	}
}