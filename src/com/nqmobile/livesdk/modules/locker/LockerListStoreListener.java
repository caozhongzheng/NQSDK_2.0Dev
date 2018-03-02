package com.nqmobile.livesdk.modules.locker;

import java.util.List;

import com.nqmobile.livesdk.commons.net.NetworkingListener;

/**
 * 锁屏列表获取监听
 * @author caozhongzheng
 * @time 2014/5/7 12:28:43
 */
public interface LockerListStoreListener extends NetworkingListener {
	public void onGetLockerListSucc(int column, int offset, List<Locker[]> lockers);
}
