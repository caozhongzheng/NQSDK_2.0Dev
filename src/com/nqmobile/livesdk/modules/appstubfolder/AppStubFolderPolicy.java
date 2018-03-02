package com.nqmobile.livesdk.modules.appstubfolder;

import android.content.Context;
import android.text.format.DateUtils;

import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.moduleframework.IPolicy;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.NetworkUtils;

public class AppStubFolderPolicy implements IPolicy {

	public static boolean needUpdateAppStubFolder() {
		boolean result = false;

		AppStubFolderPreference preference = AppStubFolderPreference
				.getInstance();

		long now = SystemFacadeFactory.getSystem().currentTimeMillis();
		long lastVfTime = preference.
				getLongValue(AppStubFolderPreference.KEY_LAST_GET_STUB_FOLDER_TIME);
		long vf_freq_wifi = preference.getStubFolderFreqWifiInMinutes() * DateUtils.MINUTE_IN_MILLIS;
		long vf_freq_3g = preference.getStubFolderFreq3GInMinutes() * DateUtils.MINUTE_IN_MILLIS;

		Context mContext = ApplicationContext.getContext();
		if (preference
				.getBooleanValue(AppStubFolderPreference.KEY_STUB_FOLDER_ENABLE)
				&& CommonMethod.hasActiveNetwork(mContext)) {
			if (NetworkUtils.isWifi(mContext)) {
				if (Math.abs(now - lastVfTime) >= (vf_freq_wifi == 0L ? 2 * DateUtils.DAY_IN_MILLIS
						: vf_freq_wifi)) {
					result = true;
				}
			} else {
				if (Math.abs(now - lastVfTime) >= (vf_freq_3g == 0L ? 7 * DateUtils.DAY_IN_MILLIS
						: vf_freq_3g)) {
					result = true;
				}
			}
		}

		return result;
	}
}