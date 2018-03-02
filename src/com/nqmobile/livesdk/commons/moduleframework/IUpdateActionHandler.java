package com.nqmobile.livesdk.commons.moduleframework;

import java.util.Map;

public interface IUpdateActionHandler {
	/**
	 * action = 1
	 */
	public void enableFeature();
	
	/**
	 * action = 2
	 */
	public void disableFeature();
	
	/**
	 * action = 4
	 */
	public void upgradeAssets(Map<String, String> keyValues);
	
	/**
	 * action = 7
	 */
	public void updatePreferences(Map<String, String> keyValues);
	
	/**
	 * action = 8
	 */
	public void hasUpdate();
	
	/**
	 * action = 9
	 */
	public void updateSearchConfig(Map<String, String> keyValues);
	
	public boolean supportModuleLevelAction();
}
