package com.nqmobile.livesdk.modules.gamefolder_v2;

import com.nqmobile.livesdk.commons.net.NetworkingListener;
import com.nqmobile.livesdk.modules.app.App;

public interface PreloadListener extends NetworkingListener {
	public void onPreloadSucc(App app);
} 
