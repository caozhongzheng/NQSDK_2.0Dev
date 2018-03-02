/**
 * 
 */
package com.nqmobile.livesdk.commons.mydownloadmanager;

/**
 * @author HouKangxi
 *
 */
public interface IDownloadObserver2 extends IDownloadObserver {
	/**
	 * 已取消
	 */
	int STATUS_CANCEL= -1;
	/**
	 * 下载进度或状态有变化时触发
	 * 
	 * @param downloadId - 下载表的id
	 * @param bytesSoFar - 已经下载的字节数
	 * @param status - 下载状态 ,one of below:<br/>
	 * {@link #STATUS_CANCEL}
	 * {@link com.nqmobile.livesdk.commons.downloadmanager.DownloadManager#STATUS_PENDING}
	 * {@link com.nqmobile.livesdk.commons.downloadmanager.DownloadManager#STATUS_RUNNING}
	 * {@link com.nqmobile.livesdk.commons.downloadmanager.DownloadManager#STATUS_PAUSED}
	 * {@link com.nqmobile.livesdk.commons.downloadmanager.DownloadManager#STATUS_SUCCESSFUL}
	 * {@link com.nqmobile.livesdk.commons.downloadmanager.DownloadManager#STATUS_FAILED}
	 */
	public void onChange(long downloadId ,long bytesSoFar, int status);
}
