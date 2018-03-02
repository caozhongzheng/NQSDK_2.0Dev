/**
 * 
 */
package com.nqmobile.livesdk.commons.prefetch.event;

/**
 * 取消静默下载的事件
 * <p>
 * 产生此事件的原因可以是主动取消（如：前台主动点击了下载，取消后台静默下载）<br/>
 * 、相应的前台资源被删除（如：虚框icon被删除），等等，总之是取消下载
 * 
 * @author HouKangxi
 *
 */
public class CancelSilentDownloadEvent {
	private String resourceId;
	private String cause;//取消原因，可选

	public CancelSilentDownloadEvent(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}
    
}
