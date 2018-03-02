package com.nqmobile.livesdk;

import android.content.Intent;

import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppStatus;
import com.nqmobile.livesdk.modules.appstubfolder.model.AppStubFolder;
import com.nqmobile.livesdk.modules.daily.DailyListListener;
import com.nqmobile.livesdk.modules.locker.Locker;
import com.nqmobile.livesdk.modules.theme.Theme;
import com.nqmobile.livesdk.modules.wallpaper.Wallpaper;

/**
 * store有关数据的更新监听接口。
 * @author changxiaofei
 * @time 2013-11-15 下午7:05:20
 */
public interface OnUpdateListener {
	/**
	 * 设置主题时，调用launcher的具体实现，并返回设置结果。
	 * @param theme
	 * @return 成功/失败
	 */
	public boolean onApplyTheme(Theme theme);
	
	/**
	 * 有新主题下载到本地完毕。需要launcher刷新本地主题列表。
	 * @param theme
	 */
	public void onThemeDownload(Theme theme);
	
	/**
	 * 应用主题完毕后，通知launcher刷新本地主题列表，当前使用主题状态。
	 * @param theme
	 */
	public void onCurrentThemeUpdate(Theme theme);
	
	/**
	 * 有本地主题被删除。需要launcher刷新本地主题列表。
	 * @param theme
	 */
	public void onThemeDelete(Theme theme);
	
	/**
	 * 有新壁纸下载到本地完毕。需要launcher刷新本地壁纸列表。
	 * @param wallpaper
	 */
	public void onWallpaperDownload(Wallpaper wallpaper);
	
	/**
	 * 有新锁屏下载到本地完毕。需要launcher刷新本地锁屏列表。
	 * @param locker
	 */
	public void onLockerDownload(Locker locker);
	
	/**
	 * 删除本地锁屏完毕。需要launcher刷新本地锁屏列表。
	 * @param locker
	 */
	public void onLockerDeleted(Locker locker);
	
	/**
	 * 设置完壁纸后，通知launcher刷新本地壁纸列表。
	 * @param wallpaper
	 */
	public void onCurrentWallpaperUpdate(Wallpaper wallpaper);
	
	/**
	 * 删除本地壁纸。需要launcher刷新本地壁纸列表。
	 * @param wallpaper
	 */
	public void onWallpaperDelete(Wallpaper wallpaper);
	
	/**
	 * 每日推荐App有刷新操作。需要launcher刷新每日推荐App列表。
	 * @param dailyListListener
	 */
	public void onDailyListUpdate(DailyListListener dailyListListener);
	
	/**
	 * 有新下载到的浏览器角标广告。需要launcher刷新浏览器角标。
	 * @param bandge
	 */
//	public void onBandgeDownload(Bandge bandge);
	
	/**
	* 虚框应用获取成功后。需要launcher在桌面显示虚框应用图标。点击图标发送接收的intent
	* @param App, Intent
	*/
	public void onAppStubAdd(App app, Intent intent);

	/**
	* 虚框应用有状态变化。需要launcher刷新或替换虚框应用状态。
	* @param App, AppStatus
	*/
	public void onAppStubStatusUpdate(App app, AppStatus status);

	/**
	* 应用有状态变化。需要launcher刷新或替换应用状态。
	* @param App, AppStatus
	*/
	public void onAppStatusUpdate(App app, AppStatus status);
	
	/**
	* 虚框文件夹解析成功后。需要launcher在桌面显示虚框文件夹和虚框应用图标。
	* @param AppStubFolder
	*/
	public void onAppStubFolderAdd(AppStubFolder folder);
	
	/**
	* 虚框文件夹更新。
	* @param AppStubFolder
	*/
	public void onAppStubFolderUpdate(AppStubFolder folder);
}
