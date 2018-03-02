package com.nqmobile.livesdk.modules.banner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;

import com.nq.interfaces.launcher.TAppResource;
import com.nq.interfaces.launcher.TBannerResource;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.theme.Theme;
import com.nqmobile.livesdk.modules.theme.Topic;
import com.nqmobile.livesdk.modules.wallpaper.Wallpaper;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.StringUtil;
import com.nqmobile.livesdk.utils.Tools;

/**
 * 应用，包括普通应用和游戏。
 * @author changxiaofei
 * @time 2013-11-14 下午5:30:46
 */
public class Banner extends App implements Serializable{
	private static final ILogger NqLog = LoggerFactory.getLogger(BannerModule.MODULE_NAME);
	
	private static final long serialVersionUID = -6988911941842961189L;
	/** plate 板块编号： 0：应用、游戏；1：主题；2：壁纸。（用于banner区分板块，banner表专用）  */
	private List<Integer> intPlate;

	/** plate 板块编号： 0：应用、游戏；1：主题；2：壁纸。3专题  4广告平台广告（用于banner区分板块，banner表专用）  */
	private int newPlate;
	/** plate 板块来源编号： 0：主题；1：锁屏；2：壁纸；3：应用、游戏。（用于banner区分板块，banner表专用）  */
	private int fromPlate;
	/** 点击量 **/
	private int clickNum;
	/** 发布时间 */
	private long lastPublishTime;
	
	/** 应用 **/
	private App app;
	/** 主题 **/
	private Theme theme;
	/** 壁纸 **/
	private Wallpaper wallpaper;
	/** 专题 **/
	private Topic topic;
	
	public Banner(){}
	
	public Banner(TAppResource appResource, Context context){
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
            String SDCardPath = CommonMethod.getSDcardPath(context);
            if(SDCardPath == null)
            	SDCardPath = CommonMethod.getSDcardPathFromPref(context);
            //icon本地路径
            if (!TextUtils.isEmpty(getStrIconUrl())) {
                String strImgPath = new StringBuilder().append(SDCardPath).append(BannerConstants.STORE_IMAGE_LOCAL_PATH_BANNER).toString();
                setStrIconPath(new StringBuilder().append(strImgPath).append(getStrId()).append("_icon")
                        .append(getStrIconUrl().substring(getStrIconUrl().lastIndexOf("."))).toString());
            }
            //image本地路径
            if (!TextUtils.isEmpty(getStrImageUrl())) {
                String strImgPath = new StringBuilder().append(SDCardPath).append(BannerConstants.STORE_IMAGE_LOCAL_PATH_BANNER).toString();
                setStrImagePath(new StringBuilder().append(strImgPath).append(getStrId()).append("_imag")
                        .append(getStrImageUrl().substring(getStrImageUrl().lastIndexOf("."))).toString());
            }
            //apk本地路径
            if (!TextUtils.isEmpty(getStrAppUrl())) {
                String strAppPath = new StringBuilder().append(SDCardPath).append(BannerConstants.STORE_IMAGE_LOCAL_PATH_BANNER).toString();
                setStrAppPath(new StringBuilder().append(strAppPath).append(getStrId())
                        .append(".apk").toString());
            }
            //预览图缓存、本地路径
            List<String> arrPreviewUrl = getArrPreviewUrl();
            if (arrPreviewUrl != null && arrPreviewUrl.size() > 0) {
                List<String> arrPreviewPath = new ArrayList<String>();
                String strPreviewPath = new StringBuilder().append(SDCardPath).append(BannerConstants.STORE_IMAGE_LOCAL_PATH_BANNER).toString();
                for (int i = 0; i < arrPreviewUrl.size(); i++) {
                    if (!TextUtils.isEmpty(arrPreviewUrl.get(i))) {
                        arrPreviewPath.add(new StringBuilder().append(strPreviewPath).append(getStrId()).append("_preview").append(i)
                                .append(arrPreviewUrl.get(i).substring(arrPreviewUrl.get(i).lastIndexOf("."))).toString());
                    }
                }
                setArrPreviewPath(arrPreviewPath);
            }

            //banner新增
            setIntPlate(appResource.getPlate());
            //setLongLocalTime(longLocalTime);//这个在保存的时候去客户端时间
        }
	}
	
	/**
     * AppResource转换成Banner对象
     *
     * @param bannerResource
     * @return Banner
     */
    public Banner(TBannerResource bannerResource, Context context) {
    	if (bannerResource != null) {
    		/**
    		 * struct TBannerResource{
    			1:string resourceId,#banner资源ID
    			2:i32 type,#类型,0自有广告 1主题 2壁纸 3专题 4广告平台广告
    			3:TAppResource app,#应用
    			4:TWallpaperResource wallpaper,#壁纸
    			5:TThemeResource theme,#主题
    			6:TTopicResource topic,#专题
    			7:i64 lastPublishTime,#发布时间
    			8:string clickNum,#点击量
    			9:string picture,#banner图片
    		 */
    		int type = bannerResource.type;
    		setStrId(bannerResource.getResourceId());
    		setNewPlate(type);
    		setClickNum(Tools.isEmpty(bannerResource.clickNum) ? 0 : Integer.valueOf(bannerResource.clickNum));
    		setStrImageUrl(StringUtil.nullToEmpty(bannerResource.picture));
    		setLastPublishTime(bannerResource.lastPublishTime);
    		
    		if(type == 0 || type == 4){
    			App a = new App(bannerResource.app, context);
    			a.setIntSourceType(BannerConstants.STORE_MODULE_TYPE_BANNER);
    			setApp(a);
    		}else if(type == 1){
    			setTheme(new Theme(bannerResource.theme, context));
    		}else if(type == 2){
    			setWallpaper(new Wallpaper(bannerResource.wallpaper, context));
    		}else if(type == 3){
    			setTopic(new Topic(bannerResource.topic, context));
    		}
    		
    		String SDCardPath = CommonMethod.getSDcardPath(context);
    		if(SDCardPath == null)
    			SDCardPath = CommonMethod.getSDcardPathFromPref(context);
    		//icon本地路径
    		if (!TextUtils.isEmpty(getStrIconUrl())) {
    			String strImgPath = new StringBuilder().append(SDCardPath).append(BannerConstants.STORE_IMAGE_LOCAL_PATH_BANNER).toString();
    			setStrIconPath(new StringBuilder().append(strImgPath).append(getStrId()).append("_icon")
    					.append(getStrIconUrl().substring(getStrIconUrl().lastIndexOf("."))).toString());
    		}
    		//image本地路径
    		if (!TextUtils.isEmpty(getStrImageUrl())) {
    			String strImgPath = new StringBuilder().append(SDCardPath).append(BannerConstants.STORE_IMAGE_LOCAL_PATH_BANNER).toString();
    			setStrImagePath(new StringBuilder().append(strImgPath).append(getStrId()).append("_imag")
    					.append(getStrImageUrl().substring(getStrImageUrl().lastIndexOf("."))).toString());
    		}
    		//apk本地路径
    		if (!TextUtils.isEmpty(getStrAppUrl())) {
    			String strAppPath = new StringBuilder().append(SDCardPath).append(BannerConstants.STORE_IMAGE_LOCAL_PATH_BANNER).toString();
    			setStrAppPath(new StringBuilder().append(strAppPath).append(getStrId())
    					.append(getStrAppUrl().substring(getStrAppUrl().lastIndexOf("."))).toString());
    		}
    		//预览图缓存、本地路径
    		List<String> arrPreviewUrl = getArrPreviewUrl();
    		if (arrPreviewUrl != null && arrPreviewUrl.size() > 0) {
    			List<String> arrPreviewPath = new ArrayList<String>();
    			String strPreviewPath = new StringBuilder().append(SDCardPath).append(BannerConstants.STORE_IMAGE_LOCAL_PATH_BANNER).toString();
    			for (int i = 0; i < arrPreviewUrl.size(); i++) {
    				if (!TextUtils.isEmpty(arrPreviewUrl.get(i))) {
    					arrPreviewPath.add(new StringBuilder().append(strPreviewPath).append(getStrId()).append("_preview").append(i)
    							.append(arrPreviewUrl.get(i).substring(arrPreviewUrl.get(i).lastIndexOf("."))).toString());
    				}
    			}
    			setArrPreviewPath(arrPreviewPath);
    		}
    	}
    	NqLog.d("Banner " + this);
    }
	
	/** plate 板块编号： 0：应用、游戏；1：主题；2：壁纸。3专题  4广告平台广告（用于banner区分板块，banner表专用）  */
	public int getNewPlate() {
		return newPlate;
	}
	public void setNewPlate(int newPlate) {
		this.newPlate = newPlate;
	}
	public int getClickNum() {
		return clickNum;
	}
	public void setClickNum(int clickNum) {
		this.clickNum = clickNum;
	}
	public App getApp() {
		return app;
	}
	public void setApp(App app) {
		this.app = app;
	}
	public Theme getTheme() {
		return theme;
	}
	public void setTheme(Theme theme) {
		this.theme = theme;
	}
	public Wallpaper getWallpaper() {
		return wallpaper;
	}
	public void setWallpaper(Wallpaper wallpaper) {
		this.wallpaper = wallpaper;
	}
	public Topic getTopic() {
		return topic;
	}
	public void setTopic(Topic topic) {
		this.topic = topic;
	}
	
	public long getLastPublishTime() {
		return lastPublishTime;
	}
	public void setLastPublishTime(long lastPublishTime) {
		this.lastPublishTime = lastPublishTime;
	}
	/**
	 * @return
	 */
	public List<Integer> getIntPlate() {
		return intPlate;
	}

	public void setIntPlate(List<Integer> intPlate) {
		this.intPlate = intPlate;
	}
	
	public int getFromPlate() {
		return fromPlate;
	}
	public void setFromPlate(int fromPlate) {
		this.fromPlate = fromPlate;
	}
	@Override
	public String toString() {
		return "Banner [intPlate=" + intPlate + ", newPlate=" + newPlate
				+ ", fromPlate:" + fromPlate
				+ ", clickNum=" + clickNum + ", lastPublishTime="
				+ lastPublishTime + ", app=" + app + ", theme=" + theme
				+ ", wallpaper=" + wallpaper + ", topic=" + topic + "]";
	}
	
	
}
