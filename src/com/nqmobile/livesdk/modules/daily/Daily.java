package com.nqmobile.livesdk.modules.daily;

import java.io.Serializable;

import android.content.Context;

import com.nq.interfaces.launcher.TWidgetResource;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.locker.Locker;
import com.nqmobile.livesdk.modules.locker.LockerManager;
import com.nqmobile.livesdk.modules.theme.Theme;
import com.nqmobile.livesdk.modules.wallpaper.Wallpaper;

/**
 * 每日推荐
 * @author changxiaofei
 * @time 2014-1-2 下午1:41:01
 */
public class Daily implements Serializable {
	private static final long serialVersionUID = -643411688880686990L;
	/** strId */
	private String strId;
	/** 资源类型：0应用 1壁纸 2主题 */
	private int intType;
	/** 应用 */
	private App app;
	/** 壁纸 */
	private Wallpaper wallpaper;
	/** 主题 */
	private Theme theme;
	/** web/webview广告推荐 */
	private Web web;
	/** 锁屏 */
	private Locker locker;
	
	/** 每日推荐列表里的序号 */
	private int seq;
	
	public Daily(){}
	
	public Daily(TWidgetResource wr, Context context){
		if (wr != null) {
            setStrId(wr.getResourceId());
            setIntType(wr.getType());
            if (wr.getType() == DailyManager.DAILY_CACHE_TYPE_APP) {
                setApp(new App(wr.getAppResource(), context));
            } else if (wr.getType() == DailyManager.DAILY_CACHE_TYPE_THEME) {
                setTheme(new Theme(wr.getThemeResource(), context));
            } else if (wr.getType() == DailyManager.DAILY_CACHE_TYPE_WALLPAPER) {
                setWallpaper(new Wallpaper(wr.getWallpaperResource(), context));
            } else if (wr.getType() == DailyManager.DAILY_CACHE_TYPE_WEB) {
                setWeb(new Web(wr.getWebResource()));
            } else if (wr.getType() == DailyManager.DAILY_CACHE_TYPE_LOCKER) {
                setLocker(LockerManager.getInstance(context).lockerResourceToLocker(wr.getLockerResource()));
            }
        }
	}
	
	public String getStrId() {
		return strId;
	}
	
	public void setStrId(String strId) {
		this.strId = strId;
	}
	
	public int getIntType() {
		return intType;
	}
	
	public void setIntType(int intType) {
		this.intType = intType;
	}
	
	public App getApp() {
		return app;
	}
	
	public void setApp(App app) {
		this.app = app;
	}
	
	public Wallpaper getWallpaper() {
		return wallpaper;
	}
	
	public void setWallpaper(Wallpaper wallpaper) {
		this.wallpaper = wallpaper;
	}
	
	public Theme getTheme() {
		return theme;
	}
	
	public void setTheme(Theme theme) {
		this.theme = theme;
	}

	public Web getWeb() {
		return web;
	}

	public void setWeb(Web web) {
		this.web = web;
	}

	public Locker getLocker() {
		return locker;
	}

	public void setLocker(Locker locker) {
		this.locker = locker;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}
}
