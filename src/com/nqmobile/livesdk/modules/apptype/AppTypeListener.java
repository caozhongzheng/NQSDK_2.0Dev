package com.nqmobile.livesdk.modules.apptype;

import java.util.List;

import com.nqmobile.livesdk.commons.net.Listener;
import com.nqmobile.livesdk.modules.apptype.model.AppTypeInfo;

public interface AppTypeListener extends Listener {
	public void getAppTypeSucc(List<AppTypeInfo> mAppTypeInfo);
}
