package com.nqmobile.livesdk.modules.incrementupdate;

import com.nqmobile.livesdk.commons.net.Listener;
import com.nqmobile.livesdk.modules.incrementupdate.model.NewVersion;

/**
 * 有新版本监听
 * @author chenyanmin
 * @time 2014-8-4 下午4:08:44
 */
public interface NewVersionListener extends Listener{
    public void onGetNewVersionSucc(NewVersion newVersion);
}
