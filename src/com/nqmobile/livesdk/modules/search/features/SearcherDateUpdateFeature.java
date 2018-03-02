package com.nqmobile.livesdk.modules.search.features;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.text.TextUtils;

import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsFeature;
import com.nqmobile.livesdk.commons.moduleframework.AbsUpdateActionHandler;
import com.nqmobile.livesdk.commons.moduleframework.IUpdateActionHandler;
import com.nqmobile.livesdk.modules.search.SearcherModule;
import com.nqmobile.livesdk.modules.search.SearcherPreference;
import com.nqmobile.livesdk.utils.Tools;

/**
 * 客户端联网频率更新
 * 
 * @author HouKangxi
 *
 */
public class SearcherDateUpdateFeature extends AbsFeature {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(SearcherModule.MODULE_NAME);
	
	private static final int FEATURE = 1118;
	// ===========================================================
	// Fields
	// ===========================================================
	private UpdateActionHandler mHandler;
	private SearcherPreference mPreference;

	// ===========================================================
	// Constructors
	// ===========================================================
	public SearcherDateUpdateFeature() {
		mHandler = new UpdateActionHandler();
		mPreference = SearcherPreference.getInstance();
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
		return true;//TODO 暂时没有开关
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

	@Override
	public String getVersionTag() {
		return "1";
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	private class UpdateActionHandler extends AbsUpdateActionHandler {
		//featureId=[搜索框featureId]&showInterval=[热词展示时间间隔,单位秒]&searchBoxType=0&searchBoxName=Baidu&url=[搜索url，包含通配符]
		/** 热词展示时间间隔,单位秒 **/
		private static final String SHOWINTERVAL = "showInterval";
		private static final String SEARCHBOXTYPE = "searchBoxType";
		private static final String SEARCHBOXNAME = "searchBoxName";
		private static final String URL_TG = "tgurl";
		private static final String URL_RC = "rcurl";
		
		@Override
		public boolean supportModuleLevelAction() {
			return true;
		}

		@Override
		public void updateSearchConfig(Map<String, String> map) {
			//这里只处理与定期联网模块本身的更新频率，其余联网频率更新功能分散在各个模块内
			// 定期联网接口，wifi 下联网频率，单位分钟
			try {
				Set<Entry<String, String>> entry = map.entrySet();
				Iterator<Entry<String, String>> iterator = entry.iterator();
				while (iterator.hasNext()) {
					Entry<String, String> itEntry = iterator.next();
					NqLog.d("search key:" + itEntry.getKey() + " value:" + itEntry.getValue());
					
				}
				String s;
				s = map.get(SHOWINTERVAL);
				if (!Tools.isEmpty(s)) {
					long value = Long.parseLong(s);
					mPreference.setShowInterval(value);
				}
				s = map.get(SEARCHBOXTYPE);
				if (!Tools.isEmpty(s)) {
					int value = Integer.parseInt(s);
					mPreference.setSearchBoxType(value);
				}
				s = map.get(SEARCHBOXNAME);
				if (!Tools.isEmpty(s)) {
					mPreference.setSearchBoxName(s);
				}
				s = map.get(URL_TG);
				if (!Tools.isEmpty(s)) {
					mPreference.setTagUrl(s);
				}
				s = map.get(URL_RC);
				if (!Tools.isEmpty(s)) {
					mPreference.setRCUrl(s);
				}
				s = map.get(SearcherPreference.KEY_searchbox_hotword_wifi);
				if (s != null && TextUtils.isDigitsOnly(s)) {
					long value = Long.parseLong(s);
					mPreference.setWifiFrequency(value);
				}
				s = map.get(SearcherPreference.KEY_searchbox_hotword_3g);
				if (s != null && TextUtils.isDigitsOnly(s)) {
					long value = Long.parseLong(s);
					mPreference.set3GFrequency(value);
				}
			} catch (Exception e) {
				NqLog.e("SearcherDateUpdateFeature updatePreferences err", e);
			}
		}

	}
}
