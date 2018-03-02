package com.nqmobile.livesdk.modules.locker;

import java.io.Serializable;
import java.util.List;

public class Locker implements Serializable{
	private static final long serialVersionUID = -4087304886739509942L;
	/** strId */
	private String strId;
	/** 功能模块来源编号：0：store列表；1：banner；2每日推荐 */
	private int intSourceType;
	/** 名称 */
	private String strName;
	/** 下载url */
	private String strLockerUrl;
	/** 预览图下载url */
    private String strPreviewUrl;
    /** 图标下载url */
	private String strIconUrl;
	/** 每日推荐图url */
    private String strDailyIconUrl;
    /** 大小 */
	private long longSize;
	/** 下载数 */
	private long longDownloadCount;
	/** 作者 */
    private String strAuthor;
    /** 最低支持引擎版本 */
    private int intMinVersion;
	/** 引擎类型（界面上是’来源’） 0—自有锁屏 1—拉风锁屏 */
	private int intEngineType;
	/** 图标本地文件路径 */
    private String strIconPath;
	/** 预览图本地文件路径 */
    private String strPreviewPath;
    /** 锁屏本地文件路径 */
    private String strLockerPath;
    /** 服务器端的更新时间 */
	private long longUpdateTime;
	/** 本地缓存时间 */
	private long longLocalTime;
	public Locker() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getStrId() {
		return strId;
	}

	public void setStrId(String strId) {
		this.strId = strId;
	}

	public int getIntSourceType() {
		return intSourceType;
	}

	public void setIntSourceType(int intSourceType) {
		this.intSourceType = intSourceType;
	}

	public String getStrName() {
		return strName;
	}

	public void setStrName(String strName) {
		this.strName = strName;
	}

	public String getStrLockerUrl() {
		return strLockerUrl;
	}

	public void setStrLockerUrl(String strLockerUrl) {
		this.strLockerUrl = strLockerUrl;
	}

	public String getStrPreviewUrl() {
		return strPreviewUrl;
	}

	public void setStrPreviewUrl(String strPreviewUrl) {
		this.strPreviewUrl = strPreviewUrl;
	}

	public String getStrIconUrl() {
		return strIconUrl;
	}

	public void setStrIconUrl(String strIconUrl) {
		this.strIconUrl = strIconUrl;
	}

	public String getStrDailyIconUrl() {
		return strDailyIconUrl;
	}

	public void setStrDailyIconUrl(String strDailyIconUrl) {
		this.strDailyIconUrl = strDailyIconUrl;
	}

	public long getLongSize() {
		return longSize;
	}

	public void setLongSize(long longSize) {
		this.longSize = longSize;
	}

	public long getLongDownloadCount() {
		return longDownloadCount;
	}

	public void setLongDownloadCount(long longDownloadCount) {
		this.longDownloadCount = longDownloadCount;
	}

	public String getStrAuthor() {
		return strAuthor;
	}

	public void setStrAuthor(String strAuthor) {
		this.strAuthor = strAuthor;
	}

	public int getIntMinVersion() {
		return intMinVersion;
	}

	public void setIntMinVersion(int intMinVersion) {
		this.intMinVersion = intMinVersion;
	}

	public int getIntEngineType() {
		return intEngineType;
	}

	public void setIntEngineType(int intEngineType) {
		this.intEngineType = intEngineType;
	}

	public String getStrIconPath() {
		return strIconPath;
	}

	public void setStrIconPath(String strIconPath) {
		this.strIconPath = strIconPath;
	}

	public String getStrPreviewPath() {
		return strPreviewPath;
	}

	public void setStrPreviewPath(String strPreviewPath) {
		this.strPreviewPath = strPreviewPath;
	}

	public String getStrLockerPath() {
		return strLockerPath;
	}

	public void setStrLockerPath(String strLockerPath) {
		this.strLockerPath = strLockerPath;
	}

	public long getLongUpdateTime() {
		return longUpdateTime;
	}
	
	public void setLongUpdateTime(long longUpdateTime) {
		this.longUpdateTime = longUpdateTime;
	}
	
	public long getLongLocalTime() {
		return longLocalTime;
	}
	
	public void setLongLocalTime(long longLocalTime) {
		this.longLocalTime = longLocalTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((strId == null) ? 0 : strId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Locker other = (Locker) obj;
		if (strId == null) {
			if (other.strId != null)
				return false;
		} else if (!strId.equals(other.strId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Locker [Id=" + strId + " ,SourceType=" + intSourceType
				+ " ,Name=" + strName + " ,LockerUrl=" + strLockerUrl
				+ " ,Preview=" + strPreviewUrl + " ,Icon="
				+ strIconUrl + " ,DailyIcon=" + strDailyIconUrl
				+ " ,Size=" + longSize + " ,DownCount="
				+ longDownloadCount + " ,Author=" + strAuthor
				+ " ,MinVersion=" + intMinVersion + " ,EngineType="
				+ intEngineType +  " ,LockerPath="
				+ strLockerPath + "]";
	}
	
	
}
