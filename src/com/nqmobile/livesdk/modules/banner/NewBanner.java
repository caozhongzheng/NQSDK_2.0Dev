package com.nqmobile.livesdk.modules.banner;

import java.io.Serializable;

import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.theme.Theme;
import com.nqmobile.livesdk.modules.theme.Topic;
import com.nqmobile.livesdk.modules.wallpaper.Wallpaper;

/**
 * 
 * @author ysongren
 *
 */
public class NewBanner implements Serializable {
	
	private static final long serialVersionUID = -477073085476171361L;
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

	/** plate 板块编号： 0：应用、游戏；1：主题；2：壁纸。3专题  4广告平台广告（用于banner区分板块，banner表专用）  */
	private int newPlate;
	/** 点击量 **/
	private int clickNum;
	
	private App app;
	private Theme theme;
	private Wallpaper wallpaper;
	private Topic topic;
	
	
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
}
