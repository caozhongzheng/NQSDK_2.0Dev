/**
 * 
 */
package com.nqmobile.livesdk.modules.lqwidget.model;

/**
 * 
 * @author HouKangxi
 *
 */
public class LqWidgetInfo {
	private String resourceId;// 小组件资源ID
	private String name;// 小组件名称
	private String packageName;// 小组件包名
	private String description;// 描述
	private int versionCode;// 版本，从apk包中读取
	private String version;// 版本，从apk包中读取
	private String widgetId;// 小组件唯一id，从apk包中读取
	private long publishTime;// 版本发布时间
	private int size;// 大小字节数，从apk包中读取
	private String icon;// 图标地址
	private int spanx;// apk包中读取
	private int spany;// apk包中读取
	private int downloadNum;// 下载量（显示值）
	private int downloadWay;// 小组件下载方式,0直接下载（本地下载） 1 跳GP
	private String linkUrl;// 小组件下载地址

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getWidgetId() {
		return widgetId;
	}

	public void setWidgetId(String widgetId) {
		this.widgetId = widgetId;
	}

	public long getPublishTime() {
		return publishTime;
	}

	public void setPublishTime(long publishTime) {
		this.publishTime = publishTime;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public int getSpanx() {
		return spanx;
	}

	public void setSpanx(int spanx) {
		this.spanx = spanx;
	}

	public int getSpany() {
		return spany;
	}

	public void setSpany(int spany) {
		this.spany = spany;
	}

	public int getDownloadNum() {
		return downloadNum;
	}

	public void setDownloadNum(int downloadNum) {
		this.downloadNum = downloadNum;
	}

	public int getDownloadWay() {
		return downloadWay;
	}

	public void setDownloadWay(int downloadWay) {
		this.downloadWay = downloadWay;
	}

	public String getLinkUrl() {
		return linkUrl;
	}

	public void setLinkUrl(String linkUrl) {
		this.linkUrl = linkUrl;
	}

	@Override
	public String toString() {
		return "LqWidgetInfo [resourceId=" + resourceId + ", name=" + name
				+ ", packageName=" + packageName + ", description="
				+ description + ", versionCode=" + versionCode + ", version="
				+ version + ", widgetId=" + widgetId + ", publishTime="
				+ publishTime + ", size=" + size + ", icon=" + icon
				+ ", spanx=" + spanx + ", spany=" + spany + ", downloadNum="
				+ downloadNum + ", downloadWay=" + downloadWay + ", linkUrl="
				+ linkUrl + "]";
	}

}
