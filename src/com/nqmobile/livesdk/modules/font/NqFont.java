package com.nqmobile.livesdk.modules.font;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.nq.interfaces.launcher.TThemeResource;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.StringUtil;
import com.xinmei365.fontsdk.bean.Font;

public class NqFont implements Serializable {
	private static final long serialVersionUID = 7741080483467466969L;
	/**
     * strId
     */
    private String strId;
    /**
     * 功能模块来源编号：0：store列表；1：banner；2每日推荐
     */
    private int intSourceType;
    /**
     * 名称
     */
    private String strName;
    /**
     * 作者
     */
    private String strAuthor;
    /**
     * 版本号
     */
    private String strVersion;
    /**
     * 来源
     */
    private String strSource;
    /**
     * 大小
     */
    private long longSize;
    /**
     * 下载数
     */
    private long longDownloadCount;
    /**
     * 字体预览下载url
     */
    private String thumbnailUrl;
    /**
     * 主题预览图下载url
     */
    private List<String> arrPreviewUrl;
    /**
     * 字体下载url
     */
    private String strFontUrl;
    /**
     * 字体图标本地文件路径
     */
    private String strIconPath;
    /**
     * 字体预览图本地文件路径
     */
    private List<String> arrPreviewPath;
    /**
     * 字体本地文件路径
     */
    private String strFontPath;
    /**
     * 服务器端的更新时间
     */
    private long longUpdateTime;
    /**
     * 本地缓存时间
     */
    private long longLocalTime;
    
    /**
     * 本地缓存Font对象
     */    
    private Font mFont;
    
    /**
     * 是否最新
     */
    private boolean isNew;
    
    /**
     * 是否最热
     */
    private boolean isHot;
    
    /**
     * 字体描述
     */
    private String desc;
    
    public NqFont(){
    }
    
    /**
     * 把字体管家的Font对象转为Nq字体对象
     * @param fontResource
     * @param context
     */
    public NqFont(Font fontResource, Context context){
    	if (fontResource != null) {
    		setFont(fontResource);
            setStrId(StringUtil.nullToEmpty("FT" + fontResource.getFontId()));
            setStrName(StringUtil.nullToEmpty(StringUtil.nullToEmpty(fontResource.getFontName())));
            setStrAuthor(StringUtil.nullToEmpty(StringUtil.nullToEmpty(fontResource.getUserName())));
            setStrVersion(StringUtil.nullToEmpty(StringUtil.nullToEmpty(fontResource.getVersioncode())));
            setLongSize(fontResource.getFontSize());
            setLongDownloadCount(fontResource.getFontDownloadCount());
            setStrIconUrl(StringUtil.nullToEmpty(StringUtil.nullToEmpty(fontResource.getThumbnailUrl())));
            setStrFontUrl(StringUtil.nullToEmpty(StringUtil.nullToEmpty(fontResource.getDownloadUr())));

            String[] preview = fontResource.getFontPreviewImg();
            if (preview != null && preview.length >= 0) {
                setArrPreviewUrl(java.util.Arrays.asList(preview));
            }
            
            String SDCardPath = CommonMethod.getSDcardPath(context);
            if(SDCardPath == null)
            	SDCardPath = CommonMethod.getSDcardPathFromPref(context);

            //font本地路径
            if (!TextUtils.isEmpty(getStrFontUrl())) {
                String strPath = new StringBuilder().append(SDCardPath).append(FontConstants.STORE_IMAGE_LOCAL_PATH_FONT).toString();
                setStrFontPath(new StringBuilder().append(strPath).append(getStrId())
                        .append(getStrFontUrl().substring(getStrFontUrl().lastIndexOf("."))).toString());
            }
            //预览图缓存、本地路径
            List<String> arrPreviewUrl = getArrPreviewUrl();
            if (arrPreviewUrl != null && arrPreviewUrl.size() > 0) {
                List<String> arrPreviewPath = new ArrayList<String>();
                String strPreviewPath = new StringBuilder().append(SDCardPath).append(FontConstants.STORE_IMAGE_LOCAL_PATH_FONT).toString();
                for (int i = 0; i < arrPreviewUrl.size(); i++) {
                    if (!TextUtils.isEmpty(arrPreviewUrl.get(i))) {
                        arrPreviewPath.add(new StringBuilder().append(strPreviewPath).append(getStrId()).append("_preview").append(i)
                                .append(StringUtil.getExt(arrPreviewUrl.get(i))).toString());
                    }
                }
                setArrPreviewPath(arrPreviewPath);
            }
            //setLongLocalTime(longLocalTime);//这个在保存的时候去客户端时间
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

    public String getStrAuthor() {
        return strAuthor;
    }

    public void setStrAuthor(String strAuthor) {
        this.strAuthor = strAuthor;
    }

    public String getStrVersion() {
        return strVersion;
    }

    public void setStrVersion(String strVersion) {
        this.strVersion = strVersion;
    }

    public String getStrSource() {
        return strSource;
    }

    public void setStrSource(String strSource) {
        this.strSource = strSource;
    }

    public long getLongSize() {
        return longSize;
    }

    public void setLongSize(long longSize) {
        this.longSize = longSize;
    }

    public String getStrIconUrl() {
        return thumbnailUrl;
    }

    public void setStrIconUrl(String strIconUrl) {
        this.thumbnailUrl = strIconUrl;
    }

    public List<String> getArrPreviewUrl() {
        return arrPreviewUrl;
    }

    public void setArrPreviewUrl(List<String> arrPreviewUrl) {
        this.arrPreviewUrl = arrPreviewUrl;
    }

    public String getStrFontUrl() {
        return strFontUrl;
    }

    public void setStrFontUrl(String strFontUrl) {
        this.strFontUrl = strFontUrl;
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

    public long getLongDownloadCount() {
        return longDownloadCount;
    }

    public void setLongDownloadCount(long longDownloadCount) {
        this.longDownloadCount = longDownloadCount;
    }

    public String getStrIconPath() {
        return strIconPath;
    }

    public void setStrIconPath(String strIconPath) {
        this.strIconPath = strIconPath;
    }

    public List<String> getArrPreviewPath() {
        return arrPreviewPath;
    }

    public void setArrPreviewPath(List<String> arrPreviewPath) {
        this.arrPreviewPath = arrPreviewPath;
    }

    public String getStrFontPath() {
        return strFontPath;
    }

    public void setStrFontPath(String strFontPath) {
        this.strFontPath = strFontPath;
    }

    public int getIntSourceType() {
        return intSourceType;
    }

    public void setIntSourceType(int intSourceType) {
        this.intSourceType = intSourceType;
    }

    public Font getFont() {
        return mFont;
    }

    public void setFont(Font font) {
        this.mFont = font;
    }

    public boolean isNew() {
        return isNew;
    }
    
    public void setIsNew(boolean value) {
        isNew = value;
    }
    
    public boolean isHot() {
        return isHot;
    }
    
    public void setIsHot(boolean value) {
    	isHot = value;
    }
    
    public String getDescription() {
        return desc;
    }
    
    public void setDescription(String value) {
    	desc = value;
    }
    
	@Override
	public String toString() {
		return "Font [strId=" + strId + ", intSourceType=" + intSourceType
				+ ", strName=" + strName + ", strAuthor=" + strAuthor
				+ ", strVersion=" + strVersion + ", strSource=" + strSource
				+ ", longSize=" + longSize + ", longDownloadCount="
				+ longDownloadCount + ", strIconUrl=" + thumbnailUrl
				+ ", arrPreviewUrl=" + arrPreviewUrl + ", strFontUrl="
				+ strFontUrl + ", strIconPath=" + strIconPath
				+ ", arrPreviewPath=" + arrPreviewPath + ", strFontPath="
				+ strFontPath + ", longUpdateTime=" + longUpdateTime
				+ ", longLocalTime=" + longLocalTime  + "]";
	}
}
