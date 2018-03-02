package com.nqmobile.livesdk.modules.browserbandge.model;

import java.io.Serializable;

import com.nq.interfaces.launcher.TBandge;

/**
 * 浏览器角标广告
 * 
 * @author caozhongzheng
 * @time 2014/5/22 15:10:05
 */
public class Bandge implements Serializable {

	private static final long serialVersionUID = -7607180563168918232L;
	private String strId;
	/** 跳转地址 */
	private String jumpUrl;
	/** 角标广告生效时间 */
	private long longStartTime;
	/** 角标广告失效时间 */
	private long longExpirTime;
	/** 角标广告类型 0：一次 ，1：常驻 */
	private int type;
	
	public static final int TYPE_ONE_TIME = 0;
	public static final int TYPE_LONG = 1;	

	public Bandge() {
		super();
	}

	public Bandge(TBandge tb) {
		this.jumpUrl = tb.jumpUrl;
		this.longExpirTime = tb.expirTime;
		this.longStartTime = tb.startTime;
		this.strId = tb.resourceId;
		this.type = tb.cornerType;

	}

	public String getStrId() {
		return strId;
	}

	public void setStrId(String strId) {
		this.strId = strId;
	}

	public String getJumpUrl() {
		return jumpUrl;
	}

	public void setJumpUrl(String jumpUrl) {
		this.jumpUrl = jumpUrl;
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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Bandge [strId=" + strId + ", jumpUrl=" + jumpUrl
				+ ", longStartTime=" + longStartTime + ", longExpirTime="
				+ longExpirTime + ",type=" + type + "]";
	}

}
