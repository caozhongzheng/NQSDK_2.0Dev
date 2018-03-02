package com.nqmobile.livesdk.modules.theme;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;

import com.nq.interfaces.launcher.TThemeResource;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.StringUtil;

/**
 * 主题
 * @author changxiaofei
 * @time 2013-11-14 下午5:30:46
 */
public class Theme implements Serializable {
	private static final long serialVersionUID = 7741080483467466968L;
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
     * 主题图标下载url
     */
    private String strIconUrl;
    /**
     * 主题预览图下载url
     */
    private List<String> arrPreviewUrl;
    /**
     * 主题下载url
     */
    private String strThemeUrl;
    /**
     * 主题图标本地文件路径
     */
    private String strIconPath;
    /**
     * 主题预览图本地文件路径
     */
    private List<String> arrPreviewPath;
    /**
     * 主题本地文件路径
     */
    private String strThemePath;
    /**
     * 服务器端的更新时间
     */
    private long longUpdateTime;
    /**
     * 本地缓存时间
     */
    private long longLocalTime;
    /**
     * 每日推荐icon
     */
    private String dailyIcon;

    private int consumePoints;
    private int pointsflag;
    
    public Theme(){
    }

    public Theme(TThemeResource themeResource, Context context){
    	if (themeResource != null) {
            setStrId(StringUtil.nullToEmpty(themeResource.getResourceId()));
            setStrName(StringUtil.nullToEmpty(StringUtil.nullToEmpty(themeResource.getName())));
            setStrAuthor(StringUtil.nullToEmpty(StringUtil.nullToEmpty(themeResource.getAuthor())));
            setStrVersion(StringUtil.nullToEmpty(StringUtil.nullToEmpty(themeResource.getVersion())));
            setStrSource(StringUtil.nullToEmpty(StringUtil.nullToEmpty(themeResource.getSource())));
            setLongSize(themeResource.getSize());
            setLongDownloadCount(themeResource.getDownloadNum());
            setStrIconUrl(StringUtil.nullToEmpty(StringUtil.nullToEmpty(themeResource.getIcon())));
            setArrPreviewUrl(themeResource.getPicture());
            setStrThemeUrl(StringUtil.nullToEmpty(StringUtil.nullToEmpty(themeResource.getDownloadUrl())));
            setDailyIcon(StringUtil.nullToEmpty(StringUtil.nullToEmpty(themeResource.getDailyIcon())));
            setConsumePoints(themeResource.getConsumePoints());
            setPointsflag(themeResource.getPointsFlag());
            String SDCardPath = CommonMethod.getSDcardPath(context);
            if(SDCardPath == null)
            	SDCardPath = CommonMethod.getSDcardPathFromPref(context);
            //icon本地路径
            if (!TextUtils.isEmpty(getStrIconUrl())) {
                String strImgPath = new StringBuilder().append(SDCardPath).append(ThemeConstants.STORE_IMAGE_LOCAL_PATH_THEME).toString();
                setStrIconPath(new StringBuilder().append(strImgPath).append(getStrId()).append("_icon")
                        .append(StringUtil.getExt(getStrIconUrl())).toString());
            }
            //theme本地路径
            if (!TextUtils.isEmpty(getStrThemeUrl())) {
                String strPath = new StringBuilder().append(SDCardPath).append(ThemeConstants.STORE_IMAGE_LOCAL_PATH_THEME).toString();
                setStrThemePath(new StringBuilder().append(strPath).append(getStrId())
                        .append(getStrThemeUrl().substring(getStrThemeUrl().lastIndexOf("."))).toString());
            }
            //预览图缓存、本地路径
            List<String> arrPreviewUrl = getArrPreviewUrl();
            if (arrPreviewUrl != null && arrPreviewUrl.size() > 0) {
                List<String> arrPreviewPath = new ArrayList<String>();
                String strPreviewPath = new StringBuilder().append(SDCardPath).append(ThemeConstants.STORE_IMAGE_LOCAL_PATH_THEME).toString();
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
        return strIconUrl;
    }

    public void setStrIconUrl(String strIconUrl) {
        this.strIconUrl = strIconUrl;
    }

    public List<String> getArrPreviewUrl() {
        return arrPreviewUrl;
    }

    public void setArrPreviewUrl(List<String> arrPreviewUrl) {
        this.arrPreviewUrl = arrPreviewUrl;
    }

    public String getStrThemeUrl() {
        return strThemeUrl;
    }

    public void setStrThemeUrl(String strThemeUrl) {
        this.strThemeUrl = strThemeUrl;
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

    public String getStrThemePath() {
        return strThemePath;
    }

    public void setStrThemePath(String strThemePath) {
        this.strThemePath = strThemePath;
    }

    public int getIntSourceType() {
        return intSourceType;
    }

    public void setIntSourceType(int intSourceType) {
        this.intSourceType = intSourceType;
    }

    public String getDailyIcon() {
        return dailyIcon;
    }

    public void setDailyIcon(String dailyIcon) {
        this.dailyIcon = dailyIcon;
    }

    public int getConsumePoints() {
        return consumePoints;
    }

    public void setConsumePoints(int consumePoints) {
        this.consumePoints = consumePoints;
    }

    public int getPointsflag() {
        return pointsflag;
    }

    public void setPointsflag(int pointsflag) {
        this.pointsflag = pointsflag;
    }

	@Override
	public String toString() {
		return "Theme [strId=" + strId + ", intSourceType=" + intSourceType
				+ ", strName=" + strName + ", strAuthor=" + strAuthor
				+ ", strVersion=" + strVersion + ", strSource=" + strSource
				+ ", longSize=" + longSize + ", longDownloadCount="
				+ longDownloadCount + ", strIconUrl=" + strIconUrl
				+ ", arrPreviewUrl=" + arrPreviewUrl + ", strThemeUrl="
				+ strThemeUrl + ", strIconPath=" + strIconPath
				+ ", arrPreviewPath=" + arrPreviewPath + ", strThemePath="
				+ strThemePath + ", longUpdateTime=" + longUpdateTime
				+ ", longLocalTime=" + longLocalTime + ", dailyIcon="
				+ dailyIcon + ", consumePoints=" + consumePoints
				+ ", pointsflag=" + pointsflag + "]";
	}
}