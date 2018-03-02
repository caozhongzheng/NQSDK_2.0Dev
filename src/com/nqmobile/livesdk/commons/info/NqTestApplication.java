package com.nqmobile.livesdk.commons.info;

import android.app.Application;

import com.nqmobile.livesdk.LauncherSDK;
import com.nqmobile.livesdk.modules.regularupdate.RegularUpdateManager;

public class NqTestApplication extends Application {
	@Override
    public void onCreate() {
        super.onCreate();
        LauncherSDK.getInstance(this).initSDK();
		if (ProcessInfo.isStoreProcess(this)) {
			RegularUpdateManager.getInstance().regularUpdate(true);
		}
    }

}
