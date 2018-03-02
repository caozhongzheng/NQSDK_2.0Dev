package com.nqmobile.livesdk.modules.app;


public class AppStatus {
	public static final int STATUS_UNKNOWN = AppManager.STATUS_UNKNOWN;//未知
    public static final int STATUS_NONE = AppManager.STATUS_NONE;//未安装，未下载
    public static final int STATUS_DOWNLOADING = AppManager.STATUS_DOWNLOADING;//未安装，下载中
    public static final int STATUS_PAUSED = AppManager.STATUS_PAUSED;//未安装，下载暂停中
    public static final int STATUS_DOWNLOADED = AppManager.STATUS_DOWNLOADED;//未安装，已下载
    public static final int STATUS_INSTALLED = AppManager.STATUS_INSTALLED;//已安装
    
	public int statusCode;
	public long downloadedBytes;
	public long totalBytes;
	public int downloadProgress;// downloadedBytes * 100 / totalBytes

	public AppStatus() {
		super();
	}

	public AppStatus(int statusCode, long downloadedBytes, long totalBytes,
			int downloadProgress) {
		super();
		this.statusCode = statusCode;
		this.downloadedBytes = downloadedBytes;
		this.totalBytes = totalBytes;
		this.downloadProgress = downloadProgress;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public long getDownloadedBytes() {
		return downloadedBytes;
	}

	public void setDownloadedBytes(long downloadedBytes) {
		this.downloadedBytes = downloadedBytes;
	}

	public long getTotalBytes() {
		return totalBytes;
	}

	public void setTotalBytes(long totalBytes) {
		this.totalBytes = totalBytes;
	}

	public int getDownloadProgress() {
		return downloadProgress;
	}

	public void setDownloadProgress(int downloadProgress) {
		this.downloadProgress = downloadProgress;
	}

	@Override
	public String toString() {
		return "AppStatus [statusCode=" + statusCode + ", downloadedBytes="
				+ downloadedBytes + ", totalBytes=" + totalBytes
				+ ", downloadProgress=" + downloadProgress + "]";
	}
}
