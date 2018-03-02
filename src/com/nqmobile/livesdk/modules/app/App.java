package com.nqmobile.livesdk.modules.app;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;

import com.nq.interfaces.launcher.TAppResource;
import com.nqmobile.livesdk.commons.modal.ResItem;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.StringUtil;

/**
 * 应用，包括普通应用和游戏。
 * @author changxiaofei
 * @time 2013-11-14 下午5:30:46
 */
public class App extends ResItem{
	private static final long serialVersionUID = 6669572537773853594L;
    /**
     * 如下常量定义来自服务器 Launcher服务的TAppResource:
     * clickActionType, #0打开详情页，1打开GP， 3打开浏览器（外部网站或广告GP下载）， 9无行为
     */
    public static final int CLICK_ACTION_TYPE_NONE = 9;
    public static final int CLICK_ACTION_TYPE_GP = 1;
    public static final int CLICK_ACTION_TYPE_DIRECT = 0;
    public static final int CLICK_ACTION_TYPE_BROWSER = 3;
    
	/** strId */
	private String strId;
	/** 功能模块来源编号：0：store列表；1：banner；2每日推荐 */
	private int intSourceType;
    /** 类型定义 0应用推荐 1链接广告 100 装机必备软件 200 装机必备游戏*/
    private int type;
	/** 应用一级分类，如：效率、系统 */
	private String strCategory1;
	/** 应用 二级分类 */
	private String strCategory2;
	/** 名称 */
	private String strName;
	/** 描述 */
	private String strDescription;
	/** 开发商 */
	private String strDevelopers;
	/** 等级，星级 */
	private float floatRate;
	/** 下载数 */
	private long longDownloadCount;
	/** 应用大小 */
	private long longSize;
	/** 应用图标下载url */
	private String strIconUrl;
	/** banner和pop的广告图片 */
	private String strImageUrl;
	/** 应用下载url */
	private String strAppUrl;
	/** 应用资源点击的行为：0打开详情页，1打开GP， 3打开浏览器（外部网站或广告GP下载）， 9无行为 */
	private int intClickActionType;
	/** 详情页或应用简介中下载按钮的行为：1打开GP下载，2直接下载（本地下载），3打开浏览器（广告GP下载），9无行为 */
	private int intDownloadActionType;
	/** 应用预览图url */
	private List<String> arrPreviewUrl;
	///** 应用图标缓存文件路径 */
	//private String strIconCachePath;
	///** banner和pop的广告图片缓存文件路径 */
	//private String strImageCachePath;
	///** 应用预览图缓存文件路径 */
	//private List<String> arrPreviewCachePath;
	/** 应用图标本地文件路径 */
	private String strIconPath;
	/** banner和pop的广告图片本地文件路径 */
	private String strImagePath;
	/** 应用本地文件路径 */
	private String strAppPath;
	/** 应用预览图本地文件路径 */
	private List<String> arrPreviewPath;
	/** 包名 */
	private String strPackageName;
	/** 版本号 */
	private String strVersion;
	/** 服务器端的更新时间 */
	private long longUpdateTime;
	/** 本地缓存时间 */
	private long longLocalTime;
	/** 桌面展示次数 */
	private int showCount;
	/** 一句话简介 */
	private String strShortIntro;
	/** 应用音频url */
	private String strAudioUrl;
	/** 应用音频本地文件路径 */
	private String strAudioPath;
	/** 应用视频url */
	private String strVideoUrl;
	/** 应用视频本地文件路径 */
	private String strVideoPath;
	/** 特效 */
	private int intEffect;

    private int rewardPoints;
    private String trackId;

	private boolean isSlientDownload;

	public App() {
		super();
	}
	
	/**
	 * AppResource转换成App对象
	 * @param appResource
	 * @param context
	 */
	public App(TAppResource appResource, Context context) {
		super();
		if (appResource != null) {
            setStrId(appResource.getResourceId());
            setStrName(StringUtil.nullToEmpty(appResource.getName()));
            setStrPackageName(StringUtil.nullToEmpty(appResource.getPackageName()));
            setStrDescription(StringUtil.nullToEmpty(appResource.getDescription()));
            setStrDevelopers(StringUtil.nullToEmpty(appResource.getDevelopers()));
            setStrVersion(StringUtil.nullToEmpty(appResource.getVersion()));
            setLongUpdateTime(appResource.getUpdateTime());
            setLongSize(appResource.getSize());
            setStrCategory1(StringUtil.nullToEmpty(appResource.getClassification1()));
            setStrCategory2(StringUtil.nullToEmpty(appResource.getClassification2()));
            setStrIconUrl(StringUtil.nullToEmpty(appResource.getIcon()));
            setStrImageUrl(StringUtil.nullToEmpty(appResource.getImage()));
            setArrPreviewUrl(appResource.getPicture());
            setLongDownloadCount(appResource.getDownloadNum());
            setIntClickActionType(appResource.getClickActionType());
            setIntDownloadActionType(appResource.getDownActionType());
            setStrAppUrl(StringUtil.nullToEmpty(appResource.getLinkUrl()));
            setFloatRate((float) appResource.getScore());
            setType(appResource.getType());
            setRewardPoints(appResource.getRewardPoints());
            setTrackId(appResource.getTrackId());
            String SDCardPath = CommonMethod.getSDcardPath(context);
			setSlientDownload(appResource.isSilentDownload == 1);
            if(SDCardPath == null)
            	SDCardPath = CommonMethod.getSDcardPathFromPref(context);
            //icon本地路径
            if (!TextUtils.isEmpty(getStrIconUrl())) {
                String strImgPath = new StringBuilder().append(SDCardPath).append(AppConstant.STORE_IMAGE_LOCAL_PATH_APP).toString();
                setStrIconPath(new StringBuilder().append(strImgPath).append(getStrId()).append("_icon")
                        .append(StringUtil.getExt(getStrIconUrl())).toString());
            }
            //image本地路径
            if (!TextUtils.isEmpty(getStrImageUrl())) {
                String strImgPath = new StringBuilder().append(SDCardPath).append(AppConstant.STORE_IMAGE_LOCAL_PATH_APP).toString();
                setStrImagePath(new StringBuilder().append(strImgPath).append(getStrId()).append("_imag")
                        .append(StringUtil.getExt(getStrImageUrl())).toString());
            }
            //apk本地路径
            if (!TextUtils.isEmpty(getStrAppUrl())) {
                String strAppPath = new StringBuilder().append(SDCardPath).append(AppConstant.STORE_IMAGE_LOCAL_PATH_APP).toString();
                setStrAppPath(new StringBuilder().append(strAppPath).append(getStrId())
                        .append(".apk").toString());
            }
            //预览图缓存、本地路径
            List<String> arrPreviewUrl = getArrPreviewUrl();
            if (arrPreviewUrl != null && arrPreviewUrl.size() > 0) {
                List<String> arrPreviewPath = new ArrayList<String>();
                String strPreviewPath = new StringBuilder().append(SDCardPath).append(AppConstant.STORE_IMAGE_LOCAL_PATH_APP).toString();
                for (int i = 0; i < arrPreviewUrl.size(); i++) {
                    if (!TextUtils.isEmpty(arrPreviewUrl.get(i))) {
                        arrPreviewPath.add(new StringBuilder().append(strPreviewPath).append(getStrId()).append("_preview").append(i)
                                .append(StringUtil.getExt(arrPreviewUrl.get(i))).toString());
                    }
                }
                setArrPreviewPath(arrPreviewPath);
            }
            //setLongLocalTime(longLocalTime);//这个在保存的时候去客户端时间
            setStrShortIntro(StringUtil.nullToEmpty(appResource.getIntroduction()));
            setStrAudioUrl(appResource.getAudio());
            setStrVideoUrl(appResource.getVideo());
            // 音频缓存、本地路径
            if (!TextUtils.isEmpty(getStrAudioUrl())) {
                String strAudioPath = new StringBuilder().append(SDCardPath).append(AppConstant.STORE_IMAGE_LOCAL_PATH_APP).toString();
                setStrAudioPath(new StringBuilder().append(strAudioPath).append(getStrId())
                        .append(StringUtil.getExt(getStrAudioUrl())).toString());
            }
            // 视频缓存、本地路径
            if (!TextUtils.isEmpty(getStrVideoUrl())) {
                String strVideoPath = new StringBuilder().append(SDCardPath).append(AppConstant.STORE_IMAGE_LOCAL_PATH_APP).toString();
                setStrVideoPath(new StringBuilder().append(strVideoPath).append(getStrId())
                        .append(StringUtil.getExt(getStrVideoUrl())).toString());
            }
            setIntEffect(appResource.getEffectId());
        }
	}

	public String getStrId() {
		return strId;
	}
	
	public void setStrId(String strId) {
		this.strId = strId;
	}

	public String getStrCategory1() {
		return strCategory1;
	}

	public void setStrCategory1(String strCategory1) {
		this.strCategory1 = strCategory1;
	}

	public String getStrCategory2() {
		return strCategory2;
	}

	public void setStrCategory2(String strCategory2) {
		this.strCategory2 = strCategory2;
	}

	public String getStrName() {
		return strName;
	}
	
	public void setStrName(String strName) {
		this.strName = strName;
	}
	
	public String getStrDescription() {
		return strDescription;
	}
	
	public void setStrDescription(String strDescription) {
		this.strDescription = strDescription;
	}
	
	public String getStrDevelopers() {
		return strDevelopers;
	}

	public void setStrDevelopers(String strDevelopers) {
		this.strDevelopers = strDevelopers;
	}
	
	public float getFloatRate() {
		return floatRate;
	}
	
	public void setFloatRate(float floatRate) {
		this.floatRate = floatRate;
	}
	
	public long getLongDownloadCount() {
		return longDownloadCount;
	}

	public void setLongDownloadCount(long longDownloadCount) {
		this.longDownloadCount = longDownloadCount;
	}
	
	public int getIntClickActionType() {
		return intClickActionType;
	}

	public void setIntClickActionType(int intClickActionType) {
		this.intClickActionType = intClickActionType;
	}

	public int getIntDownloadActionType() {
		return intDownloadActionType;
	}

	public void setIntDownloadActionType(int intDownloadWay) {
		this.intDownloadActionType = intDownloadWay;
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
	
	public String getStrAppUrl() {
		return strAppUrl;
	}
	
	public void setStrAppUrl(String strAppUrl) {
		this.strAppUrl = strAppUrl;
	}

	public List<String> getArrPreviewUrl() {
		return arrPreviewUrl;
	}
	
	public void setArrPreviewUrl(List<String> arrPreviewUrl) {
		this.arrPreviewUrl = arrPreviewUrl;
	}
	
	public String getStrPackageName() {
		return strPackageName;
	}
	
	public void setStrPackageName(String strPackageName) {
		this.strPackageName = strPackageName;
	}
	
	public String getStrVersion() {
		return strVersion;
	}
	
	public void setStrVersion(String strVersion) {
		this.strVersion = strVersion;
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

    public int getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(int rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    @Override
	public String toString() {
		/*return "App [strId=" + strId + ", intSourceType=" + intSourceType
				+ ", strCategory1=" + strCategory1 + ", strCategory2="
				+ strCategory2 + ", strName=" + strName + ", strDescription="
				+ strDescription + ", strDevelopers=" + strDevelopers
				+ ", floatRate=" + floatRate + ", longDownloadCount="
				+ longDownloadCount + ", longSize=" + longSize
				+ ", strIconUrl=" + strIconUrl + ", strImageUrl=" + strImageUrl
				+ ", strAppUrl=" + strAppUrl + ", intClickActionType="
				+ intClickActionType + ", intDownloadActionType="
				+ intDownloadActionType + ", arrPreviewUrl=" + arrPreviewUrl
				+ ", strIconPath=" + strIconPath + ", strImagePath="
				+ strImagePath + ", strAppPath=" + strAppPath
				+ ", arrPreviewPath=" + arrPreviewPath + ", strPackageName="
				+ strPackageName + ", strVersion=" + strVersion
				+ ", longUpdateTime=" + longUpdateTime + ", longLocalTime="
				+ longLocalTime + "]";*/
    	return "App [id=" + strId + ",SourceType=" + intSourceType
				+ ",Cate=" + strCategory1 + ","
				+ ", Click="
				+ intClickActionType
				+ ", DownType="
				+ intDownloadActionType 
				+ strCategory2 + ",Name=" + strName
				+ ", 包名=" + strPackageName + "]";
//		return "App [id=" + strId + ",SourceType=" + intSourceType
//				+ ",Cate1=" + strCategory1 + ",Cate2="
//				+ strCategory2 + ",Name=" + strName + ", strDescription="
//				+ ss(strDescription) + ", DownCount="
//				+ longDownloadCount + ",size=" + StringUtil.formatSize(longSize)
//				+ ", Icon=" + strIconUrl 
//				+ ", App=" + strAppUrl + ", Click="
//				+ intClickActionType + ", DownType="
//				+ intDownloadActionType + ", AppPath=" + strAppPath
//				+ ", strPackageName=" + strPackageName
//				+ ", rewardpoints=" + rewardPoints + ", audio=" + strAudioUrl
//				+ ", audioPath=" + strAudioPath + "]";
	}

	private String ss(String strDescription2) {
		// TODO Auto-generated method stub
		String s = StringUtil.nullToEmpty(strDescription2);
		int l = 0;
		if(s!=null){
			l = s.length() > 10 ? 10 : s.length();
		}
		return s.substring(0,l);
	}

	public String getStrIconPath() {
		return strIconPath;
	}

	public void setStrIconPath(String strIconPath) {
		this.strIconPath = strIconPath;
	}

	public String getStrAppPath() {
		return strAppPath;
	}

	public void setStrAppPath(String strAppPath) {
		this.strAppPath = strAppPath;
	}

	public List<String> getArrPreviewPath() {
		return arrPreviewPath;
	}

	public void setArrPreviewPath(List<String> arrPreviewPath) {
		this.arrPreviewPath = arrPreviewPath;
	}

	public String getStrImageUrl() {
		return strImageUrl;
	}

	public void setStrImageUrl(String strImageUrl) {
		this.strImageUrl = strImageUrl;
	}

	public String getStrImagePath() {
		return strImagePath;
	}

	public void setStrImagePath(String strImagePath) {
		this.strImagePath = strImagePath;
	}

	public int getIntSourceType() {
		return intSourceType;
	}

	public void setIntSourceType(int intSourceType) {
		this.intSourceType = intSourceType;
	}

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

	public String getStrShortIntro() {
		return strShortIntro;
	}

	public void setStrShortIntro(String strShortIntro) {
		this.strShortIntro = strShortIntro;
	}

	public String getStrAudioUrl() {
		return strAudioUrl;
	}

	public void setStrAudioUrl(String strAudioUrl) {
		this.strAudioUrl = strAudioUrl;
	}

	public String getStrAudioPath() {
		return strAudioPath;
	}

	public void setStrAudioPath(String strAudioPath) {
		this.strAudioPath = strAudioPath;
	}

	public String getStrVideoUrl() {
		return strVideoUrl;
	}

	public void setStrVideoUrl(String strVideoUrl) {
		this.strVideoUrl = strVideoUrl;
	}

	public String getStrVideoPath() {
		return strVideoPath;
	}

	public void setStrVideoPath(String strVideoPath) {
		this.strVideoPath = strVideoPath;
	}

	public int getShowCount() {
		return showCount;
	}

	public void setShowCount(int showCount) {
		this.showCount = showCount;
	}

	public int getIntEffect() {
		return intEffect;
	}

	public void setIntEffect(int intEffect) {
		this.intEffect = intEffect;
	}
	
	public boolean isGpApp() {
		return (intClickActionType == CLICK_ACTION_TYPE_GP ||
				intClickActionType == CLICK_ACTION_TYPE_BROWSER);
	}

	public boolean isSlientDownload() {
		return isSlientDownload;
	}

	public void setSlientDownload(boolean isSlientDownload) {
		this.isSlientDownload = isSlientDownload;
	}
}
