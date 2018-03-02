package com.nqmobile.livesdk.modules.locker;

import com.nqmobile.livesdk.commons.net.NetworkingListener;
import com.nqmobile.livesdk.modules.locker.model.LockerEngine;

/**
 * 锁屏引擎新版本获取监听
 * @author caozhongzheng
 * @time 2014/5/21 19:24:43
 */
public interface NewLockerEngineListener extends NetworkingListener {
	public void onGetNewLockerEngineSucc(LockerEngine lockerEngine);
}
