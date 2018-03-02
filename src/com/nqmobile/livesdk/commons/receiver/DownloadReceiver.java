package com.nqmobile.livesdk.commons.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.nqmobile.livesdk.commons.AppConstants;
import com.nqmobile.livesdk.commons.downloadmanager.DownloadManager;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.NqLog;
import com.nqmobile.livesdk.commons.mydownloadmanager.MyDownloadManager;
import com.nqmobile.livesdk.commons.service.BackgroundService;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppConstant;
import com.nqmobile.livesdk.modules.app.AppDetailActivity;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.installedrecommend.InstalledRecommendActivity;
import com.nqmobile.livesdk.modules.push.PushActionLogConstants;
import com.nqmobile.livesdk.modules.push.model.Push;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.modules.theme.Theme;
import com.nqmobile.livesdk.modules.theme.ThemeConstants;
import com.nqmobile.livesdk.modules.theme.ThemeDetailActivity;
import com.nqmobile.livesdk.modules.wallpaper.Wallpaper;
import com.nqmobile.livesdk.modules.wallpaper.WallpaperConstants;
import com.nqmobile.livesdk.modules.wallpaper.WallpaperDetailActivity;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.NetworkUtils;

public class DownloadReceiver extends BroadcastReceiver{
	public static final String ACTION_CLICK_PUSH = "com.nqmobile.live.click.push";
    public static final String ACTION_DOWNLOAD_PUSH = "com.nqmobile.live.download.push";
    public static final String ACTION_UNLIKE_PUSH = "com.nqmobile.live.unlike.push";
    /** 资源类型：0应用 1壁纸 2主题 */
	public static final String KEY_PUSH = "push";
	public static final String KEY_FROM_PUSH = "from_push";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		NqLog.i("DownloadReceiver receive action=" + action);
		if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
			long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
			NqLog.i("Download complete refer=" + reference);

            EventBus.getDefault().post(new DownloadCompleteEvent(reference));
		} else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            NqLog.i("network changed!");
            if (NetworkUtils.isConnected(context)) {
                Intent i = new Intent(context,BackgroundService.class);
				i.setAction(BackgroundService.REQUEST_PERIOD_CHECK_ACTION);
				context.startService(i);
			}
            EventBus.getDefault().post(new ConnectivityChangeEvent());
		} else if(action.equals(Intent.ACTION_PACKAGE_ADDED)){
            String packageName = intent.getDataString().substring(8);
            NqLog.i("installed packagename: " + packageName);
            EventBus.getDefault().post(new PackageAddedEvent(packageName));        
        } else if(action.equals(ACTION_DOWNLOAD_PUSH) || action.equals(ACTION_CLICK_PUSH)){
        	Push push = (Push) intent.getSerializableExtra(KEY_PUSH);
        	if(push == null){
        		NqLog.i("push is null when click item or click download btn");
        		return;
        	}
        	processPushClick(context, push);
        	cancelPushNotification(context, push.getStrId());
        	if(action.equals(ACTION_DOWNLOAD_PUSH))
                StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
                		PushActionLogConstants.ACTION_LOG_1905, push.getStrId(), StatManager.ACTION_CLICK, null);
        	else
                StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
                		PushActionLogConstants.ACTION_LOG_1902, push.getStrId(), StatManager.ACTION_CLICK, null);
        	
        } else if(action.equals(ACTION_UNLIKE_PUSH)){
        	Push push = (Push) intent.getSerializableExtra(KEY_PUSH);
        	if(push == null){
        		NqLog.i("push is null when click unlike push");
        		return;
        	}
        	String pushid = push.getStrId();
        	cancelPushNotification(context, pushid);
            StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
            		PushActionLogConstants.ACTION_LOG_1904, pushid, StatManager.ACTION_CLICK, null);
            NqLog.i("unlike push ,pushid: " + pushid);
        }else if(action.equals(Intent.ACTION_PACKAGE_REMOVED)){
            String packageName = intent.getDataString().substring(8);
            EventBus.getDefault().post(new PackageRemoveEvent(packageName));
        }else if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
            EventBus.getDefault().post(new BootCompletedEvent());
        }
	}

    private void cancelPushNotification(Context context, String pushid) {
		// TODO Auto-generated method stub
    	String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
		mNotificationManager.cancel(pushid.hashCode());
	}

	private void processPushClick(Context context, Push push) {
		// TODO Auto-generated method stub
		switch(push.getJumpType()){
		// push信息是应用广告的话,那么跳转事件应该是限制进入store详情,进入时,取不到广告详情.[白板]
		case 3://3--打开浏览器
			pushOpenBrowser(context, push.getDownUrl());
			break;
		case 0://0--打开store详情 
			/** 资源类型：0应用 1壁纸 2主题 */
			pushOpenStore(context, push);
			break;
		case 2://2 直接下载（本地下载）
			/** 资源类型：0应用 1壁纸 2主题 */
			String linkResId = push.getLinkResourceId();
			if(linkResId == null || linkResId.isEmpty()) {
				if(push.getDownUrl() == null || TextUtils.isEmpty(push.getDownUrl())){
		    		NqLog.i("push download url is null");
		    		return;
		    	} else {
		    		NqLog.i("pushDirectDownload url is:" + push.getDownUrl());
		    		pushDirectDownload(context, push);
                    StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
                    		PushActionLogConstants.ACTION_LOG_1903, push.getStrId(), StatManager.ACTION_CLICK, null);
		    	}
			} else {
				pushOpenDownload(context, push);
			}
			break;
		default:
			break;
		}
		
		
	}
	/**进入push的直接下载*/
	private void pushDirectDownload(Context context, Push push) {
		// TODO Auto-generated method stub
		String SDCardPath = CommonMethod.getSDcardPath(context);
        if(SDCardPath == null)
        	SDCardPath = CommonMethod.getSDcardPathFromPref(context);
        long longSize = -1;
        
        App app = new App();
		app.setStrId(push.getStrId());
		app.setStrAppUrl(push.getDownUrl());
		String strAppPath = new StringBuilder().append(SDCardPath).append(AppConstant.STORE_IMAGE_LOCAL_PATH_APP).toString();
        app.setStrAppPath(new StringBuilder().append(strAppPath).append(push.getStrId())
                .append(".apk").toString());
		app.setStrName(push.getTitle());
		app.setLongSize(longSize);
		app.setIntSourceType(AppConstants.STORE_MODULE_TYPE_PUSH);
		app.setIntDownloadActionType(AppManager.DOWNLOAD_ACTION_TYPE_DIRECT);
		app.setStrIconUrl(push.getIcon());
        app.setStrPackageName(push.getPackageName());
			
		Long downloadId = AppManager.getInstance(context).downloadApp(app);
	}

	/**进入push的下载store连接资源*/
	private void pushOpenDownload(Context context, Push push) {
		// TODO Auto-generated method stub
		String SDCardPath = CommonMethod.getSDcardPath(context);
        if(SDCardPath == null)
        	SDCardPath = CommonMethod.getSDcardPathFromPref(context);
        long longSize = -1;
		switch(push.getIntType()){
    	case AppConstants.PUSH_CACHE_TYPE_APP:
    		App app = new App();
			app.setStrId(push.getLinkResourceId());
			app.setStrAppUrl(push.getDownUrl());
			String strAppPath = new StringBuilder().append(SDCardPath).append(AppConstant.STORE_IMAGE_LOCAL_PATH_APP).toString();
			app.setStrAppPath(strAppPath);
			app.setStrName(push.getTitle());
			app.setLongSize(longSize);
			app.setIntSourceType(AppConstants.STORE_MODULE_TYPE_PUSH);
			
   			MyDownloadManager.getInstance(context).downloadApp(app);
            StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
            		PushActionLogConstants.ACTION_LOG_1903, push.getStrId(), StatManager.ACTION_CLICK, null);
   			break;
    	case AppConstants.PUSH_CACHE_TYPE_WALLPAPER:
    		Wallpaper wallpaper = new Wallpaper();
			wallpaper.setStrId(push.getLinkResourceId());
			wallpaper.setStrWallpaperUrl(push.getDownUrl());
			String strWallpaperPath = new StringBuilder().append(SDCardPath).append(WallpaperConstants.STORE_IMAGE_LOCAL_PATH_WALLPAPER).toString();
			wallpaper.setStrWallpaperPath(strWallpaperPath);
			wallpaper.setStrName(push.getTitle());
			wallpaper.setLongSize(longSize);

			MyDownloadManager.getInstance(context).downloadWallpaper(wallpaper);
            StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
            		PushActionLogConstants.ACTION_LOG_1903, push.getStrId(), StatManager.ACTION_CLICK, null);
			break;
    	case AppConstants.PUSH_CACHE_TYPE_THEME:
    		Theme theme = new Theme();
			theme.setStrId(push.getLinkResourceId());
			theme.setStrThemeUrl(push.getDownUrl());
			String strThemePath = new StringBuilder().append(SDCardPath).append(ThemeConstants.STORE_IMAGE_LOCAL_PATH_THEME).toString();
			theme.setStrThemePath(strThemePath);
			theme.setStrName(push.getTitle());
			theme.setLongSize(longSize);

			MyDownloadManager.getInstance(context).downloadTheme(theme);
            StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
            		PushActionLogConstants.ACTION_LOG_1903, push.getStrId(), StatManager.ACTION_CLICK, null);
			break;
    	}
	}

	/**进入push的浏览器详情*/
	private void pushOpenBrowser(Context context, String url){
		if(url == null || url.isEmpty()){
			NqLog.i("url is null or empty");
		}
		Uri uri = Uri.parse(url);
        Intent browser = new Intent(Intent.ACTION_VIEW, uri);
        browser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(browser);
	}
	
	/**进入push的store详情*/
	private void pushOpenStore(Context context, Push push){
		if(push == null)
			return;
    	NqLog.i("view push detail in store:type="+push.getIntType() + ",pushid="+push.getStrId() + ",resid="+push.getLinkResourceId());
    	Intent detailIntent = new Intent();
    	switch(push.getIntType()){
		case AppConstants.PUSH_CACHE_TYPE_APP:
			detailIntent.setClass(context, AppDetailActivity.class);
			detailIntent.setAction(AppDetailActivity.INTENT_ACTION);
			detailIntent.putExtra(AppDetailActivity.KEY_APP_ID, push.getLinkResourceId());
			detailIntent.putExtra(AppDetailActivity.KEY_APP_SOURCE_TYPE, AppConstants.STORE_MODULE_TYPE_PUSH);
			break;
		case AppConstants.PUSH_CACHE_TYPE_WALLPAPER:
			detailIntent.setClass(context, WallpaperDetailActivity.class);
			detailIntent.setAction(WallpaperDetailActivity.INTENT_ACTION);
            detailIntent.putExtra(DownloadReceiver.KEY_FROM_PUSH,true);
			detailIntent.putExtra(WallpaperDetailActivity.KEY_WALLPAPER_ID, push.getLinkResourceId());
			break;
		case  AppConstants.PUSH_CACHE_TYPE_THEME:
			detailIntent.setClass(context, ThemeDetailActivity.class);
			detailIntent.setAction(ThemeDetailActivity.INTENT_ACTION);
            detailIntent.putExtra(DownloadReceiver.KEY_FROM_PUSH,true);
			detailIntent.putExtra(ThemeDetailActivity.KEY_THEME_ID, push.getLinkResourceId());
			break;
		}
    	detailIntent.putExtra(KEY_FROM_PUSH, true);
    	detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	context.startActivity(detailIntent);
	}


}
