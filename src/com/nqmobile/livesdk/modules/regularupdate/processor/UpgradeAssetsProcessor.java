package com.nqmobile.livesdk.modules.regularupdate.processor;

import java.util.Map;

import android.text.TextUtils;

import com.nqmobile.livesdk.commons.moduleframework.IUpdateActionHandler;
import com.nqmobile.livesdk.modules.regularupdate.model.UpdateAction;
import com.nqmobile.livesdk.utils.UriQueryUtils;

public class UpgradeAssetsProcessor implements IUpdateActionProcessor {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	UpgradeAssetsProcessor() {
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void process(Map<Integer, IUpdateActionHandler> handlers,
			UpdateAction action) {
		Map<String, String> keyValues = UriQueryUtils.parse(action
				.getParameters());
		String f = keyValues.get("featureId");
		if (!TextUtils.isDigitsOnly(f)) {
			return;
		}

		int featureId = Integer.parseInt(f);
		IUpdateActionHandler handler = handlers.get(featureId);
		if (handler != null) {
			handler.upgradeAssets(keyValues);
		}
	}
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
