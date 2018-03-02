package com.nqmobile.livesdk.modules.daily;

import java.io.Serializable;

import com.nq.interfaces.launcher.TWebResource;
import com.nqmobile.livesdk.utils.StringUtil;

public class Web implements Serializable{
	private static final long serialVersionUID = 7380864849213250934L;
	/** strId */
	private String strId;
	/** 名称 */
	private String strName;
	/** 预览图下载url */
    private String strPreviewUrl;
    /** 图标下载url */
	private String strIconUrl;
	/** 每日推荐图url */
    private String strDailyIconUrl;
	/** 点击后跳转类型 0—打开浏览器 1—打开webview */
	private int intJumpType;
	/** 跳转url */
	private String strLinkUrl;
	
	public Web() {
		super();
	}
	
	public Web(TWebResource webResource){
		if (webResource != null) {
            setStrId(StringUtil.nullToEmpty(webResource.getId()));
            setStrName(StringUtil.nullToEmpty(webResource.getName()));
            setStrPreviewUrl(StringUtil.nullToEmpty(webResource.getPreview()));
            setStrIconUrl(StringUtil.nullToEmpty(webResource.getIcon()));
            setStrDailyIconUrl(StringUtil.nullToEmpty(webResource.getDailyIcon()));
            setIntJumpType(webResource.getJumpType());
            setStrLinkUrl(StringUtil.nullToEmpty(webResource.getLinkUrl()));
        }
	}

	public String getStrId() {
		return strId;
	}

	public void setStrId(String strId) {
		this.strId = strId;
	}

	public String getStrName() {
		return strName;
	}

	public void setStrName(String strName) {
		this.strName = strName;
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

	public int getIntJumpType() {
		return intJumpType;
	}

	public void setIntJumpType(int intJumpType) {
		this.intJumpType = intJumpType;
	}

	public String getStrLinkUrl() {
		return strLinkUrl;
	}

	public void setStrLinkUrl(String strLinkUrl) {
		this.strLinkUrl = strLinkUrl;
	}

	@Override
	public String toString() {
		return "Web [Id=" + strId + ",Name=" + strName
				+ ",PreviewUrl=" + strPreviewUrl + ",IconUrl="
				+ strIconUrl + ",DailyIconUrl=" + strDailyIconUrl
				+ ",JumpType=" + intJumpType + ",LinkUrl=" + strLinkUrl
				+ "]";
	}
	
}
