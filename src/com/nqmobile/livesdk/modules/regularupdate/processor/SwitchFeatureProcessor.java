package com.nqmobile.livesdk.modules.regularupdate.processor;

import java.util.Map;

import android.text.TextUtils;

import com.aps.ac;
import com.nqmobile.livesdk.commons.moduleframework.IUpdateActionHandler;
import com.nqmobile.livesdk.modules.regularupdate.model.UpdateAction;
import com.nqmobile.livesdk.utils.UriQueryUtils;

public class SwitchFeatureProcessor implements IUpdateActionProcessor {
	// ===========================================================
	// Constants
	// ===========================================================
	private final static int ENABLE = 1;
	private final static int DISABLE = 2;

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	SwitchFeatureProcessor() {
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
		if (handler == null) {
			return;
		}

		switch (action.getActionId()) {
		case ENABLE: {
			handler.enableFeature();
			break;
		}
		case DISABLE: {
			handler.disableFeature();
			break;
		}
		}
	}
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
