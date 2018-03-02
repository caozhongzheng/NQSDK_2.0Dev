/**
 * 
 */
package com.nqmobile.livesdk.commons.mydownloadmanager;

/**
 * 下载进度状态监听及查询
 * @author chenyanmin
 * @time 2014-1-23 下午3:45:55
 */
public interface IDownloadObserver {
	/**
	 * 下载进度或状态有变化时触发
	 * @author chenyanmin
	 * @time 2014-1-23 下午3:51:38
	 * @param downloadedBytes 已经下载的字节数
	 * @param totalBytes 总字节数
	 * @param status MyDownloadManager.STATUS_*常量
	 */
	public void onChange();
}
