package com.nqmobile.livesdk.modules.search;

import com.nqmobile.livesdk.commons.preference.SettingsPreference;

public class SearcherPreference extends SettingsPreference {
	// ===========================================================
	// Constants
	// ===========================================================
	// featureId=[搜索框featureId]&showInterval=[热词展示时间间隔,单位秒]&searchBoxType=0&searchBoxName=Baidu&url=[搜索url，包含通配符]
	/** 搜索框featureId **/
	private static final String KEY_SEARCH_FEATUREID = "search_featureId";
	/** 热词展示时间间隔,单位秒 **/
	private static final String KEY_SEARCH_SHOWINTERVAL = "search_showInterval";
	private static final String KEY_SEARCH_SEARCHBOXTYPE = "search_searchBoxType";
	private static final String KEY_SEARCH_SEARCHBOXNAME = "search_searchBoxName";

	/** 搜索框url，包含通配符 **/
	public static final String KEY_SEARCH_URL_TG = "search_tgurl";
	/** 热词url，包含通配符 **/
	public static final String KEY_SEARCH_URL_RC = "search_rcurl";
	/** 搜索框热词，wifi下联网频率，单位分钟 */
	public static final String KEY_searchbox_hotword_wifi = "l_searchbox_hotword_wifi";
	/** 搜索框热词，3g下联网频率，单位分钟 */
	public static final String KEY_searchbox_hotword_3g = "l_searchbox_hotword_3g";
	private static final String KEY_lastUpdateTime = "l_searchbox_lastUpdateTime";
    
	// ===========================================================
	// Fields
	// ===========================================================
	private static SearcherPreference sInstance;

	// ===========================================================
	// Constructors
	// ===========================================================
	private SearcherPreference() {
	}

	public static SearcherPreference getInstance() {
		if (sInstance == null) {
			sInstance = new SearcherPreference();
		}
		return sInstance;
	}

	public String getFeatureId() {
		return getStringValue(KEY_SEARCH_FEATUREID);
	}

	public void setFeatureId(String v) {
		setStringValue(KEY_SEARCH_FEATUREID, v);
	}

	public String getSearchBoxName() {
		return getStringValue(KEY_SEARCH_SEARCHBOXNAME);
	}

	public void setSearchBoxName(String v) {
		setStringValue(KEY_SEARCH_SEARCHBOXNAME, v);
	}

	public long getShowInterval() {
		return getLongValue(KEY_SEARCH_SHOWINTERVAL);
	}

	public void setShowInterval(long v) {
		setLongValue(KEY_SEARCH_SHOWINTERVAL, v);
	}
	public long getLastUpdateTime() {
		return getLongValue(KEY_lastUpdateTime);
	}
	
	public void setLastUpdateTime(long v) {
		setLongValue(KEY_lastUpdateTime, v);
	}

	public int getSearchBoxType() {
		return getIntValue(KEY_SEARCH_SEARCHBOXTYPE);
	}

	public void setSearchBoxType(int v) {
		setIntValue(KEY_SEARCH_SEARCHBOXTYPE, v);
	}

	public String getTagUrl() {
		return getStringValue(KEY_SEARCH_URL_TG);
	}

	public void setTagUrl(String v) {
		setStringValue(KEY_SEARCH_URL_TG, v);
	}

	public String getRCUrl() {
		return getStringValue(KEY_SEARCH_URL_RC);
	}

	public void setRCUrl(String v) {
		setStringValue(KEY_SEARCH_URL_RC, v);
	}

	public long getWifiFrequency() {
		return getLongValue(KEY_searchbox_hotword_wifi);
	}

	public void setWifiFrequency(long v) {
		setLongValue(KEY_searchbox_hotword_wifi, v);
	}

	public long get3GFrequency() {
		return getLongValue(KEY_searchbox_hotword_3g);
	}

	public void set3GFrequency(long v) {
		setLongValue(KEY_searchbox_hotword_3g, v);
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
