package com.nqmobile.livesdk.modules.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.DownloadManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.text.TextUtils;
import android.util.Log;

import com.nqmobile.livesdk.LauncherSDK;
import com.nqmobile.livesdk.OnUpdateListener;
import com.nqmobile.livesdk.commons.AppConstants;
import com.nqmobile.livesdk.commons.db.DataProvider;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.mydownloadmanager.IDownloadObserver;
import com.nqmobile.livesdk.commons.mydownloadmanager.MyDownloadManager;
import com.nqmobile.livesdk.commons.mydownloadmanager.table.DownloadTable;
import com.nqmobile.livesdk.commons.receiver.DownloadCompleteEvent;
import com.nqmobile.livesdk.commons.receiver.PackageAddedEvent;
import com.nqmobile.livesdk.commons.receiver.PackageInstallLogEvent;
import com.nqmobile.livesdk.modules.app.network.AppDetailProtocal.GetAppDetailSuccessEvent;
import com.nqmobile.livesdk.modules.app.network.AppListProtocol.GetAppListSuccessEvent;
import com.nqmobile.livesdk.modules.app.network.AppServiceFactory;
import com.nqmobile.livesdk.modules.app.table.AppCacheTable;
import com.nqmobile.livesdk.modules.app.table.AppLocalTable;
import com.nqmobile.livesdk.modules.appstub.AppStubConstants;
import com.nqmobile.livesdk.modules.appstubfolder.AppStubFolderConstants;
import com.nqmobile.livesdk.modules.apptype.model.AppTypeInfo;
import com.nqmobile.livesdk.modules.apptype.network.GetAppTypeProtocol.GetAppTypeSuccessEvent;
import com.nqmobile.livesdk.modules.association.AssociationActionConstants;
import com.nqmobile.livesdk.modules.banner.BannerConstants;
import com.nqmobile.livesdk.modules.categoryfolder.CategoryFolderConstants;
import com.nqmobile.livesdk.modules.installedrecommend.InstalledRecommendConstants;
import com.nqmobile.livesdk.modules.points.PointsManager;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.CollectionUtils;
import com.nqmobile.livesdk.utils.CursorUtils;
import com.nqmobile.livesdk.utils.FileUtil;
import com.nqmobile.livesdk.utils.GpUtils;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.NotificationUtil;
import com.nqmobile.livesdk.utils.PackageUtils;
import com.nqmobile.livesdk.utils.StringUtil;
import com.nqmobile.livesdk.utils.ToastUtils;

/**
 * 应用业务类
 *
 * @author changxiaofei
 * @time 2013-12-5 下午3:19:07
 */
public class AppManager extends AbsManager{
	private static final ILogger NqLog = LoggerFactory.getLogger(AppModule.MODULE_NAME);

	public static final int COLUMN_PICKS = 0;
    public static final int COLUMN_TOP_GAMES = 1;
    public static final int COLUMN_TOP_APPS = 2;
    public static final long GAME_CACHE_MAX_TIME = 24L * 60 * 60 * 1000;//24小时
    public static final long CACHE_MAX_TIME[] = {GAME_CACHE_MAX_TIME, GAME_CACHE_MAX_TIME, GAME_CACHE_MAX_TIME};//24小时
    private Context mContext;
    private static AppManager mInstance;
    private static final String STORE_APP_QUERY_BY_COLUMN = AppLocalTable.APP_SOURCE_TYPE + "="
            + AppConstant.STORE_MODULE_TYPE_MAIN + " AND "
            + AppLocalTable.APP_COLUMN + "=?";

    public static final int STATUS_UNKNOWN = -1;//未知
    public static final int STATUS_NONE = 0;//未安装，未下载
    public static final int STATUS_DOWNLOADING = 1;//未安装，下载中
    public static final int STATUS_PAUSED = 2;//未安装，下载暂停中
    public static final int STATUS_DOWNLOADED = 3;//未安装，已下载
    public static final int STATUS_INSTALLED = 4;//已安装

    /**
     * 如下常量定义来自服务器 Launcher服务的TAppResource:
     * downActionType, #详情页或应用简介中下载按钮的行为，9 无，1 打开本地Market（GP下载），2 直接下载（本地下载） 3打开浏览器（广告GP下载）
     */
    public static final int DOWNLOAD_ACTION_TYPE_NONE = 9;
    public static final int DOWNLOAD_ACTION_TYPE_GP = 1;
    public static final int DOWNLOAD_ACTION_TYPE_DIRECT = 2;
    public static final int DOWNLOAD_ACTION_TYPE_BROWSER = 3;

    public AppManager(Context context) {
        super();
        this.mContext = context.getApplicationContext();

        EventBus.getDefault().register(this);
    }

    public synchronized static AppManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AppManager(context);
        }
        return mInstance;
    }

    /**
     * 将cursor对象转换App
     *
     * @param cursor
     * @return
     */
    public App cursorToApp(Cursor cursor) {
        App app = new App();
        app.setStrId(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AppLocalTable.APP_ID))));
        app.setIntSourceType(cursor.getInt(cursor.getColumnIndex(AppLocalTable.APP_SOURCE_TYPE)));
        app.setStrName(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AppLocalTable.APP_NAME))));
        app.setStrPackageName(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AppLocalTable.APP_PACKAGENAME))));
        app.setStrDescription(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AppLocalTable.APP_DESCRIPTION))));
        app.setStrDevelopers(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AppLocalTable.APP_DEVELOPERS))));
        app.setStrCategory1(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AppLocalTable.APP_CATEGORY1))));
        app.setStrCategory2(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AppLocalTable.APP_CATEGORY2))));
        app.setStrVersion(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AppLocalTable.APP_VERSION))));
        app.setFloatRate(cursor.getFloat(cursor.getColumnIndex(AppLocalTable.APP_RATE)));
        app.setLongDownloadCount(cursor.getLong(cursor.getColumnIndex(AppLocalTable.APP_DOWNLOAD_COUNT)));
        app.setLongSize(cursor.getLong(cursor.getColumnIndex(AppLocalTable.APP_SIZE)));
        app.setStrIconUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AppLocalTable.APP_ICON_URL))));
        app.setStrIconPath(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AppLocalTable.APP_ICON_PATH))));
        app.setStrImageUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AppLocalTable.APP_IMAGE_URL))));
        app.setStrImagePath(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AppLocalTable.APP_IMAGE_PATH))));
        String previewUrl = cursor.getString(cursor.getColumnIndex(AppLocalTable.APP_PREVIEW_URL));
        if (!TextUtils.isEmpty(previewUrl)) {
            String[] s = previewUrl.split(";");
            List<String> list = new ArrayList<String>();
            for (int i = 0; i < s.length; i++) {
                list.add(s[i]);
            }
            app.setArrPreviewUrl(list);
        }
        String previewPath = cursor.getString(cursor.getColumnIndex(AppLocalTable.APP_PREVIEW_PATH));
        if (!TextUtils.isEmpty(previewPath)) {
            String[] s = previewPath.split(";");
            List<String> listPath = new ArrayList<String>();
            for (int i = 0; i < s.length; i++) {
                listPath.add(s[i]);
            }
            app.setArrPreviewPath(listPath);
        }
        app.setStrAppUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AppLocalTable.APP_URL))));
        app.setStrAppPath(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AppLocalTable.APP_PATH))));
        app.setIntClickActionType(cursor.getInt(cursor.getColumnIndex(AppLocalTable.APP_CLICK_ACTION_TYPE)));
        app.setIntDownloadActionType(cursor.getInt(cursor.getColumnIndex(AppLocalTable.APP_DOWNLOAD_ACTION_TYPE)));
        app.setLongUpdateTime(cursor.getLong(cursor.getColumnIndex(AppLocalTable.APP_UPDATETIME)));
        app.setLongLocalTime(cursor.getLong(cursor.getColumnIndex(AppLocalTable.APP_LOCALTIME)));
        app.setRewardPoints(cursor.getInt(cursor.getColumnIndex(AppLocalTable.APP_REWARDPOINTS)));
        app.setTrackId(cursor.getString(cursor.getColumnIndex(AppLocalTable.APP_TRACKID)));
        return app;
    }

    /**
     * 将app对象转换层数据库操作的ContentValues对象
     *
     * @param column 0：游戏；1：应用；
     * @param app
     * @return
     */
    public ContentValues appToContentValues(int column, App app) {
        ContentValues values = null;
        if (app != null) {
            values = new ContentValues();
            values.put(AppLocalTable.APP_ID, app.getStrId());
            values.put(AppLocalTable.APP_SOURCE_TYPE, app.getIntSourceType());
            values.put(AppLocalTable.APP_COLUMN, column);
            values.put(AppLocalTable.APP_TYPE, app.getType());
            values.put(AppLocalTable.APP_CATEGORY1, app.getStrCategory1());
            values.put(AppLocalTable.APP_CATEGORY2, app.getStrCategory2());
            values.put(AppLocalTable.APP_NAME, app.getStrName());
            values.put(AppLocalTable.APP_DESCRIPTION, app.getStrDescription());
            values.put(AppLocalTable.APP_DEVELOPERS, app.getStrDevelopers());
            values.put(AppLocalTable.APP_RATE, app.getFloatRate());
            values.put(AppLocalTable.APP_VERSION, app.getStrVersion());
            values.put(AppLocalTable.APP_SIZE, app.getLongSize());
            values.put(AppLocalTable.APP_DOWNLOAD_COUNT, app.getLongDownloadCount());
            values.put(AppLocalTable.APP_PACKAGENAME, app.getStrPackageName());
            values.put(AppLocalTable.APP_ICON_URL, app.getStrIconUrl());
            values.put(AppLocalTable.APP_IMAGE_URL, app.getStrImageUrl());
            //预览图网址
            StringBuilder previewUrl = new StringBuilder();
            List<String> previewUrls = app.getArrPreviewUrl();
            if (previewUrls != null && previewUrls.size() > 0) {
                for (int j = 0; j < previewUrls.size(); j++) {
                    previewUrl.append(previewUrls.get(j)).append(";");
                }
            }
            if (previewUrl.length() > 1) {
                values.put(AppLocalTable.APP_PREVIEW_URL, previewUrl.substring(0, previewUrl.length() - 1));
            } else {
                values.put(AppLocalTable.APP_PREVIEW_URL, "");
            }
            values.put(AppLocalTable.APP_URL, app.getStrAppUrl());
            values.put(AppLocalTable.APP_CLICK_ACTION_TYPE, app.getIntClickActionType());
            values.put(AppLocalTable.APP_DOWNLOAD_ACTION_TYPE, app.getIntDownloadActionType());
            values.put(AppLocalTable.APP_ICON_PATH, app.getStrIconPath());
            values.put(AppLocalTable.APP_IMAGE_PATH, app.getStrImagePath());
            //预览图本地路径
            StringBuilder previewPath = new StringBuilder();
            List<String> previewPaths = app.getArrPreviewPath();
            if (previewPaths != null && previewPaths.size() > 0) {
                for (int j = 0; j < previewPaths.size(); j++) {
                    previewPath.append(previewPaths.get(j)).append(";");
                }
            }
            if (previewPath.length() > 1) {
                values.put(AppLocalTable.APP_PREVIEW_PATH, previewPath.substring(0, previewPath.length() - 1));
            } else {
                values.put(AppLocalTable.APP_PREVIEW_PATH, "");
            }
            values.put(AppLocalTable.APP_PATH, app.getStrAppPath());
            values.put(AppLocalTable.APP_UPDATETIME, app.getLongUpdateTime());
            values.put(AppLocalTable.APP_LOCALTIME, app.getLongLocalTime());
            values.put(AppLocalTable.APP_REWARDPOINTS,app.getRewardPoints());
            values.put(AppLocalTable.APP_TRACKID,app.getTrackId());
        }
        return values;
    }

    public boolean isAppInstallByResId(String resId){        
        Cursor c = null;
        boolean result = false;
        String packagename = "";
        try{
            c = mContext.getContentResolver().query(AppCacheTable.APP_CACHE_URI,null,AppLocalTable.APP_ID + " = ?",new String[]{resId},null);
            if(c != null && c.moveToNext()){
                packagename = c.getString(c.getColumnIndex(AppCacheTable.APP_PACKAGENAME));
            }

            if(TextUtils.isEmpty(packagename)){
                c = mContext.getContentResolver().query(AppLocalTable.LOCAL_APP_URI,null,AppLocalTable.APP_ID + " = ?",new String[]{resId},null);
                if(c != null && c.moveToNext()){
                    packagename = c.getString(c.getColumnIndex(AppLocalTable.APP_PACKAGENAME));
                }
            }

            if(!TextUtils.isEmpty(packagename)){
                result = PackageUtils.isAppInstalled(mContext,packagename);
            }
        }finally {
            if(c != null){
                c.close();
            }
        }
        return result;
    }
    
    /**
     * 获取应用列表
     *
     * @param column
     * @param Listener
     */
    public void getAppList(final int column, final int offset, final AppListStoreListener listener) {
    	getAppList(column, offset, 0, listener);
    }
    
	private void getAppList(final int column, final int offset, int size, final AppListStoreListener listener) {
		//检查参数
		if (listener == null){
			return;
		}
		
		// 没有缓存，或缓存过期，联网查询数据。
		if (!NetworkUtils.isConnected(mContext)) {
			listener.onNoNetwork();
			return;
		}
		AppServiceFactory.getService().getAppList(mContext, column, offset, size, listener);
    }

    /**
     * 返回缓存的应用列表。
     *
     * @param column 0：新品；1：热门; 2:游戏文件夹 3:装机必备软件 4：装机必备游戏
     * @return
     */
    public List<App> getAppListFromCache(int column) {
        List<App> listApps = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;

        try {
        	if(column == 2) {
        		cursor = contentResolver.query(AppCacheTable.APP_CACHE_URI, null,
        	            AppLocalTable.APP_SOURCE_TYPE + "=" + AppConstants.STORE_MODULE_TYPE_GAME_AD,
                        null, AppLocalTable._ID + " asc");
            } else if(column == 3){
                cursor = contentResolver.query(AppCacheTable.APP_CACHE_URI, null,
                        AppLocalTable.APP_SOURCE_TYPE + "=" + AppConstants.STORE_MODULE_TYPE_MUST_INSTALL,
                        null, AppLocalTable._ID + " asc");
            } else {
        		cursor = contentResolver.query(AppCacheTable.APP_CACHE_URI, null,
        				STORE_APP_QUERY_BY_COLUMN,
        				new String[]{String.valueOf(column)}, AppLocalTable._ID
        				+ " asc");
        	}
            if (cursor != null && cursor.getCount() > 0) {
                listApps = new ArrayList<App>();
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    App app = cursorToApp(cursor);
                    listApps.add(app);
                }
            }
        } catch (Exception e) {
            NqLog.e(" getAppListFromCache " + e.toString());
        } finally {
        	CursorUtils.closeCursor(cursor);
        }
        return listApps;
    }

    /**
     * 检测是否有应用列表缓存数据
     *
     * @param column 0：新品；1：热门；2：游戏文件夹；
     * @return true:有缓存 false:无缓存
     */
    public boolean hadAvailableAppListCashe(int column) {
    	Cursor cursor = null;
        try {
            ContentResolver contentResolver = mContext.getContentResolver();
            if(column == 2) {
            	cursor = contentResolver.query(AppCacheTable.APP_CACHE_URI, 
                		new String[]{AppLocalTable.APP_ID}, AppLocalTable.APP_SOURCE_TYPE + "=" + AppConstants.STORE_MODULE_TYPE_GAME_AD, null, null);
            } else if(column == 3){
                cursor = contentResolver.query(AppCacheTable.APP_CACHE_URI,
                        new String[]{AppLocalTable.APP_ID}, AppLocalTable.APP_SOURCE_TYPE + "=" + AppConstants.STORE_MODULE_TYPE_MUST_INSTALL, null, null);
            } else {
            	cursor = contentResolver.query(AppCacheTable.APP_CACHE_URI, 
                		new String[]{AppLocalTable.APP_ID}, AppLocalTable.APP_SOURCE_TYPE + "=" + AppConstant.STORE_MODULE_TYPE_MAIN + " AND "
                		+ AppLocalTable.APP_COLUMN + "=" + column, null, null);
            }
            if (cursor != null && cursor.getCount() > 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	CursorUtils.closeCursor(cursor);
        }
        return false;
    }

    /**
     * 返回本地的应用列表。
     *
     * @return
     */
    public List<App> getAppListFromLocal(int column) {
    	List<App> listApps = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;

        try {
        	if(column == 2) {
        		cursor = contentResolver.query(AppLocalTable.LOCAL_APP_URI, null,
        	            AppLocalTable.APP_SOURCE_TYPE + "=" + AppConstants.STORE_MODULE_TYPE_GAME_AD,
                        null, AppLocalTable._ID + " asc");
            } else if(column == 3){
                cursor = contentResolver.query(AppLocalTable.LOCAL_APP_URI, null,
                        AppLocalTable.APP_SOURCE_TYPE + "=" + AppConstants.STORE_MODULE_TYPE_MUST_INSTALL,
                        null, AppLocalTable._ID + " asc");
            } else if(column == AppStubConstants.STORE_MODULE_TYPE_APPSTUB){
                cursor = contentResolver.query(AppLocalTable.LOCAL_APP_URI, null,
                        AppLocalTable.APP_SOURCE_TYPE + "=" + AppStubConstants.STORE_MODULE_TYPE_APPSTUB,
                        null, AppLocalTable._ID + " asc");
            } else {
        		cursor = contentResolver.query(AppLocalTable.LOCAL_APP_URI, null,
        				STORE_APP_QUERY_BY_COLUMN,
        				new String[]{String.valueOf(column)}, AppLocalTable._ID
        				+ " asc");
        	}
            if (cursor != null && cursor.getCount() > 0) {
                listApps = new ArrayList<App>();
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    App app = cursorToApp(cursor);
                    listApps.add(app);
                }
            }
        } catch (Exception e) {
            NqLog.e("getAppListFromLocal " + e.toString());
        } finally {
        	CursorUtils.closeCursor(cursor);
        }
        return listApps;
    }

    /**
     * 根据id 获取应用 详情。
     *
     * @param appId
     * @param listener
     */
    public void getAppDetail(final String appId, final AppDetailListener listener) {
        App app = getAppDetailFromCache(appId);
        if (app != null) {
            listener.onGetDetailSucc(app);
        } else {
        	AppServiceFactory.getService().getAppDetail(mContext, appId, listener);
        }
    }

    /**
     * 根据id 从缓存获取App详情。
     *
     * @param appId
     * @return App对象或null。null表示没有找到。
     */
    private App getAppDetailFromCache(String appId) {
        App app = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(AppCacheTable.APP_CACHE_URI, null,
                    AppLocalTable.APP_ID + "=?", new String[]{appId}, AppLocalTable._ID + " desc");
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToNext();
                app = cursorToApp(cursor);
            }
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
        	CursorUtils.closeCursor(cursor);
        }
        return app;
    }

    /**
     * 应用列表存入缓存库
     *
     * @param column 0：游戏；1：应用；2:游戏广告 3:装机比备软件 4：装机必备游戏
     * @param apps
     * @return
     */
    public boolean cacheApp(int column, int offset, List<App> apps) {
    	{
    		Throwable e = new Throwable();
    		e.setStackTrace(Thread.currentThread().getStackTrace());
    		NqLog.i("cacheApp:\n"+Log.getStackTraceString(e));
    	}
    	NqLog.i(String.format("cacheApp: column=%d, offset=%d", column,offset));
        try {
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            Builder b = null;
            
            if(offset == 0 && column != 2 && column != AppStubConstants.STORE_MODULE_TYPE_APPSTUB){
            	//清除缓存
            	String delSql = AppLocalTable.APP_SOURCE_TYPE + "=" + AppConstant.STORE_MODULE_TYPE_MAIN + " AND " +
                        AppLocalTable.APP_COLUMN + "=" + column;
            	NqLog.i("delSql_1 = "+delSql);
                b = ContentProviderOperation.newDelete(AppCacheTable.APP_CACHE_URI)
                        .withSelection(delSql, null);
                
                ops.add(b.build());
            }

            if(column == 2){
            	String delSql = AppLocalTable.APP_SOURCE_TYPE + "=" + AppConstants.STORE_MODULE_TYPE_GAME_AD;
            	NqLog.i("delSql_2 = "+delSql);
                b = ContentProviderOperation.newDelete(AppCacheTable.APP_CACHE_URI)
                        .withSelection(delSql, null);
                ops.add(b.build());
            }

            if(column == 3){
            	String delSql =AppLocalTable.APP_SOURCE_TYPE + "=" + AppConstants.STORE_MODULE_TYPE_MUST_INSTALL;
            	NqLog.i("delSql_3 = "+delSql);
                b = ContentProviderOperation.newDelete(AppCacheTable.APP_CACHE_URI)
                        .withSelection(delSql,null);
                ops.add(b.build());
            }
/*
            if(column == ThemeConstants.STORE_MODULE_TYPE_APPSTUB){
                b = ContentProviderOperation.newDelete(AppLocalTable.APP_CACHE_URI)
                        .withSelection(AppLocalTable.APP_SOURCE_TYPE + "=" + ThemeConstants.STORE_MODULE_TYPE_APPSTUB,null);
                ops.add(b.build());
            }
*/
            ContentResolver contentResolver = mContext.getContentResolver();
            
            if (apps != null && apps.size() > 0) {
                App app = null;
                long localTime = new Date().getTime();
                for (int i = 0; i < apps.size(); i++) {
                    app = apps.get(i);
                    if (app != null) {
                        app.setLongLocalTime(localTime);
                        ContentValues values = appToContentValues(column, app);
                        b = ContentProviderOperation.newInsert(AppCacheTable.APP_CACHE_URI).withValues(values);
                        ops.add(b.build());
                    }
                }
            }

            contentResolver.applyBatch(DataProvider.DATA_AUTHORITY,ops);

            //更新缓存时间。
            if(column == 0|| column == 1){
            	AppPreference.getInstance().setLongValue(AppPreference.KEY_APP_LIST_CACHE_TIME[column], new Date().getTime());
            }
            return true;
        } catch (Exception e) {
            NqLog.e("saveAppCache error " + e.toString());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 应用 数据存入本地库
     *
     * @param app
     * @return
     */
    private void saveApp(App app) {
        try {
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            Builder deleteOp = ContentProviderOperation.newDelete(
                    AppLocalTable.LOCAL_APP_URI).withSelection(
                    AppLocalTable.APP_ID + " = ?",
                    new String[]{app.getStrId()});
            ops.add(deleteOp.build());

            app.setLongLocalTime(System.currentTimeMillis());
            ContentValues values = appToContentValues(-1,
                    app);
            Builder insertOp = ContentProviderOperation.newInsert(
                    AppLocalTable.LOCAL_APP_URI)
                    .withValues(values);
            ops.add(insertOp.build());

            mContext.getContentResolver().applyBatch(
            		DataProvider.DATA_AUTHORITY, ops);
            //不需要保存icon、预览图到本地文件夹
        } catch (Exception e) {
            NqLog.e(e);
        }
    }

    public static int getCount(List<App> apps) {
        if (null == apps)
            return 0;
        int count = 0;
        for (int i = 0; i < apps.size(); i++) {
            if (null != apps.get(i) &&
                    !TextUtils.isEmpty(apps.get(i).getStrId()))
                count++;
        }
        NqLog.d("app count=" + count);
        return count;
    }
    
    
    public static class Status {
        public int statusCode;
        public long downloadedBytes;
        public long totalBytes;
    }
    
    /**
     * 返回App的下载、安装状态
     *
     * @param app
     * @return 总是返回非null
     * @author chenyanmin
     * @time 2014-1-23 上午11:06:17
     */
    public Status getStatus(App app) {
        Status result = new Status();
        result.statusCode = STATUS_UNKNOWN;

        if (app == null) {
            return null;
        }

        if (PackageUtils.isAppInstalled(mContext, app.getStrPackageName())) {
            result.statusCode = STATUS_INSTALLED;
        } else if (isDirectDownload(app)) {
            result.statusCode = STATUS_NONE;//默认状态是未安装、未下载
            MyDownloadManager downloader = MyDownloadManager.getInstance(mContext);
            Long downloadId = downloader.getDownloadId(app.getStrAppUrl());
            if (downloadId == null) {
            	downloadId = downloader.getDownloadIdByResID(app.getStrId());
            }
            NqLog.d("AppManager downloadId:" + downloadId + ", name:" + app.getStrName());
            if (downloadId != null) {
                int[] bytesAndStatus = downloader.getBytesAndStatus(downloadId);
                NqLog.i("AppManager " + app.getStrName()+" staus:" + bytesAndStatus[0] +","  + bytesAndStatus[1] +","  + bytesAndStatus[2] +","  + bytesAndStatus[3] +"]");
                if (bytesAndStatus[0] == 1) {
                    result.statusCode = convertStatus(bytesAndStatus[1]);
                    if(result.statusCode == STATUS_DOWNLOADED) {
                    	String filePath = app.getStrAppPath();
                    	if (!TextUtils.isEmpty(filePath)){
                            File file = new File(filePath);
                            if (!file.exists()){
                            	result.statusCode = STATUS_NONE;
                            }
                        }
                    }
                    result.downloadedBytes = bytesAndStatus[2];
                    result.totalBytes = bytesAndStatus[3];
                }
            }
        }
        return result;
    }

    private int convertStatus(int status) {
        int result = STATUS_UNKNOWN;

        switch (status) {
            case MyDownloadManager.STATUS_SUCCESSFUL:
                result = STATUS_DOWNLOADED;
                break;
            case MyDownloadManager.STATUS_RUNNING:
                result = STATUS_DOWNLOADING;
                break;
            case MyDownloadManager.STATUS_PENDING:
                result = STATUS_DOWNLOADING;
                break;
            case MyDownloadManager.STATUS_FAILED:
                result = STATUS_NONE;
                break;
            case MyDownloadManager.STATUS_NONE:
                result = STATUS_NONE;
                break;
            case MyDownloadManager.STATUS_PAUSED:
                result = STATUS_PAUSED;
                break;
            default:
                break;
        }

        return result;
    }

    /**
     * 下载App的apk
     *
     * @param app App对象
     * @author chenyanmin
     * @time 2014-1-23 下午2:39:51
     */
    public Long downloadApp(App app) {
        return downloadApp(app, DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
    }

    /**
     * 支持指定网络类型的下载app方法
     * @param app
     * @param networkflag
     * @return
     */
    public Long downloadApp(App app,int networkflag) {
        NqLog.i("downloadApp app="+app.getStrName());
        Long result = 0l;

        if (app == null || TextUtils.isEmpty(app.getStrAppUrl())) {
            return result;
        }

        if (!NetworkUtils.isConnected(mContext)) {
        	ToastUtils.toast(mContext, "nq_nonetwork");
            return result;
        }

        try {
            Uri url = Uri.parse(app.getStrAppUrl());
            Intent intent = null;
            switch (app.getIntDownloadActionType()) {
                case DOWNLOAD_ACTION_TYPE_BROWSER:
                    intent = new Intent(Intent.ACTION_VIEW, url);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    break;
                case DOWNLOAD_ACTION_TYPE_DIRECT:
                    int status = getStatus(app).statusCode;
                    if(status == STATUS_DOWNLOADED){
                        OnDownloadComplete(app);
                    }else{
                        Long downloadId = MyDownloadManager.getInstance(mContext).downloadApp(app,networkflag);
                        if (downloadId != null) {
                            saveApp(app);
                            result = downloadId;
                        }
                    }
                    break;
                case DOWNLOAD_ACTION_TYPE_GP:
                    intent = new Intent(Intent.ACTION_VIEW, url);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    break;
                case DOWNLOAD_ACTION_TYPE_NONE:
                    break;
            }
        } catch (Exception e) {
            NqLog.e(e);
        }

        return result;
    }

    /**
     * 是否直接下载
     *
     * @param app
     * @return
     * @author chenyanmin
     * @time 2014-1-23 下午5:20:52
     */
    private boolean isDirectDownload(App app) {
        return app.getIntDownloadActionType() == DOWNLOAD_ACTION_TYPE_DIRECT;
    }

    /**
     * 注册下载进度监听
     *
     * @param app
     * @param observer
     * @author chenyanmin
     * @time 2014-1-23 下午5:43:00
     */
    public void registerDownloadObserver(App app, IDownloadObserver observer) {
        if (app == null || observer == null) {
            return;
        }

        if (isDirectDownload(app)) {
            MyDownloadManager downloader = MyDownloadManager.getInstance(mContext);
            Long downloadId = downloader.getDownloadId(app.getStrAppUrl());
            if(downloadId == null)
            	downloadId = downloader.getDownloadIdByResID(app.getStrId());
            downloader.registerDownloadObserver(downloadId, observer);
//            downloader.registerDownloadObserverUri(app.getStrAppUrl(), observer);
        }
    }

    /**
     * 取消下载进度监听
     *
     * @param app
     * @author chenyanmin
     * @time 2014-1-23 下午5:43:29
     */
    public void unregisterDownloadObserver(App app) {
        if (app == null) {
            return;
        }

        if (isDirectDownload(app)) {
            MyDownloadManager downloader = MyDownloadManager.getInstance(mContext);
            Long downloadId = downloader.getDownloadId(app.getStrAppUrl());
            downloader.unregisterDownloadObserver(downloadId);
        }

    }

    /**
     * 根据ClickActionType打开App详情页
     *
     * @param app
     * @author chenyanmin
     * @time 2014-1-26 下午9:40:53
     */
    public void viewAppDetail(int column, App app) {
        if (app == null) {
            return;
        }

        try {
            Uri url = Uri.parse(app.getStrAppUrl());
            NqLog.d("viewAppDetail appurl:" + app.getStrAppUrl() + ", type:" + app.getIntClickActionType());
            Intent intent = null;
            switch (app.getIntClickActionType()) {
                case App.CLICK_ACTION_TYPE_BROWSER:
                    GpUtils.viewDetail(mContext, app.getStrAppUrl());
                    break;
                case App.CLICK_ACTION_TYPE_DIRECT:
                    intent = new Intent(mContext, AppDetailActivity.class);
                    intent.putExtra(AppDetailActivity.KEY_APP, app);
                    intent.putExtra(AppDetailActivity.KEY_COLUMN,column);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    break;
                case App.CLICK_ACTION_TYPE_GP:
                    GpUtils.viewDetail(mContext, app.getStrAppUrl());
                    break;
                case App.CLICK_ACTION_TYPE_NONE:
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            NqLog.e(e);
        }
    }

    private App getSavedApp(String appId) {
        App result = null;
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = mContext.getContentResolver();
            cursor = contentResolver.query(AppLocalTable.LOCAL_APP_URI, null, AppLocalTable.APP_ID + " = ?", new String[]{appId}, AppLocalTable._ID + " DESC");
            if (cursor != null && cursor.moveToFirst()) {
                result = cursorToApp(cursor);
            }
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
        	CursorUtils.closeCursor(cursor);
        }

        return result;
    }

    public void OnDownloadComplete(String appId) {
        if (TextUtils.isEmpty(appId)) {
            return;
        }

        App app = getSavedApp(appId);
        OnDownloadComplete(app);
    }

    public void OnDownloadComplete(App app){
        if (app == null) {
            return;
        }
        PackageUtils.installApp(mContext, app.getStrAppPath());
    }

    public boolean isCacheExpired(int column){
        Long cacheTime = 0l;
        cacheTime = AppPreference.getInstance().getLongValue(AppPreference.KEY_APP_LIST_CACHE_TIME[column]);
        Long currentTime = new Date().getTime();
    	return currentTime - cacheTime > CACHE_MAX_TIME[column];
    }
    
    
    /**获取应用类型和包名 return: "3_pkgname "*/
    public String getAppSourceType(String appId) {
    	String result = "";
    	if (TextUtils.isEmpty(appId)) {
            return result;
        }

    	ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;
        try {
        	cursor = contentResolver.query(AppLocalTable.LOCAL_APP_URI, new String[]{AppLocalTable.APP_SOURCE_TYPE, AppLocalTable.APP_PACKAGENAME},
        			AppLocalTable.APP_ID + " = ?", new String[]{appId}, AppLocalTable._ID + " desc");
                if (cursor != null && cursor.getCount() > 0) {
                	cursor.moveToNext();
                	int type = convertType(cursor.getInt(cursor.getColumnIndex(AppLocalTable.APP_SOURCE_TYPE)));
                	result = type + "_" +
                			StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AppLocalTable.APP_PACKAGENAME)));
                }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	CursorUtils.closeCursor(cursor);
        }
        return result;
    }
    
    // 1-store，2-游戏文件夹，3-装机必备，4-PUSH，5-虚框应用， 6-banner，7-积分中心，8-关联推荐，9-虚框文件夹, 10- 安装后关联推荐
    // 11-分类文件夹底部推荐. 12-分类文件夹虚框
    public static int convertType(int sourceType) {
    	int result = -1;
        switch (sourceType) {
        case AppConstant.STORE_MODULE_TYPE_MAIN:
    		result = 1;
    		break;
        case AppConstants.STORE_MODULE_TYPE_GAME_AD:
    		result = 2;
    		break;
        case AppConstants.STORE_MODULE_TYPE_MUST_INSTALL:
    		result = 3;
    		break;
    	case AppConstants.STORE_MODULE_TYPE_PUSH:
    		result = 4;
    		break;
    	case AppStubConstants.STORE_MODULE_TYPE_APPSTUB:
    		result = 5;
    		break;    	
    	case BannerConstants.STORE_MODULE_TYPE_BANNER:
    		result = 6;
    		break;
    	case AppConstants.STORE_MODULE_TYPE_POINTS_CENTER:
    		result = 7;
    		break;
    	case AssociationActionConstants.APP_SOURCE_TYPE:
    		result = 8;
    		break;    		
    	case AppStubFolderConstants.STORE_MODULE_TYPE_STUB_FOLDER:
    		result = 9;
    		break;
    	case InstalledRecommendConstants.APP_SOURCE_TYPE:
    		result = 10;
    		break;    	
    	case CategoryFolderConstants.DOWNLOAD_SOURCE_TYPE_BOTEM:
    		result = 11;
    		break;
    	case CategoryFolderConstants.DONWLOAD_SOURCE_TYPE_APPSTUB:
    		result = 12;
    		break;      		
    	default:
    		result = -1;
    		break;
    	}
		return result;
	}

    public void registerApp(Long downloadId, App mApp) {
    	final App app = mApp;
    	final Long downId = downloadId;
    	MyDownloadManager.getInstance(mContext).registerDownloadObserver(downloadId, new IDownloadObserver(){
			@Override
			public void onChange() {
				updateProgress(downId, app);
			}
		});
	}

    private void unregisterApp(Long downloadId, App app){
    	NqLog.i("AppManager unregisterApp : downId=" + downloadId + ", app=" +  app.getStrId()+"/"+app.getStrName());
    	MyDownloadManager.getInstance(mContext).unregisterDownloadObserver(downloadId);
    }

    private void updateProgress(Long downloadId, App mApp) {
		NqLog.d("AppManager updateProgress: pid=" + android.os.Process.myPid() + ",uid=" + android.os.Process.myUid()
				+ ",CallingPid=" + Binder.getCallingPid() + ",CallingUid=" + Binder.getCallingUid());
        Status mStatus = getStatus(mApp);
        AppStatus mAppStatus = new AppStatus();
        mAppStatus.setTotalBytes(mStatus.totalBytes);
        mAppStatus.setStatusCode(mStatus.statusCode);
       	NqLog.d(mApp.toString());
        NqLog.d("statusCode=" + mStatus.statusCode + ",downloadedBytes=" + mStatus.downloadedBytes + ",totalBytes" + mStatus.totalBytes);
        switch (mStatus.statusCode) {
            case AppManager.STATUS_UNKNOWN: //按STATUS_NONE处理
            case AppManager.STATUS_NONE:
                break;
            case AppManager.STATUS_DOWNLOADING:
                if (mStatus.totalBytes > 0 && mStatus.downloadedBytes >= 0
                        && mStatus.downloadedBytes <= mStatus.totalBytes) {

                    int percentage = (int) ((0.01 + mStatus.downloadedBytes)
                            / mStatus.totalBytes * 100);
                    mAppStatus.setDownloadedBytes(mStatus.downloadedBytes);
                    mAppStatus.setDownloadProgress(percentage);
                    if (percentage == 100) {
                    	try {
                    		ToastUtils.toast(mContext, "nq_label_download_success");
						} catch (Exception e) {
							e.printStackTrace();
						}
                    }
                } else {//pending状态
                	mAppStatus.setDownloadedBytes(mStatus.downloadedBytes);
                    mAppStatus.setDownloadProgress(0);
                }
                break;
            case AppManager.STATUS_PAUSED:
                if (mStatus.totalBytes > 0 && mStatus.downloadedBytes >= 0
                        && mStatus.downloadedBytes <= mStatus.totalBytes) {
                    int percentage = (int) ((0.01 + mStatus.downloadedBytes)
                            / mStatus.totalBytes * 100);
                    mAppStatus.setDownloadedBytes(mStatus.downloadedBytes);
                    mAppStatus.setDownloadProgress(percentage);
                } else {//pending状态
                	mAppStatus.setDownloadedBytes(mStatus.downloadedBytes);
                    mAppStatus.setDownloadProgress(0);
                }
                break;
            case AppManager.STATUS_DOWNLOADED:
            	mAppStatus.setDownloadedBytes(mStatus.downloadedBytes);
                mAppStatus.setDownloadProgress(100);
                break;
            case AppManager.STATUS_INSTALLED:
            	unregisterApp(downloadId, mApp);
                break;
            default:
                break;
        }
        NqLog.d("AppManager " + downloadId + " =downloadId AppStub STATUS: " + mAppStatus);
        
        sendAppProgress(mApp, mAppStatus);
        if(mAppStatus.downloadProgress >= 100){
        	unregisterApp(downloadId, mApp);
        }
    }

    private void sendAppProgress(App app, AppStatus status){
    	List<OnUpdateListener> listeners = LauncherSDK.getInstance(mContext).getRegisteredOnUpdateListeners();
		if(listeners != null && listeners.size() > 0){
			for(OnUpdateListener l:listeners){
				l.onAppStatusUpdate(app, status);
			}
		}
    }

    // 重启后重新注册下载进度监听
    public void registerAppAfterReboot(int column) {
		List<App> apps = getAppListFromLocal(column);
		if(apps == null || apps.size() == 0)
			return;
		for(App app:apps){
			if(app != null){
				int statusCode = getStatus(app).statusCode;
				if(statusCode == AppManager.STATUS_INSTALLED ||
						statusCode == AppManager.STATUS_DOWNLOADED){
					continue;
				} else {
					long downId = MyDownloadManager.getInstance(mContext).getDownloadId(app.getStrAppUrl());
					registerApp(downId, app);
				}
			}
		}
	}

	public String getAppNameByResId(String resId){
		String result = PointsManager.getInstance(mContext).getDownloadPointsAppNameByResId(resId);
        if (!TextUtils.isEmpty(result)){
        	return result;
        }
        Cursor c = null;
        try{
            c = mContext.getContentResolver().query(AppCacheTable.APP_CACHE_URI,null,AppLocalTable.APP_ID + " = ?",new String[]{resId},null);
            if(c != null && c.moveToNext()){
                result = c.getString(c.getColumnIndex(AppLocalTable.APP_NAME));
            }
            if(TextUtils.isEmpty(result)){
           	 c = mContext.getContentResolver().query(AppLocalTable.LOCAL_APP_URI,null,AppLocalTable.APP_ID + " = ?",new String[]{resId},null);
                if(c != null && c.moveToNext()){
                    result = c.getString(c.getColumnIndex(AppLocalTable.APP_NAME));
                }
           }
        }finally {
			CursorUtils.closeCursor(c);
        }
        return result;
    }
	
    public String getAppNameByPackName(String packageName){
        String result = PointsManager.getInstance(mContext).getDownloadPointsAppNameByPackName(packageName);

        NqLog.d("getAppNameByPackName 11 packageName:" + packageName + " result:" + result);
        if (!TextUtils.isEmpty(result)){
        	return result;
        }
        Cursor c = null;
        try{
            c = mContext.getContentResolver().query(AppCacheTable.APP_CACHE_URI,null,AppLocalTable.APP_PACKAGENAME + " = ?",new String[]{packageName},null);
            if(c != null && c.moveToNext()){
                result = c.getString(c.getColumnIndex(AppLocalTable.APP_NAME));
            }
            NqLog.d("getAppNameByPackName 22 packageName:" + packageName + " result:" + result);
            if(TextUtils.isEmpty(result)){
            	 c = mContext.getContentResolver().query(AppLocalTable.LOCAL_APP_URI,null,AppLocalTable.APP_PACKAGENAME + " = ?",new String[]{packageName},null);
                 if(c != null && c.moveToNext()){
                     result = c.getString(c.getColumnIndex(AppLocalTable.APP_NAME));
                 }
            }
            NqLog.d("getAppNameByPackName 33 packageName:" + packageName + " result:" + result);
        }finally {
			CursorUtils.closeCursor(c);
        }
        return result;
    }
    
    private boolean isStoreResource(String packageName){
        boolean result = false;
        Cursor c = null;
        try{
            c = mContext.getContentResolver().query(AppCacheTable.APP_CACHE_URI,null,AppLocalTable.APP_PACKAGENAME + " = ?",new String[]{packageName},null);
            if(c != null && c.moveToNext()){
                result = true;
            } else {
                c = mContext.getContentResolver().query(AppLocalTable.LOCAL_APP_URI,null,AppLocalTable.APP_PACKAGENAME + " = ?",new String[]{packageName},null);
                if(c != null && c.moveToNext()){
                    result = true;
                }
            }
        }finally {
			CursorUtils.closeCursor(c);
        }
        return result;
    }
    
    private String processAppInstall(Context context,String packagename){
    	String resourceId = null;
    	Cursor cursor = null;
    	try {
    		cursor = context.getContentResolver().query(AppCacheTable.APP_CACHE_URI,
    				null,
    				AppCacheTable.APP_PACKAGENAME + " = ?",
    				new String[]{packagename},null);
    		if(cursor != null && cursor.moveToNext()){
    			resourceId = cursor.getString(cursor.getColumnIndex(AppCacheTable.APP_ID));
    		}    		
		} catch (Exception e) {
            NqLog.e(e);
		}finally{
			CursorUtils.closeCursor(cursor);
		}

		if (resourceId == null) {
	    	try {
	    		cursor = context.getContentResolver().query(AppLocalTable.LOCAL_APP_URI,
	    				null,
	    				AppLocalTable.APP_PACKAGENAME + " = ?",
	    				new String[]{packagename},null);
	    		if(cursor != null && cursor.moveToNext()){
	    			resourceId = cursor.getString(cursor.getColumnIndex(AppLocalTable.APP_ID));
	    		}
	    	} catch (Exception e) {
	            NqLog.e(e);
			}finally{
				CursorUtils.closeCursor(cursor);
			}	    		
		}
		
        if(resourceId != null){
        	try {
        		cursor = context.getContentResolver().query(DownloadTable.TABLE_URI,
        				null,
        				DownloadTable.DOWNLOAD_RES_ID + " = ?",
        				new String[]{resourceId},
        				null);
        		if(cursor != null && cursor.moveToNext()){
        			String destpath = cursor.getString(cursor.getColumnIndex(DownloadTable.DOWNLOAD_DEST_PATH));
        			File file = new File(destpath);
        			if(file.exists()){
        				file.delete();
        			}
        		}
			} catch (Exception e) {
	            NqLog.e(e);
			}finally{
				CursorUtils.closeCursor(cursor);
			}
        }
        return resourceId;
    }
	
	private void processStoreDownload(Context context,long refer){
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(DownloadTable.TABLE_URI, null,
                    DownloadTable.DOWNLOAD_DOWNLOAD_ID + " = " + refer, null,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                int type = cursor.getInt(cursor.getColumnIndex(DownloadTable.DOWNLOAD_TYPE));
                String resId = cursor.getString(cursor.getColumnIndex(DownloadTable.DOWNLOAD_RES_ID));
                String destPath = cursor.getString(cursor.getColumnIndex(DownloadTable.DOWNLOAD_DEST_PATH));
                if (FileUtil.isFileExists(destPath) && !TextUtils.isEmpty(resId)){//确保文件存在
                    switch(type) {
                        case MyDownloadManager.DOWNLOAD_TYPE_APP:{
                        	NotificationUtil.showInstallAppNotifi(context,resId,destPath);
                        	AppManager.getInstance(context).OnDownloadComplete(resId);
                        	StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
                        			AppActionConstants.ACTION_LOG_1502, resId, StatManager.ACTION_DOWNLOAD,
                        			"0_" + AppManager.getInstance(context).getAppSourceType(resId)+"_1");
                        	
                        	break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
			CursorUtils.closeCursor(cursor);
        }
    }
	
    
	@Override
	public void init() {
		EventBus.getDefault().register(this);
	}
	
	public void onEvent(GetAppDetailSuccessEvent event) {
		AppDetailListener listener = (AppDetailListener) event.getTag();
		if (event.isSuccess()) {
			listener.onGetDetailSucc(event.getApp());
		}else{
			listener.onErr();
		}
	}
	
	public void onEvent(GetAppListSuccessEvent event) {
		AppListStoreListener listener = (AppListStoreListener) event.getTag();
		try {
			if(event.isSuccess()){
				listener.onGetAppListSucc(event.getColumn(), event.getOffset(),
					event.getApps());
			}else {
				listener.onErr();
			}
		} catch (Exception e) {
			NqLog.e(e);
		}
		
		// Store里的应用推荐板块下发的游戏，都应该进入AppType表里存储着，安装后就能出现在游戏文件夹了
		List<App> apps = event.getApps();
		if (event.getColumn() == 0 && CollectionUtils.isNotEmpty(apps)) {//column 0：游戏；1：应用
			List<AppTypeInfo> appTypeList = new ArrayList<AppTypeInfo>(
					apps.size());
			for (App app : apps) {
				AppTypeInfo appType = new AppTypeInfo();
				appType.setPackageName(app.getStrPackageName());
				appType.setCode(200);
				appTypeList.add(appType);
			}

			EventBus.getDefault().post(
					new GetAppTypeSuccessEvent(appTypeList, null));
		}

	}
	
	public void onEvent(PackageAddedEvent event) {
		String resourceId = processAppInstall(mContext, event.getPackageName());
		if(isStoreResource(event.getPackageName())){
            NotificationUtil.cancleNoti(mContext,NotificationUtil.NOTIF_ID_INSTALL_APP);
            NotificationUtil.showLauncherAppNotifi(mContext, event.getPackageName());

            EventBus.getDefault().post(new PackageInstallLogEvent(event.getPackageName(), resourceId));
		}
	}
	
	public void onEvent(DownloadCompleteEvent event) {
		processStoreDownload(mContext, event.getRefer());
	}
	
}
