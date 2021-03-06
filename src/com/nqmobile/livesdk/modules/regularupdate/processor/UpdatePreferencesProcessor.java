package com.nqmobile.livesdk.modules.regularupdate.processor;

import java.util.Map;

import com.nqmobile.livesdk.commons.moduleframework.IUpdateActionHandler;
import com.nqmobile.livesdk.modules.regularupdate.RegularUpatePreference;
import com.nqmobile.livesdk.modules.regularupdate.model.UpdateAction;
import com.nqmobile.livesdk.utils.UriQueryUtils;

public class UpdatePreferencesProcessor implements IUpdateActionProcessor {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	private RegularUpatePreference mPreference;

	// ===========================================================
	// Constructors
	// ===========================================================
	UpdatePreferencesProcessor() {
		mPreference = RegularUpatePreference.getInstance();
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
		String value = keyValues.get("version");
		if (value != null) {
			mPreference.setRegularUpdateVersion(value);
		}

		for (IUpdateActionHandler handler : handlers.values()) {
			if (handler.supportModuleLevelAction()) {
				handler.updatePreferences(keyValues);
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
