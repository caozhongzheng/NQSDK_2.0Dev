package com.nqmobile.livesdk.modules.theme;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;

import com.nq.interfaces.launcher.TThemeResource;
import com.nq.interfaces.launcher.TTopicResource;
import com.nqmobile.livesdk.modules.wallpaper.WallpaperConstants;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.StringUtil;


/**
 *  1:string resourceId,#专题资源ID
 *	2:string name,#专题名称	
 *	3:string icon,#专题图标
 *	4:string dailyIcon,#每日推荐图标
 *	5:string picture,#专题图片	
 *	6:i64 lastPublishTime, #发布时间	
 *	7:list<TThemeResource> themes, #包含的主题
 *	
 * @author ysongren
 *
 */
public class Topic implements Serializable {

	private static final long serialVersionUID = 2622041579050736394L;

	/** 专题资源ID **/
	private String resId;
	/** 专题名称 **/
	private String name;
	/** 专题图标 **/
	private String icon;
	/** 每日推荐图标 **/
	private String dailyIcon;
	/** 专题图片 **/
	private String picture;
	/** 发布时间 **/
	private long lastPublishTime;
	/** 包含的主题 **/
	private List<Theme> themes;
	
	private String iconPath;
	private String dailyIconPath;
	private String picturePath;
	
	public Topic(){}
	
	public Topic(TTopicResource resource, Context context){
		if (resource != null) {
            setResId(StringUtil.nullToEmpty(resource.getResourceId()));
            setName(StringUtil.nullToEmpty(resource.getName()));
            setIcon(StringUtil.nullToEmpty(resource.icon));
            setDailyIcon(StringUtil.nullToEmpty(resource.dailyIcon));
            setPicture(StringUtil.nullToEmpty(resource.picture));
            setLastPublishTime(resource.lastPublishTime);
            
            List<TThemeResource> themeResources =  resource.themes;
            List<Theme> themes = new ArrayList<Theme>();
            if(themeResources != null && themeResources.size() > 0){
            	for (TThemeResource tThemeResource : themeResources) {
            		themes.add(new Theme(tThemeResource, context));
				}
            }
            setThemes(themes);
            
            String SDCardPath = CommonMethod.getSDcardPath(context);
            if(SDCardPath == null)
            	SDCardPath = CommonMethod.getSDcardPathFromPref(context);
            //icon本地路径
            if (!TextUtils.isEmpty(getIcon())) {
                String strImgPath = new StringBuilder().append(SDCardPath).append(WallpaperConstants.STORE_IMAGE_LOCAL_PATH_WALLPAPER).toString();
                setIconPath(new StringBuilder().append(strImgPath).append(getResId()).append("_icon")
                        .append(getIcon().substring(getIcon().lastIndexOf("."))).toString());
            }
            //DailyIcon本地路径
            if (!TextUtils.isEmpty(getDailyIcon())) {
                String strPath = new StringBuilder().append(SDCardPath).append(WallpaperConstants.STORE_IMAGE_LOCAL_PATH_WALLPAPER).toString();
                setDailyIconPath(new StringBuilder().append(strPath).append(getResId())
                        .append(getDailyIcon().substring(getDailyIcon().lastIndexOf("."))).toString());
            }
            
            //Picture预览图路径
            if(!TextUtils.isEmpty(getPicture())) {
            	String strPath = new StringBuilder().append(SDCardPath).append(WallpaperConstants.STORE_IMAGE_LOCAL_PATH_WALLPAPER).toString();
            	setPicturePath(new StringBuilder().append(strPath).append(getResId()).append("_preview")
                .append(getPicture().substring(getPicture().lastIndexOf("."))).toString());
            }
            //theme.setLongLocalTime(longLocalTime);//这个在保存的时候去客户端时间
        }
	}
	
	public String getResId() {
		return resId;
	}
	public void setResId(String resId) {
		this.resId = resId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getDailyIcon() {
		return dailyIcon;
	}
	public void setDailyIcon(String dailyIcon) {
		this.dailyIcon = dailyIcon;
	}
	public String getPicture() {
		return picture;
	}
	public void setPicture(String picture) {
		this.picture = picture;
	}
	public long getLastPublishTime() {
		return lastPublishTime;
	}
	public void setLastPublishTime(long lastPublishTime) {
		this.lastPublishTime = lastPublishTime;
	}
	public  List<? extends Theme>  getThemes() {
		return themes;
	}
	public void setThemes(List<Theme> themes) {
		this.themes = themes;
	}
	
	public String getIconPath() {
		return iconPath;
	}
	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}
	public String getDailyIconPath() {
		return dailyIconPath;
	}
	public void setDailyIconPath(String dailyIconPath) {
		this.dailyIconPath = dailyIconPath;
	}
	public String getPicturePath() {
		return picturePath;
	}
	public void setPicturePath(String picturePath) {
		this.picturePath = picturePath;
	}
	
	@Override
	public String toString() {
		return "Topic [resId=" + resId + ", name=" + name + ", icon=" + icon
				+ ", dailyIcon=" + dailyIcon + ", picture=" + picture
				+ ", lastPublishTime=" + lastPublishTime + ", themes=" + themes
				+ ", iconPath=" + iconPath + ", dailyIconPath=" + dailyIconPath
				+ ", picturePath=" + picturePath + "]";
	}
	
}
