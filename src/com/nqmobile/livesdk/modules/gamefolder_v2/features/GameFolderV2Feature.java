package com.nqmobile.livesdk.modules.gamefolder_v2.features;

import java.util.HashMap;
import java.util.Map;

import com.nqmobile.livesdk.commons.moduleframework.AbsSwitchFeature;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;
import com.nqmobile.livesdk.modules.gamefolder_v2.GameFolderV2Module;
import com.nqmobile.livesdk.modules.gamefolder_v2.GameFolderV2Preference;

public class GameFolderV2Feature extends AbsSwitchFeature {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final int FEATURE_GAME_FOLDER = 100;
	private static final HashMap<String, String> FREQ_MAP = new HashMap<String, String>();
	static {
		FREQ_MAP.put("game_freq_wifi", GameFolderV2Preference.KEY_GAME_FREQ_WIFI); // 游戏文件夹，wifi下联网频率，单位分钟
		FREQ_MAP.put("game_freq_3g", GameFolderV2Preference.KEY_GAME_FREQ_3G); // 游戏文件夹，非wifi下联网频率，单位分钟
		FREQ_MAP.put("l_newgame_show_num",
				GameFolderV2Preference.KEY_NEWGAME_SHOWNUM); // 全部推荐游戏展示次数配置，单位次数
		FREQ_MAP.put("l_newgame_cachevalid",
				GameFolderV2Preference.KEY_NEWGAME_CACHEVALID); // 游戏文件夹缓存时间配置，单位分钟
	}
	// ===========================================================
	// Fields
	// ===========================================================
    private GameFolderV2Preference mPreference;

	// ===========================================================
	// Constructors
	// ===========================================================
	public GameFolderV2Feature() {
		mPreference = GameFolderV2Preference.getInstance();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public int getFeatureId() {
		return FEATURE_GAME_FOLDER;
	}

	@Override
	public int getStatus() {
		return mPreference.getGameFolderStatus();
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
				GameFolderV2Preference.getInstance().setLongValue(clientKey,
						Long.parseLong(value));
			}
		}
	}

	@Override
	protected IModule getModule() {
		return ModuleContainer.getInstance().getModuleByName(
				GameFolderV2Module.MODULE_NAME);
	}
}
