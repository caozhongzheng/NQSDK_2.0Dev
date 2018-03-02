package com.nqmobile.livesdk.modules.locker;

import java.util.List;

import com.nqmobile.livesdk.modules.locker.model.LockerEngine;

/**
 * store有关锁屏的监听接口。
 * 
 * @author caozhongzheng
 * @time 2014/6/5 11:18:36
 */
public interface ILockerSDK {
	/**
	 * 查看锁屏引擎类型详细信息
	 * 
	 * @param engineID
	 *            引擎类型ID(0:本地灵动锁屏引擎 1:拉风锁屏引擎)
	 * @return
	 */
	LockerEngine getLockerEngine(int engineID);

	/**
	 * 查看全部被锁屏引擎类型详细信息
	 * 
	 * @return
	 */
	List<LockerEngine> getLockerEngineList();

	/**
	 * 锁屏应用接口
	 * 
	 * @param l
	 * @return SDK回调监听对象，锁屏引擎返回应用成果或失败结果
	 */
	boolean applyLocker(Locker l);

	/**
	 * 锁屏体验接口
	 * 
	 * @param l
	 * @return SDK回调监听对象，锁屏引擎返回体验成果或失败结果
	 */
	boolean previewLocker(Locker l);

	/**
	 * 锁屏引擎升级接口 新的锁屏引擎文件下载成功后，SDK调用此方法，通知锁屏引擎模块加载新引擎文件，返回锁屏加载成功或失败结果
	 * 
	 * @param le
	 * @return
	 */
	boolean upgradeLockerEngine(LockerEngine le);
}
