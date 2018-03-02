package com.nqmobile.livesdk.modules.locker;

import java.util.List;

import com.nqmobile.livesdk.commons.net.Listener;

/**
 * 锁屏列表获取监听
 * @time 2014/5/22 10:40:56
 */
public interface LockerListListener extends Listener {
	public void onGetLockerListSucc(List<Locker> lockers);
}
