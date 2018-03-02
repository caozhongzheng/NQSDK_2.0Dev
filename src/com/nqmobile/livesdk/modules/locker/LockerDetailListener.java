package com.nqmobile.livesdk.modules.locker;

import com.nqmobile.livesdk.commons.net.Listener;

/**
 * 锁屏详情获取监听
 * @author caozhongzheng
 * @time 2014/5/7 16:03:44
 */
public interface LockerDetailListener extends Listener {
	public void onGetDetailSucc(Locker locker);
}
