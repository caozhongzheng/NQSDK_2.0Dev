package com.nqmobile.livesdk.modules.mustinstall;

import com.nqmobile.livesdk.commons.net.Listener;
import com.nqmobile.livesdk.modules.app.App;

import java.util.List;

/**
 * Created by Rainbow on 2014/11/18.
 */
public interface AppListListener extends Listener {

    public void onGetAppListSucc(List<App> apps);
}
