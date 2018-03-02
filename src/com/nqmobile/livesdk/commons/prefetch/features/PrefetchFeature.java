package com.nqmobile.livesdk.commons.prefetch.features;

import static com.nqmobile.livesdk.commons.prefetch.PrefetchPreference.*;

import java.util.Map;

import android.text.TextUtils;

import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsSwitchFeature;
import com.nqmobile.livesdk.commons.moduleframework.IModule;
import com.nqmobile.livesdk.commons.moduleframework.ModuleContainer;
import com.nqmobile.livesdk.commons.prefetch.PrefetchModule;
import com.nqmobile.livesdk.commons.prefetch.PrefetchPreference;

public class PrefetchFeature extends AbsSwitchFeature {
	private ILogger logger;
	
	public PrefetchFeature() {
		logger = LoggerFactory.getLogger(PrefetchModule.MODULE_NAME);
	}

	@Override
	public int getFeatureId() {
		return 119;
	}

	@Override
	public boolean supportModuleLevelAction() {
		return true;
	}

	@Override
	public void updatePreferences(Map<String, String> map) {
		PrefetchPreference helper = PrefetchPreference.getInstance();
		String format = "change config '%s', set to %s";
//		{
//			String k = KEY_DOWNLOAD_MODULE;
//			String v = map.get(k);
//			if (v != null) {
//				logger.d(String.format(format, k,v));
//				helper.setStringValue(k, v);
//			}
//		}
//		{
//			String k = KEY_DOWNLOAD_ALLOW_SIZE;
//			String v = map.get(k);
//			if (v != null && TextUtils.isDigitsOnly(v = v.trim())) {
//				logger.d(String.format(format, k,v));
//				helper.setLongValue(k, Long.parseLong(v)<<10);// kb->b
//			}
//		}
		{
			String k = KEY_DOWNLOAD_CACHESIZE;
			String v = map.get(k);
			if (v != null && TextUtils.isDigitsOnly(v = v.trim())) {
				logger.d(String.format(format, k,v));
				helper.setLongValue(k, Long.parseLong(v)<<10);// kb->b
			}
		}
	}

	@Override
	protected IModule getModule() {
		return ModuleContainer.getInstance().getModuleByName(
				PrefetchModule.MODULE_NAME);
	}
}
