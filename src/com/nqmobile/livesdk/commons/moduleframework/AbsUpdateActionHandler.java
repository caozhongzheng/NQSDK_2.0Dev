package com.nqmobile.livesdk.commons.moduleframework;

import java.util.Map;

public abstract class AbsUpdateActionHandler implements IUpdateActionHandler {

	@Override
	public void enableFeature() {
	}

	@Override
	public void disableFeature() {
	}

	@Override
	public void upgradeAssets(Map<String, String> keyValues) {
	}
	
	@Override
	public void updatePreferences(Map<String, String> keyValues) {
		
	}

	@Override
	public void hasUpdate() {
	}

	@Override
	public boolean supportModuleLevelAction() {
		return false;
	}

	@Override
	public void updateSearchConfig(Map<String, String> keyValues) {
	}
}
