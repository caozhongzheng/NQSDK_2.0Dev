/**
 * 
 */
package com.nqmobile.livesdk.modules.push.features;

import java.util.Map;

import com.nqmobile.livesdk.commons.moduleframework.AbsSwitchFeature;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;
import com.nqmobile.livesdk.commons.preference.SettingsPreference;
import com.nqmobile.livesdk.modules.push.PushModule;
import com.nqmobile.livesdk.modules.push.PushPreference;

/**
 * push Feature,处理push开关和push联网更新
 * 
 * @author HouKangxi
 *
 */
public class PushFeature extends AbsSwitchFeature {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final int FEATURE = 105;
	// ===========================================================
	// Fields
	// ===========================================================
	private SettingsPreference mHelper;

	// ===========================================================
	// Constructors
	// ===========================================================
	public PushFeature() {
		mHelper = PushPreference.getInstance();
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
	public boolean supportModuleLevelAction() {
		return true;
	}

	@Override
	public void updatePreferences(Map<String, String> map) {
		{
			// wifi 下 push联网频率，单位分钟
			String s = map.get("push_freq_wifi");
			if (s != null) {
				long value = Long.parseLong(s);
				mHelper.setLongValue(PushPreference.KEY_PUSH_FREQ_WIFI, value);
			}
		}
		{// 3G 下 push联网频率，单位分钟
			String s = map.get("push_freq_3g");
			if (s != null) {
				long value = Long.parseLong(s);
				mHelper.setLongValue(PushPreference.KEY_PUSH_FREQ_3G, value);
			}
		}
	}

	@Override
	protected IModule getModule() {
		return ModuleContainer.getInstance().getModuleByName(
				PushModule.MODULE_NAME);
	}

}
