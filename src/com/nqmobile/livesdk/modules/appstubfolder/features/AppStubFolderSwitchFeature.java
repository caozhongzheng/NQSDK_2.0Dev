/**
 * 
 */
package com.nqmobile.livesdk.modules.appstubfolder.features;

import java.util.HashMap;
import java.util.Map;

import com.nqmobile.livesdk.commons.moduleframework.AbsSwitchFeature;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;
import com.nqmobile.livesdk.modules.appstubfolder.AppStubFolderModule;
import com.nqmobile.livesdk.modules.appstubfolder.AppStubFolderPreference;

/**
 * 虚框应用开关Feature
 * 
 * @author HouKangxi
 * 
 */
public class AppStubFolderSwitchFeature extends AbsSwitchFeature {
	// ===========================================================
	// Constants
	// ===========================================================
	private final int FEATURE = 116;
	private static final HashMap<String, String> FREQ_MAP = new HashMap<String, String>();
	static {
		FREQ_MAP.put("l_virtualfolder_freq_wifi",
				AppStubFolderPreference.KEY_STUB_FOLDER_FREQ_WIFI); // 虚框文件夹，wifi下联网频率，单位分钟
		FREQ_MAP.put("l_virtualfolder_freq_3g",
				AppStubFolderPreference.KEY_STUB_FOLDER_FREQ_3G); // 虚框文件夹，非wifi下联网频率，单位分钟
	}
	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

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
	public void updatePreferences(Map<String, String> serverParamMap) {
		for (Map.Entry<String, String> entry : FREQ_MAP.entrySet()) {
			String serverKey = entry.getKey();
			String value = serverParamMap.get(serverKey);
			if (value != null) {
				String clientKey = entry.getValue();
				AppStubFolderPreference.getInstance().setLongValue(clientKey,
						Long.parseLong(value));
			}
		}
	}

	@Override
	protected IModule getModule() {
		return ModuleContainer.getInstance().getModuleByName(
				AppStubFolderModule.MODULE_NAME);
	}
}
