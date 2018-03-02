package com.nqmobile.livesdk.modules.push.model;

import java.io.Serializable;

/**
 * Push
 * @author caozhongzheng
 * @time 2014年4月10日11:48:37
 */
public class Push implements Serializable {
	private static final long serialVersionUID = -1712681302849315145L;
	/** push资源ID,如 PH5122dba3a388de1fbe529c6d */
	private String strId;
	/** 标题 */
	private String title;
	/** 副标题 */
	private String subtitle;
	/** 图标URL */
	private String icon;
	/** push类型  0:普通类型 1:自定义大图类型*/
	private int pushStyle;
	/** push自定义类型大图*/
	private String bigIcon;
	/** push大图类型显示按钮  0:不显示 1:显示*/
	private int useBtn;
	/** push自定义类型 不喜欢按钮文本*/
	private String unlikeText;
	/** push自定义类型 下载按钮文本*/
	private String downText;
	/** 跳转类型,0--打开store详情 2 直接下载（本地下载）3--打开浏览器 9--无行为 */
	private int jumpType;
	/** 下载地址 （直接下载地址或GP下载地址，取决于jumpType */
	private String downUrl;
	/** 应用包名 */
	private String packageName;
	/** 资源Id */
	private String linkResourceId;
	/** 资源类型：0应用 1主题 2壁纸 */
	private int intType;
	/** 联网环境限制  0---只要有网络都可以展示消息 1--仅WIFI下展示消息 */
	private int netType;
	/** 用户场景限制  0---任何情况下都可以展示消息 1--仅回到桌面下展示消息 */
	private int desktopType;
	/** 消息生效时间 */
	private long longStartTime;
	/** 消息失效时间 */
	private long longExpirTime;
	/** 展示时间段开始时间,如08:00 */
	private String showStartTime;
	/**展示时间段结束时间,如23:00 */
	private String showEndTime;
	
	private  boolean slientDownload;
	private long size;
	public boolean isSlientDownload() {
		return slientDownload;
	}
	public void setSlientDownload(boolean slientDownload) {
		this.slientDownload = slientDownload;
	}
	
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getStrId() {
		return strId;
	}
	public void setStrId(String strId) {
		this.strId = strId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSubtitle() {
		return subtitle;
	}
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public int getPushStyle() {
		return pushStyle;
	}
	public void setPushStyle(int pushStyle) {
		this.pushStyle = pushStyle;
	}
	public String getBigIcon() {
		return bigIcon;
	}
	public void setBigIcon(String bigIcon) {
		this.bigIcon = bigIcon;
	}
	public int getUseBtn() {
		return useBtn;
	}
	public void setUseBtn(int useBtn) {
		this.useBtn = useBtn;
	}
	public String getUnlikeText() {
		return unlikeText;
	}
	public void setUnlikeText(String unlikeText) {
		this.unlikeText = unlikeText;
	}
	public String getDownText() {
		return downText;
	}
	public void setDownText(String downText) {
		this.downText = downText;
	}
	public int getJumpType() {
		return jumpType;
	}
	public void setJumpType(int jumpType) {
		this.jumpType = jumpType;
	}
	public String getDownUrl() {
		return downUrl;
	}
	public void setDownUrl(String downUrl) {
		this.downUrl = downUrl;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getLinkResourceId() {
		return linkResourceId;
	}
	public void setLinkResourceId(String linkResourceId) {
		this.linkResourceId = linkResourceId;
	}
	public int getIntType() {
		return intType;
	}
	public void setIntType(int intType) {
		this.intType = intType;
	}
	public int getNetType() {
		return netType;
	}
	public void setNetType(int netType) {
		this.netType = netType;
	}
	public int getDesktopType() {
		return desktopType;
	}
	public void setDesktopType(int desktopType) {
		this.desktopType = desktopType;
	}
	public long getLongStartTime() {
		return longStartTime;
	}
	public void setLongStartTime(long longStartTime) {
		this.longStartTime = longStartTime;
	}
	public long getLongExpirTime() {
		return longExpirTime;
	}
	public void setLongExpirTime(long longExpirTime) {
		this.longExpirTime = longExpirTime;
	}
	public String getShowStartTime() {
		return showStartTime;
	}
	public void setShowStartTime(String showStartTime) {
		this.showStartTime = showStartTime;
	}
	public String getShowEndTime() {
		return showEndTime;
	}
	public void setShowEndTime(String showEndTime) {
		this.showEndTime = showEndTime;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	@Override
	public String toString() {
		return "Push [strId=" + strId + ", title=" + title + ", subtitle="
				+ subtitle + ", icon=" + icon + ", pushStyle=" + pushStyle
				+ ", bigIcon=" + bigIcon + ", useBtn=" + useBtn
				+ ", unlikeText=" + unlikeText + ", downText=" + downText
				+ ", jumpType=" + jumpType + ", downUrl=" + downUrl
				+ ", packageName=" + packageName + ", linkResourceId="
				+ linkResourceId + ", intType=" + intType + ", netType="
				+ netType + ", desktopType=" + desktopType + ", longStartTime="
				+ longStartTime + ", longExpirTime=" + longExpirTime
				+ ", showStartTime=" + showStartTime + ", showEndTime="
				+ showEndTime + "]";
	}
	
}
