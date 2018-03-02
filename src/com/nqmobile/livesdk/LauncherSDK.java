package com.nqmobile.livesdk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;

import com.nqmobile.livesdk.commons.AppConstants;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.image.ImageListener;
import com.nqmobile.livesdk.commons.image.ImageManager;
import com.nqmobile.livesdk.commons.image.LoadIconListener;
import com.nqmobile.livesdk.commons.info.ClientInfo;
import com.nqmobile.livesdk.commons.info.ProcessInfo;
import com.nqmobile.livesdk.commons.init.InitManager;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.mydownloadmanager.MyDownloadManager;
import com.nqmobile.livesdk.commons.net.UpdateListener;
import com.nqmobile.livesdk.commons.receiver.LauncherResumeEvent;
import com.nqmobile.livesdk.commons.receiver.LiveReceiver;
import com.nqmobile.livesdk.commons.service.BackgroundService;
import com.nqmobile.livesdk.commons.ui.FontFactory;
import com.nqmobile.livesdk.commons.ui.StoreMainActivity;
import com.nqmobile.livesdk.commons.ui.TypefaceLayoutInflater;
import com.nqmobile.livesdk.modules.activation.ActivePreference;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppDetailActivity;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.app.AppManager.Status;
import com.nqmobile.livesdk.modules.app.AppStatus;
import com.nqmobile.livesdk.modules.appstub.AppStubConstants;
import com.nqmobile.livesdk.modules.appstub.AppStubManager;
import com.nqmobile.livesdk.modules.appstubfolder.AppStubFolderConstants;
import com.nqmobile.livesdk.modules.appstubfolder.AppStubFolderManager;
import com.nqmobile.livesdk.modules.appstubfolder.model.AppStubFolder;
import com.nqmobile.livesdk.modules.apptype.AppTypeListener;
import com.nqmobile.livesdk.modules.apptype.AppTypeManager;
import com.nqmobile.livesdk.modules.association.AssociationActionConstants;
import com.nqmobile.livesdk.modules.association.AssociationManager;
import com.nqmobile.livesdk.modules.browserbandge.BandgeManager;
import com.nqmobile.livesdk.modules.categoryfolder.AppStubListener;
import com.nqmobile.livesdk.modules.categoryfolder.CategoryFolderConfig;
import com.nqmobile.livesdk.modules.categoryfolder.CategoryFolderManager;
import com.nqmobile.livesdk.modules.categoryfolder.RecommendAppsListener;
import com.nqmobile.livesdk.modules.daily.Daily;
import com.nqmobile.livesdk.modules.daily.DailyActionConstants;
import com.nqmobile.livesdk.modules.daily.DailyListListener;
import com.nqmobile.livesdk.modules.daily.DailyManager;
import com.nqmobile.livesdk.modules.daily.Web;
import com.nqmobile.livesdk.modules.daily.WebActivity;
import com.nqmobile.livesdk.modules.defaultlauncher.KP;
import com.nqmobile.livesdk.modules.feedback.FeedBackActivity;
import com.nqmobile.livesdk.modules.feedback.FeedBackListener;
import com.nqmobile.livesdk.modules.feedback.FeedBackManager;
import com.nqmobile.livesdk.modules.font.FontManager;
import com.nqmobile.livesdk.modules.gamefolder_v2.AdvertisementListListener;
import com.nqmobile.livesdk.modules.gamefolder_v2.GameFolderV2ActionConstants;
import com.nqmobile.livesdk.modules.gamefolder_v2.GameManager;
import com.nqmobile.livesdk.modules.incrementupdate.IncrementUpdateConstants;
import com.nqmobile.livesdk.modules.incrementupdate.IncrementUpdateManager;
import com.nqmobile.livesdk.modules.locker.ILockerSDK;
import com.nqmobile.livesdk.modules.locker.Locker;
import com.nqmobile.livesdk.modules.locker.LockerDetailActivity;
import com.nqmobile.livesdk.modules.locker.LockerListListener;
import com.nqmobile.livesdk.modules.locker.LockerManager;
import com.nqmobile.livesdk.modules.locker.model.LockerEngine;
import com.nqmobile.livesdk.modules.lqwidget.LqWidgetListener;
import com.nqmobile.livesdk.modules.lqwidget.LqWidgetManager;
import com.nqmobile.livesdk.modules.mustinstall.MustInstallPreference;
import com.nqmobile.livesdk.modules.push.PushManager;
import com.nqmobile.livesdk.modules.search.SearchHotwordsCallback;
import com.nqmobile.livesdk.modules.search.SearcherActionConstants;
import com.nqmobile.livesdk.modules.search.SearcherManager;
import com.nqmobile.livesdk.modules.stat.ActionConstants;
import com.nqmobile.livesdk.modules.stat.PkgFirstCliskStatPreference;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.modules.storeentry.StoreEntryPreference;
import com.nqmobile.livesdk.modules.theme.Theme;
import com.nqmobile.livesdk.modules.theme.ThemeDetailActivity;
import com.nqmobile.livesdk.modules.theme.ThemeListListener;
import com.nqmobile.livesdk.modules.theme.ThemeManager;
import com.nqmobile.livesdk.modules.theme.ThemePreference;
import com.nqmobile.livesdk.modules.wallpaper.Wallpaper;
import com.nqmobile.livesdk.modules.wallpaper.WallpaperDetailActivity;
import com.nqmobile.livesdk.modules.wallpaper.WallpaperListListener;
import com.nqmobile.livesdk.modules.wallpaper.WallpaperManager;
import com.nqmobile.livesdk.modules.wallpaper.WallpaperPreference;
import com.nqmobile.livesdk.modules.weather.CityListener;
import com.nqmobile.livesdk.modules.weather.GetPositionListener;
import com.nqmobile.livesdk.modules.weather.HotCities;
import com.nqmobile.livesdk.modules.weather.WeatherListener;
import com.nqmobile.livesdk.modules.weather.WeatherManager;
import com.nqmobile.livesdk.modules.weather.WeatherUpdateListener;
import com.nqmobile.livesdk.modules.weather.model.City;
import com.nqmobile.livesdk.utils.GpUtils;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.PackageUtils;
import com.nqmobile.livesdk.utils.PreferenceDataHelper;
import com.nqmobile.livesdk.utils.ToastUtils;

/**
 * 给launcher的接口
 * @author changxiaofei
 * @time 2013-11-27 上午10:44:07
 */
public class LauncherSDK {
	private static final ILogger NqLog = LoggerFactory.getLogger(LauncherSDK.class.getSimpleName());
	
	/** store 模块编号 widget模块*/
	public static final int STORE_MODULE_TYPE_WIDGET = 3;
	/** store 模块编号 Launcher壁纸和主题图标列表模块*/
	public static final int STORE_MODULE_TYPE_LAUNCHER_ICON = 4;
    /** store 模块编号 游戏广告*/
    public static final int STORE_MODULE_TYPE_GAME_AD = 5;
    /** store 模块编号 装机必备 */
    public static final int STORE_MODULE_TYPE_MUST_INSTALL = 6;
	/** store 模块编号，PUSH模块 */
	public static final int STORE_MODULE_TYPE_PUSH = 7;
	
	/** store 模块编号，积分中心 */
	public static final int STORE_MODULE_TYPE_POINTS_CENTER = 9;
    public static final int STORE_FRAGMENT_INDEX_APP = 0;
    public static final int STORE_FRAGMENT_INDEX_THEME = 1;
    public static final int STORE_FRAGMENT_INDEX_WALLPAPER = 2;
	
    public Context mContext;
    /** store数据更新的监听对象 */
    private List<OnUpdateListener> onUpdateListeners = new ArrayList<OnUpdateListener>();
    private List<ILockerSDK> lockerSDKs = new ArrayList<ILockerSDK>();
    private static LauncherSDK mInstance;
    private String mApkPath;
    public static final String KEY_FROM_DAILY = "from_daily";
    private static final String DAILY_CLICK = "1602";
    private static final String DAILY_DISPLAY = "1601";
    private ExecutorService mThreadPool;

    private LauncherSDK(Context context) {
        this.mContext = context.getApplicationContext();
        ApplicationContext.setContext(mContext);
    }
    
    public synchronized static LauncherSDK getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new LauncherSDK(context);
        }
        return mInstance;
    }

    /**
     * 注册相关的更新监听。包括：主题的下载完成、设置、删除事件；壁纸的下载完成、设置、删除事件；每日推荐数据刷新事件。
     * @param onUpdateListener
     */
    public synchronized void registerOnUpdateListener(OnUpdateListener onUpdateListener) {
        NqLog.i("registerOnUpdateListener onUpdateListener=" + onUpdateListener);
        if (onUpdateListeners == null) {
            onUpdateListeners = new ArrayList<OnUpdateListener>();
            ActivePreference.getInstance().setBooleanValue(ActivePreference.KEY_NEED_FORE_ACTIVE, true);
        }
        if (onUpdateListener != null){
            if(!onUpdateListeners.contains(onUpdateListener)){
                this.onUpdateListeners.add(onUpdateListener);
            }
        }
    }
    /**
     * 解除注册相关的更新监听。包括：主题的下载完成、设置、删除事件；壁纸的下载完成、设置、删除事件；每日推荐数据刷新事件。
     * @param onUpdateListener
     */
    public synchronized void unregisterOnUpdateListener(OnUpdateListener onUpdateListener) {
        NqLog.i("unregisterOnUpdateListener onUpdateListener=" + onUpdateListener);
        if (onUpdateListeners != null && onUpdateListeners.size() > 0) {
            onUpdateListeners.remove(onUpdateListener);
        }
    }

    /**
     * 注册天气数据更新监听
     * @param cityId
     * @param listener
     */
    public void registerWeatherUpdateListener(ArrayList<String> cityId, WeatherUpdateListener listener) {
        NqLog.i("regigestWeatherUpdateListener listener=" + listener);
        
        WeatherManager weatherManager = WeatherManager.getInstance();
        weatherManager.setFavorCities(cityId);
        weatherManager.addWeatherUpdateListener(listener);
    }

    /**
     * 解除天气数据更新监听
     * @param listener
     */
    public void unregisterWeatherUpdateListener(WeatherUpdateListener listener) {
        NqLog.i("unregisterWeatherUpdateListener listener=" + listener);
        WeatherManager.getInstance().addWeatherUpdateListener(listener);
    }


    /**
     * 注册锁屏相关的更新监听。包括：获取锁屏引擎类型、锁屏应用和体验、锁屏引擎升级事件。
     * @param ILockerSDK
     */
    public synchronized void setLockerSDK(ILockerSDK iLockerSDK) {
        NqLog.i("setLockerSDK ILockerSDK=" + iLockerSDK);
        if (lockerSDKs == null) {
        	lockerSDKs = new ArrayList<ILockerSDK>();
        }
        if (lockerSDKs != null)
            this.lockerSDKs.add(iLockerSDK);
    }

    /**
     * 解除注册相关的锁屏更新监听。包括：获取锁屏引擎类型、锁屏应用和体验、锁屏引擎升级事件。
     * @param ILockerSDK
     */
    public synchronized void removeLockerSDK(ILockerSDK iLockerSDK) {
        NqLog.i("removeLockerSDK ILockerSDK=" + iLockerSDK);
        if (lockerSDKs != null && lockerSDKs.size() > 0) {
        	lockerSDKs.remove(iLockerSDK);
        }
    }

    /**
     * 获取已注册的OnUpdateListeners监听对象集合
     */
    public List<OnUpdateListener> getRegisteredOnUpdateListeners() {
        return this.onUpdateListeners;
    }

    public List<ILockerSDK> getLockerSDKs() {
        return this.lockerSDKs;
    }

    /**
     * 锁屏SDK是否启用，启用:true(锁屏功能开启)
     * @param enable
     */
    public void onLockerSDKStatusChanged(boolean enable){
    	PreferenceDataHelper.getInstance(mContext).setBooleanValue(PreferenceDataHelper.KEY_LOCKER_SDK_ENABLE, true);
    }
    
    public LockerEngine getLockerEngine(int engineID){
    	if(lockerSDKs != null && lockerSDKs.size() > 0){
    		LockerEngine le = new LockerEngine();
    		for(int i=0; i<lockerSDKs.size(); i++){
    			le = lockerSDKs.get(i).getLockerEngine(engineID);
    			break;
    		}
    		return le;
    	}
    	return null;
    }

    public boolean applyLocker(Locker l){
    	boolean result = false;
    	if(lockerSDKs != null && lockerSDKs.size() > 0){
    		for(int i=0; i<lockerSDKs.size(); i++){
    			boolean b = tryApplyLocker(lockerSDKs.get(i), l);
    			result = result || b;
    		}
    		NqLog.i("applyLocker: " + (result?"SUC":"FAILED"));
    	}
    	return result;
    }
    
    private boolean tryApplyLocker(ILockerSDK iLockerSDK, Locker l) {
		// TODO Auto-generated method stub
    	boolean result = false;
    	if(iLockerSDK == null)
    		return result;

    	try{
    		result = iLockerSDK.applyLocker(l);
    	} catch (Exception e) {
			// TODO: handle exception
    		NqLog.e(e);
		}

    	return result;
    }

    public boolean previewLocker(Locker l){
    	boolean result = false;
    	if(lockerSDKs != null && lockerSDKs.size() > 0){
    		for(int i=0; i<lockerSDKs.size(); i++){
    			boolean b = tryPreviewLocker(lockerSDKs.get(i), l);
    			result = result || b;
    		}
    		NqLog.i("previewLocker: " + (result?"SUC":"FAILED"));
    	}
    	return result;
    }
    
    private boolean tryPreviewLocker(ILockerSDK iLockerSDK, Locker l) {
		// TODO Auto-generated method stub
    	boolean result = false;
    	if(iLockerSDK == null)
    		return result;

    	try{
    		result = iLockerSDK.previewLocker(l);
    	} catch (Exception e) {
			// TODO: handle exception
    		NqLog.e(e);
		}

    	return result;
    }

    public boolean upgLocker(LockerEngine le){
    	boolean result = false;
    	if(lockerSDKs != null && lockerSDKs.size() > 0){
    		for(int i=0; i<lockerSDKs.size(); i++){
    			boolean b = tryUpgLocker(lockerSDKs.get(i), le);
    			result = result || b;
    		}
    		NqLog.i("upgLocker: " + (result?"SUC":"FAILED"));
    	}
    	return result;
    }
    
    private boolean tryUpgLocker(ILockerSDK iLockerSDK, LockerEngine le) {
		// TODO Auto-generated method stub
    	boolean result = false;
    	if(iLockerSDK == null)
    		return result;

    	try{
    		result = iLockerSDK.upgradeLockerEngine(le);
    	} catch (Exception e) {
			// TODO: handle exception
    		NqLog.e(e);
		}

    	return result;
    }

	/**
     * launcher SDK初始化
     */
    public boolean initSDK() {
        NqLog.i("LauncherSDK.initSDK()");
        
		boolean result = false;
		try {
			final InitManager initMgr = new InitManager(mContext);
			initMgr.registerModules();
			initMgr.init();
			AppStubManager.getInstance(mContext).registerAppStubAfterReboot();
			result = true;
			NqLog.i("LauncherSDK.initSDK() OK!");
		} catch (Exception e) {
			NqLog.e(e);
		}

		return result;
    }

    /**
     * 获取天气
     * @param cityId 城市ID
     * @param language 语言
     * @param days 未来几天的天气，为0表示不需要这部分的天气信息
     * @param hours 未来几小时的天气，为0表示不需要这部分的天气信息
     * @param listener 接口回调
     */
    public void getWeather(String cityId, String language, int days, int hours, WeatherListener listener) {
        NqLog.i("LiveSDK getWeather");
        WeatherManager.getInstance().getWeather(cityId, language, days, hours, listener);
    }

    /**
     * 获取城市列表
     * @param keyword 关键字
     * @param language 语言
     * @param listener 接口回调
     */
    public void getCity(String keyword, String language, CityListener listener) {
        NqLog.i("LiveSDK getCity ");
        if (TextUtils.isEmpty(keyword)) {
            listener.onErr();
        } else {
            WeatherManager.getInstance().getCity(keyword, language, listener);
        }
    }

    /**
     * 获取每日推荐应用列表。(旧接口 一次获取2个)
     * @param country 所在国家
     * @param language 所属语言
     * @param listener 监听类
     */
    public void getDailyList(String country, String language, DailyListListener listener) {
        getDailyList(2,listener);
    }

    /**
     * 根据size返回每日推荐数据
     * @param size
     * @param listener
     */
    public void getDailyList(int size,DailyListListener listener){
        DailyManager.getInstance(mContext).getDailyList(size,listener);
    }

    /**
     * 获取本地主题列表。
     * @param listener 监听类
     */
    public void getLocalTheme(ThemeListListener listener) {
        listener.onGetThemeListSucc(ThemeManager.getInstance(mContext).getThemeListFromLocal());
    }

    /**
     * 获取本地壁纸列表。
     * @param listener 监听类
     */
    public void getLocalWallpaper(WallpaperListListener listener) {
        listener.onGetWallpaperListSucc(WallpaperManager.getInstance(mContext).getWallpaperListFromLocal());
    }

    /**
     * 获取本地锁屏列表。
     * @param listener 监听类
     */
    public void getLocalLocker(LockerListListener listener) {
    	listener.onGetLockerListSucc(LockerManager.getInstance(mContext).getLockerListFromLocal());
    }

    /**
     * 进入store页面。
     * @param fragmentToShow 请使用常量：ThemeConstants.STORE_FRAGMENT_INDEX_……。0应用；1主题；2壁纸；3管理
     * @param columnToShow 栏目索引
     */
    public void gotoStore(int fragmentToShow, int columnToShow) {
        Intent intent = new Intent(mContext, StoreMainActivity.class);
        intent.putExtra(StoreMainActivity.KEY_FRAGMENT_INDEX_TO_SHOW, fragmentToShow);
        intent.putExtra(StoreMainActivity.KEY_FRAGMENT_COLUMN_TO_SHOW, columnToShow);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    /**
     * 进入store页面。
     * @param fragmentToShow 请使用常量：ThemeConstants.STORE_FRAGMENT_INDEX_……。0应用；1主题；2壁纸；3管理
     */
    public void gotoStore(int fragmentToShow) {
        gotoStore(fragmentToShow, 0);
    }

    /**
     * 进入store的应用详情页面。
     * @param App
     */
    public void gotoDetail(App app) {
        if (app == null){
            NqLog.e("parameter app is null");
            return;
        } else {
            NqLog.d("gotoDetail: " + app.getStrId());
        }

        Intent intent = new Intent(mContext, AppDetailActivity.class);
        intent.putExtra(AppDetailActivity.KEY_APP, app);
        intent.putExtra(KEY_FROM_DAILY,true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    /**
     * 进入store的主题详情页面。
     * @param Theme
     */
    public void gotoDetail(Theme theme) {
        if (theme == null){
            NqLog.e("parameter theme is null");
            return;
        }else {
            NqLog.d("gotoDetail: " + theme.getStrId());
        }

        Intent intent = new Intent(mContext, ThemeDetailActivity.class);
        intent.putExtra(ThemeDetailActivity.KEY_THEME, theme);
        intent.putExtra(KEY_FROM_DAILY,true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    /**
     * 进入store的壁纸详情页面。
     * @param Wallpaper
     */
    public void gotoDetail(Wallpaper wallpaper) {
        if (wallpaper == null){
            NqLog.e("parameter wallpaper is null");
            return;
        } else {
            NqLog.d("gotoDetail: " + wallpaper.getStrId());
        }
        
        ArrayList<Wallpaper> result = new ArrayList<Wallpaper>();
        result.add(wallpaper);
        Intent intent = new Intent(mContext, WallpaperDetailActivity.class);
        intent.putExtra(WallpaperDetailActivity.KEY_WALLPAPERS, result);
        intent.putExtra(KEY_FROM_DAILY,true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    /**
     * 进入store的web/webview广告推荐详情页面。
     * @param Web
     */
    public void gotoDetail(Web web) {
        if (web == null){
            NqLog.e("parameter web is null");
            return;
        } else {
            NqLog.d("gotoDetail: " + web.getStrId());
        }

    	/** 点击后跳转类型 0—打开浏览器 1—打开webview */
        if(web.getIntJumpType() == 0){
        	Uri url = Uri.parse(web.getStrLinkUrl());
            Intent intent = new Intent(Intent.ACTION_VIEW, url);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } else if(web.getIntJumpType() == 1){
        	Intent intent = new Intent(mContext, WebActivity.class);
        	intent.putExtra(WebActivity.KEY_WEB_URL, web.getStrLinkUrl());
        	intent.putExtra(KEY_FROM_DAILY,true);
        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	mContext.startActivity(intent);
        }
    }

    /**
     * 进入store的锁屏详情页面。
     * @param Locker
     */
    public void gotoDetail(Locker locker) {
        if (locker == null){
            NqLog.e("parameter locker is null");
            return;
        } else {
            NqLog.d("gotoDetail: " + locker.getStrId());
        }

        ArrayList<Locker> list = new ArrayList<Locker>();
        list.add(locker);
        Intent intent = new Intent(mContext, LockerDetailActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(LockerDetailActivity.KEY_LOCKERS, list);
		intent.putExtra(KEY_FROM_DAILY,true);
		mContext.startActivity(intent);
    }

    /**
     * 进入store的应用、主题、壁纸、web/webview广告推荐、锁屏详情页面。
     * @param Daily
     */
    public void gotoDetail(Daily daily) {
        if (daily == null){
            NqLog.e("parameter daily is null");
            return;
        } else {
            NqLog.d("gotoDetail: " + daily.getIntType());
        }

        if (daily.getIntType() == DailyManager.DAILY_CACHE_TYPE_APP) {
            gotoDetail(daily.getApp());
        } else if (daily.getIntType() == DailyManager.DAILY_CACHE_TYPE_THEME) {
            gotoDetail(daily.getTheme());
        } else if (daily.getIntType() == DailyManager.DAILY_CACHE_TYPE_WALLPAPER) {
            gotoDetail(daily.getWallpaper());
        } else if (daily.getIntType() == DailyManager.DAILY_CACHE_TYPE_WEB) {
            gotoDetail(daily.getWeb());
        } else if (daily.getIntType() == DailyManager.DAILY_CACHE_TYPE_LOCKER) {
            gotoDetail(daily.getLocker());
        } else {
            NqLog.e("unknown intType " + daily.getIntType());
        }
    }

    /**
     * 使用主题。需要launcher刷新本地主题列表，当前使用主题状态。
     * @param theme
     */
    public boolean applyTheme(Theme theme) {
        NqLog.i("LauncherSDK applyTheme!");
        boolean result = false;
        if (onUpdateListeners != null && onUpdateListeners.size() > 0) {
            boolean applyResult = false;

            //应用了主题后 当前桌面的默认壁纸就是主题带的壁纸，所以设置成默认的wallpaperId
            WallpaperPreference.getInstance().setStringValue(WallpaperPreference.KEY_CURRENT_WALLPAPER,"wallpaper_default");
            for (int i = 0; i < onUpdateListeners.size(); i++) {
                boolean b = tryApplyTheme(onUpdateListeners.get(i),theme);// 应用主题
                applyResult = applyResult || b;
            }
            if (applyResult) {// 设置成功
            	ThemePreference.getInstance().setStringValue(ThemePreference.KEY_CURRENT_THEME, theme.getStrId());
                for (int i = 0; i < onUpdateListeners.size(); i++) {
                    onUpdateListeners.get(i).onCurrentThemeUpdate(theme);// 通知刷新
                }
                ToastUtils.toast(mContext, "nq_set_theme_succ");
                result = true;

                Intent i = new Intent();
                i.setClassName(mContext.getPackageName(), "com.lqsoft.launcher.LauncherFirst");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(i);
            }
        }
        return result;
    }

    private boolean tryApplyTheme(OnUpdateListener listener, Theme theme) {
        boolean result = false;
        if (listener == null){
            return result;
        }

        try {
            NqLog.d("onApplyTheme: " + listener.toString());
            result = listener.onApplyTheme(theme);
        } catch (Exception e) {
            NqLog.e(e);
        }

        return result;
    }

    public void themeDownload(Theme theme){
        NqLog.i("LauncherSDK themeDownload!");
        if (onUpdateListeners != null && onUpdateListeners.size() > 0) {
            for(OnUpdateListener l : onUpdateListeners){
                l.onThemeDownload(theme);
            }
        }
    }

    public void wallpaperDownload(Wallpaper wallpaper){
        NqLog.i("LauncherSDK wallpaperDownload!");
        if (onUpdateListeners != null && onUpdateListeners.size() > 0) {
            for(OnUpdateListener l : onUpdateListeners){
                l.onWallpaperDownload(wallpaper);
            }
        }
    }
    
    public void lockerDownload(Locker locker){
        NqLog.i("LauncherSDK lockerDownload!");
        if (onUpdateListeners != null && onUpdateListeners.size() > 0) {
            for(OnUpdateListener l : onUpdateListeners){
                l.onLockerDownload(locker);
            }
        }
    }

    public void lockerDeleted(Locker locker){
        NqLog.i("LauncherSDK lockerDeleted!");
        if (onUpdateListeners != null && onUpdateListeners.size() > 0) {
            for(OnUpdateListener l : onUpdateListeners){
                l.onLockerDeleted(locker);
            }
        }
    }
    
    /**
     * 使用壁纸。需要launcher刷新本地壁纸列表，当前使用壁纸状态。
     * @param wallpaer
     */
    public boolean applyWallpaper(Wallpaper wallpaper) {
        return WallpaperManager.getInstance(mContext).applyWallpaper(wallpaper,false);
    }

    /**
     * 删除壁纸
     * @param w
     */
    public boolean deleteWallpaper(Wallpaper w){
        return WallpaperManager.getInstance(mContext).deleteWallpaper(w);
    }

    /**
     * 删除主题
     */
    public boolean deleteTheme(String resId){
        return ThemeManager.getInstance(mContext).deleteTheme(resId);
    }

    /**
     * 删除锁屏
     * @param Locker
     */
    public boolean deleteLocker(Locker l){
        return LockerManager.getInstance(mContext).deleteLocalLoker(l);
    }

    /**
     * 获取当前使用主题的ID
     * @return
     */
    public String getCurrentThemeID() {
        return ThemeManager.getInstance(mContext).getCurrentThemeID();
    }

    /**
     * 获取当前使用壁纸的ID
     * @return
     */
    public String getCurrentWallpaperID() {
        return WallpaperManager.getInstance(mContext).getCurrentWallpaperID();
    }

    /**
     * 获取Daily的icon图片。
     * @param app
     * @param imageLoadListener
     */
    public void getIconBitmap(Daily daily, LoadIconListener imageLoadListener) {
        if (daily.getIntType() == DailyManager.DAILY_CACHE_TYPE_APP) {
            ImageManager.getInstance(mContext).getIconBitmap(DailyManager.STORE_MODULE_TYPE_DAILY, daily.getApp(), imageLoadListener);
        } else if (daily.getIntType() == DailyManager.DAILY_CACHE_TYPE_THEME) {
            ImageManager.getInstance(mContext).getIconBitmap(DailyManager.STORE_MODULE_TYPE_DAILY, daily.getTheme(), imageLoadListener);
        } else if (daily.getIntType() == DailyManager.DAILY_CACHE_TYPE_WALLPAPER) {
            ImageManager.getInstance(mContext).getIconBitmap(DailyManager.STORE_MODULE_TYPE_DAILY, daily.getWallpaper(), imageLoadListener);
        } else if (daily.getIntType() == DailyManager.DAILY_CACHE_TYPE_WEB) {
            ImageManager.getInstance(mContext).getIconBitmap(DailyManager.STORE_MODULE_TYPE_DAILY, daily.getWeb(), imageLoadListener);
        } else if (daily.getIntType() == DailyManager.DAILY_CACHE_TYPE_LOCKER) {
            ImageManager.getInstance(mContext).getIconBitmap(DailyManager.STORE_MODULE_TYPE_DAILY, daily.getLocker(), imageLoadListener);
        } else {
            imageLoadListener.onLoadComplete(ImageManager.getInstance(mContext).getDefaultIcon());
        }
    }

    /**
     * 获取App的icon图片。
     * @param app
     * @param imageLoadListener
     */
    public void getIconBitmap(App app, LoadIconListener imageLoadListener) {
        ImageManager.getInstance(mContext).getIconBitmap(STORE_MODULE_TYPE_LAUNCHER_ICON, app, imageLoadListener);
    }

    /**
	 * 获取App的preview图片。
	 * 
	 * @param app
	 * @param index
	 *            显示第几张预览图
	 * @param imageLoadListener
	 */
	public void getPreviewBitmap(App app, int index,
			LoadIconListener imageLoadListener) {
		ImageManager.getInstance(mContext).getPreviewBitmap(
				STORE_MODULE_TYPE_GAME_AD, index, app,
				imageLoadListener);
	}
    /**
     * 获取Game的icon图片。
     * @param app
     * @param imageLoadListener
     */
    public void getIconBitmap(App app, ImageListener imageListener) {
		// TODO Auto-generated method stub
    	ImageManager.getInstance(mContext).getIconBitmap(STORE_MODULE_TYPE_GAME_AD, app, imageListener);
	}

    /**
     * 获取虚框文件夹的icon图片。
     * @param asf
     * @param imageLoadListener
     */
    public void getIconBitmap(AppStubFolder asf, LoadIconListener imageListener) {
		// TODO Auto-generated method stub
    	ImageManager.getInstance(mContext).getIconBitmap(AppStubFolderConstants.STORE_MODULE_TYPE_STUB_FOLDER, asf, imageListener);
	}
    /**
     * 获取Theme的icon图片。
     * @param theme
     * @param imageLoadListener
     */
    public void getIconBitmap(Theme theme, LoadIconListener imageLoadListener) {
        ImageManager.getInstance(mContext).getIconBitmap(STORE_MODULE_TYPE_LAUNCHER_ICON, theme, imageLoadListener);
    }

    /**
     * 获取Theme的icon图片。
     * @param theme
     * @param imageLoadListener
     * @param customize true:定制大小(customize为true时width和height有效)
     * @param width 定制宽度(width和height如果有任何一个不大于0都无效)
     * @param height 定制高度(width和height如果有任何一个不大于0都无效)
     */
    public void getIconBitmap(Theme theme, LoadIconListener imageLoadListener,
            boolean customize, int width, int height) {
        ImageManager.getInstance(mContext).getIconBitmap(
                AppConstants.STORE_MODULE_TYPE_LAUNCHER_ICON, theme,
                imageLoadListener, customize, width, height);
    }

    /**
     * 获取Wallpaper的icon图片。
     * @param wallpaper
     * @param imageLoadListener
     */
    public void getIconBitmap(Wallpaper wallpaper, LoadIconListener imageLoadListener) {
        ImageManager.getInstance(mContext).getIconBitmap(STORE_MODULE_TYPE_LAUNCHER_ICON, wallpaper, imageLoadListener);
    }

    /**
     * 用户反馈
     */
    public void feedBack() {
        Intent i = new Intent(mContext, FeedBackActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(i);
    }
    /**
     * 用户反馈
     */
    public void feedBack(String contact, String content, FeedBackListener listener) {
    	FeedBackManager.getInstance().uploadFeedback(contact, content, listener);
    }

    /**
     * 检查更新
     */
    public void checkUpdate(final UpdateListener listener) {
    	IncrementUpdateManager.getInstance(mContext).getUpdate(listener);
    }

    /**
     * 检查更新文件的状态
     * @return
     */
    private boolean getDownloadState(){
        PreferenceDataHelper helper = PreferenceDataHelper.getInstance(mContext);
        boolean downloadFinish = helper.getBooleanValue(PreferenceDataHelper.KEY_DOWNLOAD_FINISH);
        String updateFileName = helper.getStringValue(PreferenceDataHelper.KEY_UPDATE_FILE_NAME);
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),updateFileName);
        NqLog.i("file.path="+file.getPath());
        mApkPath = file.getPath();
        if(!file.exists()){
            helper.setBooleanValue(PreferenceDataHelper.KEY_DOWNLOAD_FINISH, false);
        }

        if(file.exists() && downloadFinish){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 下载更新包
     */
    public void downloadNewApk() {
        //Utility.getInstance(mContext).downloadApp(false);
    	IncrementUpdateManager mgr = IncrementUpdateManager.getInstance(mContext);
    	try {
			mgr.processUpgrade(new Handler());
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    public boolean haveUpdate(){
        return PreferenceDataHelper.getInstance(mContext).getBooleanValue(PreferenceDataHelper.KEY_HAVE_UPDATE);
    }

    /**
     * 获取热门城市列表
     * @param language
     * @return
     */
    public List<City> getHotCities(String language) {
        if (language.equals("zh")) {
            return HotCities.queryHotCities(mContext, "zh");
        } else {
            return HotCities.queryHotCities(mContext, "en");
        }
    }

    /**
     * @param daily 涉及的Daily对象
     * @param action 动作 0 展示 1 点击
     */
    public void onDailyAction(Daily daily,int action){ 
//    	NqLog.i("onDailyAction daily="+daily+" action="+action);
    	int type = daily.getIntType();
    	String desc = action == 0 ? DailyActionConstants.ACTION_LOG_1601:DailyActionConstants.ACTION_LOG_1602;
    	switch(type){
            case DailyManager.DAILY_CACHE_TYPE_APP:
                StatManager.getInstance().onAction(
                        StatManager.TYPE_AD_STAT, desc, daily.getApp().getStrId(),
                        action, String.valueOf(DailyManager.DAILY_CACHE_TYPE_APP));
                NqLog.i(daily.getApp().toString());
                break;
            case DailyManager.DAILY_CACHE_TYPE_THEME:
                StatManager.getInstance().onAction(
                        StatManager.TYPE_STORE_ACTION, desc, daily.getTheme().getStrId(),
                        action, String.valueOf(DailyManager.DAILY_CACHE_TYPE_THEME));
                NqLog.i(daily.getTheme().toString());
                break;
            case DailyManager.DAILY_CACHE_TYPE_WALLPAPER:
                StatManager.getInstance().onAction(
                        StatManager.TYPE_STORE_ACTION, desc, daily.getWallpaper().getStrId(),
                        action, String.valueOf(DailyManager.DAILY_CACHE_TYPE_WALLPAPER));
                NqLog.i(daily.getWallpaper().toString());
                break;
            case DailyManager.DAILY_CACHE_TYPE_WEB:
                StatManager.getInstance().onAction(
                        StatManager.TYPE_STORE_ACTION, desc, daily.getWeb().getStrId(),
                        action, String.valueOf(DailyManager.DAILY_CACHE_TYPE_WEB));
                NqLog.i(daily.getWeb().toString());
                break;
            case DailyManager.DAILY_CACHE_TYPE_LOCKER:
                StatManager.getInstance().onAction(
                        StatManager.TYPE_STORE_ACTION, desc, daily.getLocker().getStrId(),
                        action, String.valueOf(DailyManager.DAILY_CACHE_TYPE_LOCKER));
                NqLog.i(daily.getLocker().toString());
                break;
        }
    }

    public void getPosition(GetPositionListener listener){
    	WeatherManager.getInstance().getPosition(listener);
    }

    //设置天气widget计量单位是否为华氏温度
    public void setWeatherUnitToFah(boolean value) {
    	WeatherManager.getInstance().setWeatherUnitToFah(value);
    }
    
    //取天气widget计量单位是否为华氏温度
    public boolean getWeatherUnitToFah() {
    	return WeatherManager.getInstance().isWeatherUnitToFah();
    }
    
    //点击广告方法
    public void clickRecommend(int column,App app){
        AppManager.getInstance(mContext).viewAppDetail(column,app);
    }
    
    public String getShareLink(){
    	String channelId = ClientInfo.getChannelId((ContextWrapper) mContext);
		String result = MResource.getString(mContext, "nq_share_link", channelId);
    	return result;  	
    }
    
    public boolean getCreateGameFolder(){
    	//return PreferenceDataHelper.getInstance(mContext).getBooleanValue(PreferenceDataHelper.KEY_GAME_FOLDER_ENABLE);
    	return false; //TODO
    }
    
    public boolean isGame(String packagename) {
    	return GameManager.getInstance().isGameFromLocal(packagename);
    }

    public void onOnlineThemeClick(){
        StatManager.getInstance().
                onAction(StatManager.TYPE_STORE_ACTION, DailyActionConstants.ACTION_LOG_1603, null, 1, null);
    }

    /**
     * 用户回到了桌面的回调
     */
    public void onResume(){
        StatManager.getInstance().
                onAction(StatManager.TYPE_STORE_ACTION, ActionConstants.ACTION_LOG_2000, null, 0, null);

//        killProcess(ProcessInfo.PROCESS_STORE, false);
        EventBus.getDefault().post(new LauncherResumeEvent());
        
        //显示push信息
        PushManager.getInstance().showPush(PushManager.TYPE_DESKTOP);

		//是否显示升级信息
		checkShowUpdateInfo();
    }

    public void onPreRestoreDefault(){
    	killProcess(ProcessInfo.PROCESS_STORE, true);
    	killProcess(ProcessInfo.PROCESS_PROVIDER, true);
    }

    private void killProcess(String processName, boolean immediately) {
		// TODO Auto-generated method stub
    	int pid = -1;
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = am.getRunningAppProcesses();
        int myUid = android.os.Process.myUid();
        for(ActivityManager.RunningAppProcessInfo info:list){
            if(info.uid == myUid && info.processName.equals(processName)){
                pid = info.pid;
            }
        }

        if(pid == -1)
        	return;
        
        NqLog.i("killProcess pid=" + pid + " ,processName=" + processName + " ,immediately=" + immediately);
        if(!immediately){
        	final int ppid = pid;
        	if (mThreadPool == null) {
				mThreadPool = Executors.newSingleThreadExecutor();
			}
			mThreadPool.execute(new Runnable() {

				@Override
				public void run() {
					if (ppid != 0) {
						try {
							Thread.sleep(1000);
							android.os.Process.killProcess(ppid);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

				}
			});
        }
        else {
        	android.os.Process.killProcess(pid);
        }
	}

	/**
	 * 获取关联推荐信息
	 * 
	 * @param packageName
	 */
    public App getAssociationApps(String packageName) {
    	NqLog.i("getAssociationApps is be invoked, argument = " + packageName);
    	return AssociationManager.getInstance(mContext).getAssociationApps(packageName);
    }
	
	/**
	 * 关联推荐事件统计
	 * 
	 * @param resId 资源id
	 * 		  type 0:关联推荐展示  1:关联推荐应用点击安装
	 */	
    public void onAssociationAppClick (String resId, int type) {	
    	if(type == 0){
            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION, AssociationActionConstants.ACTION_LOG_2401, resId, 1, null);
    	} else if(type == 1){
            StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT, AssociationActionConstants.ACTION_LOG_2402, resId, 1, null);
    	}    	
    }
	/**
	 * 桌面点击icon事件统计
	 * 
	 * @param packageName
	 * @from 0 从桌面点击 1 从抽屉点击 2 文件夹
	 * @type 0 功能入口 1 快捷方式
	 */
	public void onIconClick(String packageName, int from, int type) {
		PkgFirstCliskStatPreference mPrefernce = PkgFirstCliskStatPreference.getInstance();
		if (!mPrefernce.getBooleanValue(packageName)) {
			mPrefernce.setBooleanValue(packageName, true);
			StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
					ActionConstants.ACTION_LOG_2001, packageName, 5,
					from + "_" + type);
		} else {
			StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
					ActionConstants.ACTION_LOG_2001, packageName, StatManager.ACTION_CLICK,
					from + "_" + type);
		}
        
		// Utility.getInstance(mContext).launcherGameFolder(packageName);
	}

	/**
	 * 桌面点击游戏文件夹, 虚框文件夹事件统计
	 * 
	 * @folderId 游戏文件夹 -5999L表示新版游戏文件夹, 虚框文件夹ID
	 * @type 0 打开文件夹  1打开虚框文件夹
	 */
	public void onFolderClick(long folderId, int type) {
		if (folderId == GameManager.GAME_FOLDER_ID) {
            StatManager.getInstance().onAction(
					StatManager.TYPE_STORE_ACTION,
					GameFolderV2ActionConstants.ACTION_LOG_1712, null, 1, null);
		}
	}

	public boolean isAppStubFolderDeleteable() {
		return AppStubFolderManager.getInstance(mContext).isFolderOpratable();
	}

	/**
	 * 桌面点击虚框文件夹事件统计
	 * 
	 * @param folderId 虚框文件夹ID
	 * @param action 0 创建虚框文件夹  1打开虚框文件夹 2删除虚框文件夹
	 */
	public void onAppStubFolderAction(long folderId, int action) {
		NqLog.i("onAppStubFolderAction folderId= " + folderId + " ,action=" + action);
		if (action == 1) {
			AppStubFolderManager.getInstance(mContext).onFolderClick(folderId);
		} else if (action == 2) {
			AppStubFolderManager.getInstance(mContext).onFolderDelete(folderId);
		} else if (action == 0) {
			AppStubFolderManager.getInstance(mContext).onFolderCreate(folderId);
		}
	}
	/**
     * 桌面点击虚框事件统计
     * @resId  虚框应用ID 
     * @type  0点击虚框, 1删除虚框 , 3点击虚框文件夹里的虚框 
     */
    public void onAppStubClick(String resID, String packageName, int type){
    	if(type == 0){
            StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT, AppStubConstants.ACTION_LOG_2302, resID, 1, packageName);
    	} else if(type == 1){
            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION, AppStubConstants.ACTION_LOG_2306, resID, 1, packageName);
            EventBus.getDefault().post(new AppStubManager.AppStubDeleteEvent(resID, packageName, null));
    	} else if(type == 3){
            StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT, AppStubFolderConstants.ACTION_LOG_2605, resID, 1, packageName);
    	}
    }

	/** 角标展示
     * @intent  之前桌面收到的角标intent
     * @return true:统计成功  false:已统计过或者intent无效(如url和热塑Id为空)
     */
    public boolean onBandgeDisplay(Intent intent){
    	NqLog.e("onBandgeDisplay");
    	return BandgeManager.getInstance(mContext).onReceiveBandgeIntent(mContext, intent, 0);
    }
    
    /** 角标点击
     * @intent  之前桌面收到的角标intent
     * @return true:成功调起浏览器打开广告链接  false:intent无效(如url和热塑Id为空)
     */
    public boolean onBandgeClick(Context activityContext, Intent intent){
    	NqLog.e(intent.toURI());
    	return BandgeManager.getInstance(mContext).onReceiveBandgeIntent(activityContext, intent, 1);
    }
    
    public void onAppStubRemove(String appID){
    	// 行为日志统计;
    }
    
	/**
	 * action： 0 创建 1 打开 2 删除 3关闭游戏文件夹中推荐 4 准备打开
	 *        21	删除游戏文件夹的二次确认弹框出现
     * 		  22	删除游戏文件夹二次确认弹框删除按钮点击
     * 		  23	删除游戏文件夹二次确认弹框取消按钮点击
     * 		  24	删除游戏文件夹的二次确认弹框消失
	 */
	public void onFolderAction(long folderId, int action) {
	    if (folderId == GameManager.GAME_FOLDER_ID) {
	        GameManager.getInstance().onFolderAction(action);
	    }
	}

	/**
	 * 根据size返回游戏文件夹推荐数据
	 * 
	 * @param size
	 * @param listener
	 */
	public void getGameList(int size, AdvertisementListListener listener) {
		GameManager.getInstance().getGameList(size, listener);
	}

	/**
	 * 返回所有游戏文件夹推荐数据
	 * 
	 * @param listener
	 */
	public void getAllGameList(AdvertisementListListener listener) {
		GameManager.getInstance().getAllGameList(listener);
	}
	
	/**
	 * 返回pkgNames所对应的type
	 * 
	 * @param pkgName : 包名列表
     * @param isFirst : 是否首次分类
	 * @param listener
	 */
	public void getAppType(List<String> pkgNames, boolean isFirst, AppTypeListener listener){
		NqLog.i("pkgNames="+pkgNames + ",isFirst="+isFirst + ",listener="+listener);
		if (pkgNames == null || pkgNames.isEmpty()){
			NqLog.i("pkgNames is empty");
			return;
		}
		AppTypeManager.getInstance().getAppType(pkgNames,listener);
	}
	/**
	 * 主动请求SDK发送创建游戏文件夹的广播
	 * 
	 * @param String
	 *            packageName
	 */
	public void requestSendGameBroadcast(String packageName) {
		NqLog.i("requestSendGameBroadcast packageName=" + packageName);
		GameManager.getInstance().requestCreateGameFolder();
	}

	/** 点击游戏文件夹中游戏时 */
	public void onGameClick(long folderId, App mApp) {
		if (folderId == GameManager.GAME_FOLDER_ID) {
            StatManager.getInstance().onAction(
					StatManager.TYPE_AD_STAT,
					GameFolderV2ActionConstants.ACTION_LOG_1714, mApp.getStrId(), StatManager.ACTION_CLICK, null);
		}
	}
	
	/** 点击游戏文件夹中游戏时 */
	public void onGameFolderJoystickClick() {
        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
				GameFolderV2ActionConstants.ACTION_LOG_1725, null, 0, null);
	}
	
	/** 游戏推荐详情页资源展示 */
	public void onGameFolderDetailDisplay(App mApp) {
		NqLog.i("ACTION_LOG_1726:mApp="+mApp);
        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
				GameFolderV2ActionConstants.ACTION_LOG_1726, mApp.getStrId(), 0, mApp.getStrPackageName());
	}


	/** 下载游戏文件夹时会有进度更新 */
	public void onAppDownload(App mApp) {
        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
				GameFolderV2ActionConstants.ACTION_LOG_1715, mApp.getStrId(), 0, mApp.getStrPackageName());

		Long downloadId = downloadApp(mApp);
		
		if (downloadId != null) {
            StatManager.getInstance().onAction(
					StatManager.TYPE_STORE_ACTION,
					GameFolderV2ActionConstants.ACTION_LOG_1716, mApp.getStrId(), 0, mApp.getStrPackageName());
			Intent i = new Intent();
			i.setAction(LiveReceiver.ACTION_APP_UPDATE);
			i.putExtra("downloadid", downloadId);
			i.putExtra("app", mApp);
			mContext.sendBroadcast(i);
		}
	}

	public void onRelatedAppDownload(App mApp) {
		if (mApp == null){
			return;
		}
		
		if (mApp.isGpApp()) {
			GpUtils.viewDetail(mContext, mApp.getStrAppUrl());
		} else {
			downloadApp(mApp);
		}
	}
	
	private Long downloadApp(App mApp) {		
    	Long downloadId = null;
		try {	
			if (!NetworkUtils.isConnected(mContext)) {
				ToastUtils.toast(mContext, "nq_nonetwork");
				return null;
			}
			
			int downType = AppManager.STATUS_UNKNOWN;
			Status mStatus = AppManager.getInstance(mContext).getStatus(mApp);
			switch (mStatus.statusCode) {
			case AppManager.STATUS_UNKNOWN: // 按STATUS_NONE处理
			case AppManager.STATUS_NONE:
				downType = AppManager.STATUS_NONE;
				break;
			case AppManager.STATUS_DOWNLOADING:
				ToastUtils.toast(mContext, "nq_in_downloading");
				break;
			case AppManager.STATUS_PAUSED:
				downType = AppManager.STATUS_PAUSED;
				break;
			case AppManager.STATUS_INSTALLED:
				break;
			case AppManager.STATUS_DOWNLOADED:
                PackageUtils.installApp(mContext,mApp.getStrAppPath());
				break;
			default:
				break;
			}

        	if(AppManager.STATUS_NONE == downType) {
            	downloadId = AppManager.getInstance(mContext).downloadApp(mApp);
        	} else if(AppManager.STATUS_PAUSED == downType) {
        		downloadId = MyDownloadManager.getInstance(mContext).getDownloadId(mApp.getStrAppUrl());
                if (downloadId == null) {
                	downloadId = MyDownloadManager.getInstance(mContext).getDownloadIdByResID(mApp.getStrId());
                }
        		MyDownloadManager.getInstance(mContext).resumeDownload(downloadId);
        	}
        	
        	if(downloadId != null) {
        		if (NetworkUtils.isWifi(mContext)) {
        			ToastUtils.toast(mContext, "nq_start_download");
        		} else {
        			ToastUtils.toast(mContext, "nq_start_download_3g");
        		}
			}
		} catch (Exception e) {
			NqLog.e(e);
		}

		return downloadId;
	}
	
	/** 获取应用状态
	 *  STATUS_UNKNOWN = -1;//未知
	 *  STATUS_NONE = 0;//未安装，未下载
	 *  STATUS_DOWNLOADING = 1;//未安装，下载中
	 *  STATUS_PAUSED = 2;//未安装，下载暂停中
	 *  STATUS_DOWNLOADED = 3;//未安装，已下载
	 *  STATUS_INSTALLED = 4;//已安装 */
	public AppStatus getAppStatus(App app) {
		if(app == null || TextUtils.isEmpty(app.getStrId()))
			return null;
		Status mStatus = AppManager.getInstance(mContext).getStatus(app);
		AppStatus mAppStatus = new AppStatus();
		mAppStatus.setStatusCode(mStatus.statusCode);
        mAppStatus.setTotalBytes(mStatus.totalBytes);
        mAppStatus.setDownloadedBytes(mStatus.downloadedBytes);
        int percentage = 0;
        if(mStatus.totalBytes > 0){
        	percentage = (int) ((0.01 + mStatus.downloadedBytes)
                    / mStatus.totalBytes * 100);
        }
        mAppStatus.setDownloadProgress(percentage > 100 ? 100 : percentage);
        
        return mAppStatus;
	}
	
	private void checkShowUpdateInfo() {
    	IncrementUpdateManager mgr = IncrementUpdateManager.getInstance(mContext);
    	if (mgr.isReadyToPrompt() == true) {
    		mgr.promptUpdateInfo(new Handler());
    	}
	}

	/**
	 * 根据folderID返回虚框文件夹推荐数据,在桌面重启后从SDK中获取持久化数据
	 * 
	 * @param folderID
	 */
	public AppStubFolder getAppStubFolder(long folderID) {
		List<AppStubFolder> list = AppStubFolderManager.getInstance(mContext).getAppStubFolderListFromCache(0, folderID, true);
		if(list != null && !list.isEmpty())
			return list.get(0);
		return null;
	}
	
	/**
	 * 返回虚框文件夹推荐数据,在桌面重启后从SDK中获取持久化数据
	 * 
	 */
	public List<AppStubFolder> getAppStubFolderList() {
		return AppStubFolderManager.getInstance(mContext).getAppStubFolderListFromCache(0, 0, true);
	}
	
	/**
	 * 返回虚框应用的点击时查看详情Intent
	 * */
	public Intent getAppStubIntentByApp(App app) {
		return AppStubManager.getInstance(mContext).getAppStubIntent(app);
	}

    /**
     * 事件统计方法
     * @param type
     * @param desc
     * @param resourceId
     * @param actionType
     * @param scene
     */
    public void onAction(int type,String desc, String resourceId, int actionType, String scene){
        StatManager.getInstance().onAction(type,desc,resourceId,actionType,scene);

        if(desc.equals("1809")){
            MustInstallPreference.getInstance().setBooleanValue(MustInstallPreference.KEY_MUSI_INSTALL_ICON_CREATE,false);
        }
    }
    
    public void setCurrentWallpaperID(String id){
    	WallpaperManager.getInstance(mContext).setCurrentWallpaperID(id);
    }

    public void setCurrentThemeID(String id){
        ThemeManager.getInstance(mContext).setCurrentThemeID(id);
    }
    
    public void notifyLauncherReady(){
    	ApplicationContext.setLauncherReady(true);
    	
        Intent i = new Intent(mContext,BackgroundService.class);
        i.setAction(BackgroundService.IMMEDIATE_PERIOD_CHECK_ACTION);
        mContext.startService(i);
    }
    
    /**
     * @param type 参数1：0-自动添加到桌面，1-用户手动添加到桌面
     */
    public void addSearchToScreen(int type){
    	onAction(StatManager.TYPE_STORE_ACTION, SearcherActionConstants.ACTION_LOG_3101, null, 0, type + "");
    }
    
    public void deleteSearchFromScreen(){
    	onAction(StatManager.TYPE_STORE_ACTION, SearcherActionConstants.ACTION_LOG_3104, null, 0, null);
    }
    
    public void clickSearchBox(){
    	onAction(StatManager.TYPE_STORE_ACTION, SearcherActionConstants.ACTION_LOG_3102, null, 0, null);
    }
    
    public void intoHotwordPage(){
    	onAction(StatManager.TYPE_STORE_ACTION, SearcherActionConstants.ACTION_LOG_3105, null, 0, null);
    }
    
    public void hotwordRefreshButton(){
    	onAction(StatManager.TYPE_STORE_ACTION, SearcherActionConstants.ACTION_LOG_3106, null, 0, null);
    }
    
    /**
     * type:点击搜索热词  
     * @param type 参数1：0-widget底部的热词，1-热词页面里的热词 
     */
    public void clickHotword(String type){
    	onAction(StatManager.TYPE_STORE_ACTION, SearcherActionConstants.ACTION_LOG_3107, null, 0, type);
    }
    public void gotoSearchViewDetail(String keyWord){
    	gotoSearchViewDetail(keyWord, false);
    }
    /**
     * Goto 搜索
     * @param keyWord
     */
    public void gotoSearchViewDetail(String keyWord,boolean fromWeb) {
    	onAction(StatManager.TYPE_STORE_ACTION, SearcherActionConstants.ACTION_LOG_3103, null,fromWeb? 1: 0, null);
    	SearcherManager.getInstance(mContext).gotoViewDetail(keyWord);
    }

    public void getRecommendAppsByCategory(int category, int size,boolean byUser,RecommendAppsListener listener){
        CategoryFolderManager.getInstance().getRecommendAppsByCategory(category,size,byUser,listener);
    }

    /**
     * 分类文件夹事件统计
     * @param category
     * @param action 0 创建 1 从桌面打开 2 从tab页切换到
     */
    public void onCategoryFolderAction(int category, int action){
        CategoryFolderManager.getInstance().onCategoryFolderAction(category,action);
    }

    public CategoryFolderConfig getCategoryFolderConfig(){
        return new CategoryFolderConfig();
    }

    public void getAppStubByCategory(int category,AppStubListener listener){
    	CategoryFolderManager.getInstance().getAppStubByCategory(category, listener);
    }
    
    /**
     * 热词 搜索
     * @param hotWord
     * @param type 参数1：0-widget底部的热词，1-热词页面里的热词 
     */
    public void gotoHotwordDetail(String hotWord, String type) {
    	clickHotword(type);
    	SearcherManager.getInstance(mContext).gotoHotwordDetail(hotWord);
    }
    
    public void getHotwords(SearchHotwordsCallback searchHotwordsCallback){
    	 SearcherManager.getInstance(mContext).getHotwords(searchHotwordsCallback);
    }
    
	public void gotoStoreByCategory(int category) {
    	CategoryFolderManager.getInstance().gotoStoreByCategory(category);
    }
	
    public void createStoreEntryFromDesk() {
    	StoreEntryPreference.getInstance().setBooleanValue(StoreEntryPreference.KEY_STORE_ENTRY_CREATED, true);
	}
    /**
     * 分类文件夹虚框事件统计
     * @param category
     * @param params
     */
    public void onAppStubAction(int category, String resID, String packageName, int action, String params) {
        CategoryFolderManager.getInstance().onAppStubAction(category,resID, packageName,action,params);
    }
    //----------小组件相关---------
    /**
     * 
     * @param activity
     * @param widgetId
     * @param widgetName
     */
//    public void viewLqWidget(Activity activity, String widgetId, String widgetName){
//    	LqWidgetManager.getInstance().viewWidget(activity, widgetId, widgetName);
//    }
//    public void getLqWidgetList(LqWidgetListener listener){
//    	LqWidgetManager.getInstance().getLqWidgetList(listener);
//    }
    //------------
    
    public boolean canDeleteGameFolder() { 
        return true; 
    }
    
    /**
     * 增量更新主动点击弹框事件统计
     * @param type 0:升级弹框展示  1:用户点击以后再说 2:用户点击立即升级
     */
    public void onIncrementUpdateAction(int type) {
    	switch (type) {
    	case 0:
	        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
	        		IncrementUpdateConstants.ACTION_LOG_3004, null, 0, null);
	        break;
    	case 1:
	        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
	        		IncrementUpdateConstants.ACTION_LOG_3006, null, 0, null);
	        break;
    	case 2:
	        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
	        		IncrementUpdateConstants.ACTION_LOG_3005, null, 0, null);
	        break;
	    default:
	    	break;
    	}  	
    }
    //----------小组件相关---------
    /**
     * 
     * @param widgetId
     * @param widgetName
     */
    public void viewLqWidget(Activity activity, String widgetId, String widgetName){
    	LqWidgetManager.getInstance().viewWidget(activity, widgetId, widgetName);
    }
    public void getLqWidgetList(LqWidgetListener listener){
    	LqWidgetManager.getInstance().getLqWidgetList(listener);
    }
    //------------
        
    /**
     * 初始化字体管家sdk
     */
    public void initFontManager(Application application) {
    	FontManager.getInstance(mContext).initFontManager(application);
    	
    }

    public boolean isDefaultLauncher(){
        return KP.a(ApplicationContext.getContext());
    }

    public void setDefaultLaucnher(){
        KP.a(ApplicationContext.getContext(),true);
    }

    public void clearDefaultLauncher(){
        KP.b(ApplicationContext.getContext());
    }

    /**
     * 获取当天设置的字体ttf文件路径
     */
    public String getCurrentFontPath() {
    	return FontManager.getInstance(mContext).getCurrentFont();
    }
    
    public LayoutInflater getCustomLayoutInflator(LayoutInflater original){
    	if (FontFactory.hasSetDefaultTypeface()){
    		return new TypefaceLayoutInflater(original, mContext);
    	}
    	return original;
    }
}
