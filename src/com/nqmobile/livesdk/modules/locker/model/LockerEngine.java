package com.nqmobile.livesdk.modules.locker.model;

import java.io.Serializable;

public class LockerEngine implements Serializable {
	private static final long serialVersionUID = 4952397364311427551L;
	
	public static final int LIB_ENGINE_LD = 0;
    public static final int LIB_ENGINE_LF = 1;
	
	/**类型ID*/
	private int type;
	/**版本*/
	private int version;
	/**版本名*/
	private String versionName;
	/**引擎大小*/
	private long size;
	/**引擎文件路径*/
	private String path;
	
	private String downUrl;
	private String md5;

	public LockerEngine() {
		super();
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getDownUrl() {
		return downUrl;
	}

	public void setDownUrl(String downUrl) {
		this.downUrl = downUrl;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}
	
	@Override
	public String toString() {
		return "LockerEngine [type=" + type + ", version=" + version
				+ ", versionName=" + versionName + ", size=" + size + ", path="
				+ path + "]";
	}

}
