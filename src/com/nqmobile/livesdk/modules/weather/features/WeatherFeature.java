/**
 * 
 */
package com.nqmobile.livesdk.modules.weather.features;

import java.util.Map;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.moduleframework.AbsFeature;
import com.nqmobile.livesdk.commons.moduleframework.AbsUpdateActionHandler;
import com.nqmobile.livesdk.commons.moduleframework.IUpdateActionHandler;
import com.nqmobile.livesdk.utils.PreferenceDataHelper;

/**
 * 天气+PM2.5 开关 和联网更新频率
 * <p>
 * 开关暂不支持
 * 
 * @author HouKangxi
 *
 */
public class WeatherFeature extends AbsFeature {

	// ===========================================================
	// Constants
	// ===========================================================
	private static final int FEATURE = 0;
	// ===========================================================
	// Fields
	// ===========================================================
	private UpdateHandler mHandler;
	private PreferenceDataHelper mHelper;

	// ===========================================================
	// Constructors
	// ===========================================================

	public WeatherFeature() {
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
		return true;
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
		public boolean supportModuleLevelAction() {
			return true;
		}

		@Override
		public void updatePreferences(Map<String, String> map) {
			{
				// wifi 下 联网频率，单位分钟
				String s = map.get("l_weather_freq_wifi");
				if (s != null) {
					long value = Long.parseLong(s);
					mHelper.setLongValue(
							PreferenceDataHelper.KEY_WEATHER_FREQ_WIFI, value);
				}
			}
			{// 3G 下 联网频率，单位分钟
				String s = map.get("l_weather_freq_3g");
				if (s != null) {
					long value = Long.parseLong(s);
					mHelper.setLongValue(
							PreferenceDataHelper.KEY_WEATHER_FREQ_3G, value);
				}
			}
		}
	}
}
