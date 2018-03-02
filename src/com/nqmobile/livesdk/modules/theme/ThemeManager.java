package com.nqmobile.livesdk.modules.theme;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;

import com.nqmobile.livesdk.LauncherSDK;
import com.nqmobile.livesdk.OnUpdateListener;
import com.nqmobile.livesdk.commons.db.DataProvider;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.image.ImageListener;
import com.nqmobile.livesdk.commons.image.ImageLoader;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.mydownloadmanager.IDownloadObserver;
import com.nqmobile.livesdk.commons.mydownloadmanager.MyDownloadManager;
import com.nqmobile.livesdk.commons.mydownloadmanager.table.DownloadTable;
import com.nqmobile.livesdk.commons.receiver.DownloadCompleteEvent;
import com.nqmobile.livesdk.commons.receiver.LiveReceiver;
import com.nqmobile.livesdk.modules.app.AppActionConstants;
import com.nqmobile.livesdk.modules.app.AppConstant;
import com.nqmobile.livesdk.modules.points.PointsPreference;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.modules.theme.network.ThemeDetailProtocal.GetThemeDetailSuccessEvent;
import com.nqmobile.livesdk.modules.theme.network.ThemeListProtocol.GetThemeListSuccessEvent;
import com.nqmobile.livesdk.modules.theme.network.ThemeServiceFactory;
import com.nqmobile.livesdk.modules.theme.table.ThemeCacheTable;
import com.nqmobile.livesdk.modules.theme.table.ThemeLocalTable;
import com.nqmobile.livesdk.modules.wallpaper.WallpaperManager;
import com.nqmobile.livesdk.utils.CursorUtils;
import com.nqmobile.livesdk.utils.FileUtil;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.NotificationUtil;
import com.nqmobile.livesdk.utils.StringUtil;
import com.nqmobile.livesdk.utils.ToastUtils;

/**
 * 主题业务类
 *
 * @author changxiaofei
 * @time 2013-12-5 下午3:19:07
 */
public class ThemeManager extends AbsManager{
	private static final ILogger NqLog = LoggerFactory.getLogger(ThemeModule.MODULE_NAME);
	
    public static final int COLUMN_PICKS = 0;
    public static final int COLUMN_TOP_GAMES = 1;
    public static final int COLUMN_TOP_APPS = 2;
    public static final long CACHE_MAX_TIME = 24L * 60 * 60 * 1000;//24小时
    private Context mContext;
    private static ThemeManager mInstance;

    public static final int STATUS_UNKNOWN = -1;//未知
    public static final int STATUS_NONE = 0;//未下载
    public static final int STATUS_DOWNLOADING = 1;//下载中
    public static final int STATUS_PAUSED = 2;//下载暂停中
    public static final int STATUS_DOWNLOADED = 3;//已下载
    public static final int STATUS_CURRENT_THEME = 4;//当前主题

    public static final int POINT_FLAG_NOT = 0;
    public static final int POINT_FLAG_CONSUME = 1;
    public static final int POINT_FLAG_NOT_CONSUME = 2;

    public static final String DEFAULT_THEME = "default";

    public ThemeManager(Context context) {
        super();
        this.mContext = context.getApplicationContext();

        EventBus.getDefault().register(this);
    }

    public synchronized static ThemeManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ThemeManager(context);
        }
        return mInstance;
    }

    /**
     * 将cursor对象转换Theme
     *
     * @param cursor
     * @return
     */
    public Theme cursorToTheme(Cursor cursor) {
        Theme theme = new Theme();
        theme.setStrId(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(ThemeLocalTable.THEME_ID))));
        theme.setIntSourceType(cursor.getColumnIndex(ThemeLocalTable.THEME_SOURCE_TYPE));
        theme.setStrName(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(ThemeLocalTable.THEME_NAME))));
        theme.setStrAuthor(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(ThemeLocalTable.THEME_AUTHOR))));
        theme.setStrVersion(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(ThemeLocalTable.THEME_VERSION))));
        theme.setStrSource(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(ThemeLocalTable.THEME_SOURCE))));
        theme.setLongSize(cursor.getLong(cursor.getColumnIndex(ThemeLocalTable.THEME_SIZE)));
        theme.setLongDownloadCount(cursor.getLong(cursor.getColumnIndex(ThemeLocalTable.THEME_DOWNLOAD_COUNT)));
        theme.setStrIconUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(ThemeLocalTable.THEME_ICON_URL))));
        theme.setStrIconPath(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(ThemeLocalTable.THEME_ICON_PATH))));
        theme.setDailyIcon(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(ThemeLocalTable.THEME_DAILYICON))));
        String previewUrl = cursor.getString(cursor.getColumnIndex(ThemeLocalTable.THEME_PREVIEW_URL));
        if (!TextUtils.isEmpty(previewUrl)) {
            String[] s = previewUrl.split(";");
            List<String> list = new ArrayList<String>();
            for (int i = 0; i < s.length; i++) {
                list.add(s[i]);
            }
            theme.setArrPreviewUrl(list);
        }
        String previewPath = cursor.getString(cursor.getColumnIndex(ThemeLocalTable.THEME_PREVIEW_PATH));
        if (!TextUtils.isEmpty(previewPath)) {
            String[] s = previewPath.split(";");
            List<String> listPath = new ArrayList<String>();
            for (int i = 0; i < s.length; i++) {
                listPath.add(s[i]);
            }
            theme.setArrPreviewPath(listPath);
        }
        theme.setStrThemeUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(ThemeLocalTable.THEME_URL))));
        theme.setStrThemePath(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(ThemeLocalTable.THEME_PATH))));
        theme.setLongUpdateTime(cursor.getLong(cursor.getColumnIndex(ThemeLocalTable.THEME_UPDATETIME)));
        theme.setLongLocalTime(cursor.getLong(cursor.getColumnIndex(ThemeLocalTable.THEME_LOCALTIME)));
        theme.setPointsflag(cursor.getInt(cursor.getColumnIndex(ThemeLocalTable.THEME_POINTSFLAG)));
        theme.setConsumePoints(cursor.getInt(cursor.getColumnIndex(ThemeLocalTable.THEME_COSUMEPOINTS)));
        return theme;
    }

    /**
     * 将theme对象转换层数据库操作的ContentValues对象
     *
     * @param column 0：新品；1：热门；-1：未知（下载主题，保存数据到Local表时，栏目字段为-1）
     * @param theme
     * @return
     */
    public ContentValues themeToContentValues(int column, Theme theme) {
        ContentValues values = null;
        if (theme != null) {
            values = new ContentValues();
            values.put(ThemeLocalTable.THEME_ID, theme.getStrId());
            values.put(ThemeLocalTable.THEME_SOURCE_TYPE, theme.getIntSourceType());
            values.put(ThemeLocalTable.THEME_COLUMN, column);
            values.put(ThemeLocalTable.THEME_NAME, theme.getStrName());
            values.put(ThemeLocalTable.THEME_AUTHOR, theme.getStrAuthor());
            values.put(ThemeLocalTable.THEME_VERSION, theme.getStrVersion());
            values.put(ThemeLocalTable.THEME_SOURCE, theme.getStrSource());
            values.put(ThemeLocalTable.THEME_SIZE, theme.getLongSize());
            values.put(ThemeLocalTable.THEME_DOWNLOAD_COUNT, theme.getLongDownloadCount());
            values.put(ThemeLocalTable.THEME_ICON_URL, theme.getStrIconUrl());
            values.put(ThemeLocalTable.THEME_DAILYICON,theme.getDailyIcon());
            //预览图网址
            StringBuilder previewUrl = new StringBuilder();
            List<String> previewUrls = theme.getArrPreviewUrl();
            if (previewUrls != null && previewUrls.size() > 0) {
                for (int j = 0; j < previewUrls.size(); j++) {
                    previewUrl.append(previewUrls.get(j)).append(";");
                }
            }
            if (previewUrl.length() > 1) {
                values.put(ThemeLocalTable.THEME_PREVIEW_URL, previewUrl.substring(0, previewUrl.length() - 1));
            } else {
                values.put(ThemeLocalTable.THEME_PREVIEW_URL, "");
            }
            values.put(ThemeLocalTable.THEME_URL, theme.getStrThemeUrl());
            values.put(ThemeLocalTable.THEME_ICON_PATH, theme.getStrIconPath());
            //预览图本地路径
            StringBuilder previewPath = new StringBuilder();
            List<String> previewPaths = theme.getArrPreviewPath();
            if (previewPaths != null && previewPaths.size() > 0) {
                for (int j = 0; j < previewPaths.size(); j++) {
                    previewPath.append(previewPaths.get(j)).append(";");
                }
            }
            if (previewPath.length() > 1) {
                values.put(ThemeLocalTable.THEME_PREVIEW_PATH, previewPath.substring(0, previewPath.length() - 1));
            } else {
                values.put(ThemeLocalTable.THEME_PREVIEW_PATH, "");
            }
            values.put(ThemeLocalTable.THEME_PATH, theme.getStrThemePath());
            values.put(ThemeLocalTable.THEME_UPDATETIME, theme.getLongUpdateTime());
            values.put(ThemeLocalTable.THEME_LOCALTIME, theme.getLongLocalTime());
            values.put(ThemeLocalTable.THEME_COSUMEPOINTS,theme.getConsumePoints());
            values.put(ThemeLocalTable.THEME_POINTSFLAG,theme.getPointsflag());
        }
        return values;
    }

    /**
     * 获取主题列表
     *
     * @param column
     * @param offset
     * @param ThemeListListener
     */
    public void getThemeList(final int column, final int offset, final ThemeListStoreListener listener) {
    	if(listener == null)
    		return;
    	
    	// 没有网络
    	if (!NetworkUtils.isConnected(mContext)) {
    		listener.onNoNetwork();
    		return;
    	}
    	
    	// 没有缓存，或缓存过期，联网查询数据。
    	ThemeServiceFactory.getService().getThemeList(mContext, column, offset, listener);
    }

    /**
     * 返回本地的主题列表。
     *
     * @return
     */
    public List<Theme> getThemeListFromLocal() {
        List<Theme> listThemes = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(ThemeLocalTable.LOCAL_THEME_URI, null, null, null, ThemeLocalTable._ID + " asc");
            if (cursor != null && cursor.getCount() > 0) {
                listThemes = new ArrayList<Theme>();
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    Theme theme = cursorToTheme(cursor);
                    ThemeStatus status = getStatus(theme);
                    if(status.statusCode == STATUS_DOWNLOADED || status.statusCode == STATUS_CURRENT_THEME){
                        listThemes.add(cursorToTheme(cursor));
                    }
                }
            }
        } catch (Exception e) {
            NqLog.e("getThemeListFromLocal " + e.toString());
        } finally {
			CursorUtils.closeCursor(cursor);
        }
        return listThemes;
    }

    /**
     * 返回缓存的主题列表。
     *
     * @param column 0：新品；1：热门。
     * @return
     */
    public List<Theme[]> getArrThemeListFromCache(int column) {
        List<Theme[]> listThemes = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(ThemeCacheTable.THEME_CACHE_URI, null,
            		ThemeCacheTable.THEME_SOURCE_TYPE + "="
            	            + AppConstant.STORE_MODULE_TYPE_MAIN + " AND "
            	            + ThemeCacheTable.THEME_COLUMN + "=?", new String[]{String.valueOf(column)}, ThemeCacheTable._ID + " asc");
            if (cursor != null && cursor.getCount() > 0) {
                listThemes = new ArrayList<Theme[]>();
                int index = 0;
                Theme[] themes = null;
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                	if(index==0){
						themes = new Theme[3];
					}
                    themes[index] = cursorToTheme(cursor);
                    if(index==2){
						listThemes.add(themes);
						index = 0;
					}else{
						index++;
					}
                }
                if(cursor.getCount()%3!=0){
					listThemes.add(themes);
				}
            }
        } catch (Exception e) {
            NqLog.e("getArrThemeListFromCache " + e.toString());
        } finally {
			CursorUtils.closeCursor(cursor);
        }
        return listThemes;
    }

    /**
     * 根据id 获取Theme 详情。
     *
     * @param id
     * @param listener
     */
    public void getThemeDetail(String id, final ThemeDetailListener listener) {
    	ThemeServiceFactory.getService().getThemeDetail(mContext, id, listener);
    }

    /**
     * 检测是否有主题缓存数据
     *
     * @param column 0：新品；1：热门；
     * @return
     */
    public boolean hadAvailableThemeListCashe(int column) {
    	Cursor cursor = null;
        try {
            ContentResolver contentResolver = mContext.getContentResolver();
            cursor = contentResolver.query(ThemeCacheTable.THEME_CACHE_URI, 
            		new String[]{ThemeCacheTable.THEME_ID}, ThemeCacheTable.THEME_COLUMN + "=" + column, null, null);
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
     * 缓存主题数据
     *
     * @param column    0：新品；1：热门；
     * @param offset
     * @param arrThemes
     * @return
     */
    public boolean saveThemeCache(int column, int offset, List<Theme[]> arrThemes) {
        try {
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            Builder b = null;
            
            if(offset == 0){
            	// 清除缓存
                b = ContentProviderOperation.newDelete(ThemeCacheTable.THEME_CACHE_URI)
                        .withSelection(ThemeCacheTable.THEME_SOURCE_TYPE + "=" + AppConstant.STORE_MODULE_TYPE_MAIN + " AND " + ThemeCacheTable.THEME_COLUMN + "=" + column, null);
                ops.add(b.build());
            }
            ContentResolver contentResolver = mContext.getContentResolver();

            if (arrThemes != null && arrThemes.size() > 0) {
                Theme[] themes = null;
                long localTime = new Date().getTime();
                for (int i = 0; i < arrThemes.size(); i++) {
                    themes = arrThemes.get(i);
                    for (int j = 0; j < themes.length; j++) {
                    	if (themes[j] != null) {
                            themes[j].setLongLocalTime(localTime);
                            ContentValues values = themeToContentValues(column, themes[j]);
//                            contentResolver.delete(ThemeCacheTable.THEME_CACHE_URI, DataProvider.THEME_ID + "='" + themes[j].getStrId() + "'", null);
                            b = ContentProviderOperation.newInsert(ThemeCacheTable.THEME_CACHE_URI).withValues(values);
                            ops.add(b.build());
                    	}
                    }
                }
            }

            contentResolver.applyBatch(DataProvider.DATA_AUTHORITY, ops);

            if(column == 0 || column == 1){
                //更新缓存时间。
            	ThemePreference.getInstance().setLongValue(ThemePreference.KEY_THEME_LIST_CACHE_TIME[column], new Date().getTime());
            }
            return true;
        } catch (Exception e) {
            NqLog.e("saveThemeCache error " + e.toString());
            e.printStackTrace();
        }

        return false;
    }

    private boolean saveImageToFile(final String srcUrl, final String destFilePath) {
        boolean result = false;
        if (TextUtils.isEmpty(srcUrl)) {
            return result;
        }

        ImageLoader.getInstance(mContext).getImage(srcUrl, new ImageListener() {
            @Override
            public void onErr() {
            }

            @Override
            public void getImageSucc(String url, BitmapDrawable drawable) {
                if (drawable != null) {
                    FileUtil.writeBmpToFile(drawable.getBitmap(), new File(destFilePath));
                }
            }
        });

        return true;
    }

    /**
     * 保存icon、预览图到本地文件夹
     *
     * @param theme
     * @return
     */
    private boolean saveImages(Theme theme) {
        boolean result = false;
        if (theme != null) {
            result = saveImageToFile(theme.getStrIconUrl(), theme.getStrIconPath());
            List<String> previewUrls = theme.getArrPreviewUrl();
            List<String> previewPaths = theme.getArrPreviewPath();
            if (previewUrls != null && previewPaths != null && previewUrls.size() == previewPaths.size()) {
                for (int i = 0; i < previewUrls.size(); i++) {
                    result = saveImageToFile(previewUrls.get(i), previewPaths.get(i));
                }
            }
        }
        return result;
    }

    /**
     * 主题数据存入本地库
     *
     * @param theme
     * @return
     */
    private void saveTheme(final Theme theme) {
        (new Thread() {
            @Override
            public void run() {
                try {
                    ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                    Builder deleteOp = ContentProviderOperation.newDelete(
                            ThemeLocalTable.LOCAL_THEME_URI).withSelection(
                            ThemeLocalTable.THEME_ID + " = ?",
                            new String[]{theme.getStrId()});
                    ops.add(deleteOp.build());

                    theme.setLongLocalTime(System.currentTimeMillis());
                    ContentValues values = themeToContentValues(-1, theme);
                    Builder insertOp = ContentProviderOperation.newInsert(
                            ThemeLocalTable.LOCAL_THEME_URI).withValues(values);
                    ops.add(insertOp.build());

                    mContext.getContentResolver().applyBatch(
                    		DataProvider.DATA_AUTHORITY, ops);

                    saveImages(theme);
                } catch (Exception e) {
                    NqLog.e(e);
                }
            }
        }).start();
    }

    /**
     * 根据id 从缓存获取Theme详情。
     *
     * @param themeId
     * @return Theme对象或null。null表示没有找到。
     */
    public Theme getThemeDetailFromCache(String themeId) {
        Theme theme = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(ThemeCacheTable.THEME_CACHE_URI, null,
            		ThemeCacheTable.THEME_ID + "=?", new String[]{themeId}, ThemeCacheTable._ID + " desc");
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToNext();
                theme = cursorToTheme(cursor);
            }
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
			CursorUtils.closeCursor(cursor);
        }
        return theme;
    }

    public static int getCount(List<Theme[]> themes) {
        if (null == themes)
            return 0;
        int count = 0;
        for (int i = 0; i < themes.size(); i++) {
        	for(int j=0; j<themes.get(i).length; j++){
        		if (null != themes.get(i)[j] &&
                        !TextUtils.isEmpty(themes.get(i)[j].getStrId()))
                    count++;
        	}
            
        }
        NqLog.d("theme count=" + count);
        return count;
    }

    private boolean isDefaultTheme(Theme theme) {
        return theme != null && DEFAULT_THEME.equals(theme.getStrThemePath());
    }

    private boolean isCurrentTheme(Theme theme) {
        String currentThemeId = getCurrentThemeID();
        if ("".equals(currentThemeId)) {
            if (isDefaultTheme(theme)) {// 默认主题
                return true;
            } else {
                return false;
            }
        }

        return currentThemeId.equals(theme.getStrId());
    }

    public static class ThemeStatus {
        public int statusCode;
        public long downloadedBytes;
        public long totalBytes;
    }

    /**
     * 返回Theme的下载、安装状态
     *
     * @param app
     * @return 总是返回非null
     * @author chenyanmin
     * @time 2014-1-23 上午11:06:17
     */
    public ThemeStatus getStatus(Theme theme) {
        ThemeStatus result = new ThemeStatus();
        result.statusCode = STATUS_UNKNOWN;

        if (theme == null) {
            return result;
        }

        if (isCurrentTheme(theme)) {
            result.statusCode = STATUS_CURRENT_THEME;
        } else {
            MyDownloadManager downloader = MyDownloadManager.getInstance(mContext);
            Long downloadId = downloader.getDownloadId(theme.getStrThemeUrl());
            int[] bytesAndStatus = downloader.getBytesAndStatus(downloadId);
            if (bytesAndStatus[0] == 1) {
                result.statusCode = convertStatus(bytesAndStatus[1]);
                if(result.statusCode == STATUS_DOWNLOADED) {
                	String filePath = theme.getStrThemePath();
                	if (!TextUtils.isEmpty(filePath)){
                        File file = new File(filePath);
                        if (!file.exists()){
                        	result.statusCode = STATUS_NONE;
                        }
                    }
                }
                result.downloadedBytes = bytesAndStatus[2];
                result.totalBytes = bytesAndStatus[3];
            } else {
                result.statusCode = STATUS_NONE;// 未下载
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
     * 应用主题
     *
     * @param theme
     */
    public boolean applyTheme(Theme theme) {
		boolean result = false;

		if (theme == null || TextUtils.isEmpty(theme.getStrThemePath())) {
			return result;
		}

        Intent i = new Intent();
        i.setAction(LiveReceiver.ACTION_APPLY_THEME);
        i.putExtra("theme", theme);
        mContext.sendBroadcast(i);
        result = true;
        WallpaperManager.getInstance(mContext).setCurrentWallpaperID("");
		return result;
	}
    
    /**
     * 下载Theme
     *
     * @param theme Theme对象
     * @author chenyanmin
     * @time 2014-1-23 下午2:39:51
     */
    public Long downloadTheme(Theme theme) {
        Long result = null;

        if (theme == null || TextUtils.isEmpty(theme.getStrThemeUrl())
                || DEFAULT_THEME.equals(theme.getStrThemePath())) {
            return result;
        }

        if (!NetworkUtils.isConnected(mContext)) {
        	ToastUtils.toast(mContext, "nq_nonetwork");
            return result;
        }

        try {
            Long downloadId = MyDownloadManager.getInstance(mContext).downloadTheme(theme);
            if (downloadId != null) {
                saveTheme(theme);
                result = downloadId;
            }
        } catch (Exception e) {
            NqLog.e(e);
        }

        return result;
    }

    /**
     * 注册下载进度监听
     *
     * @param theme
     * @param observer
     * @author chenyanmin
     * @time 2014-1-23 下午5:43:00
     */
    public void registerDownloadObserver(Theme theme, IDownloadObserver observer) {
        if (theme == null || observer == null) {
            return;
        }

        MyDownloadManager downloader = MyDownloadManager.getInstance(mContext);
        Long downloadId = downloader.getDownloadId(theme.getStrThemeUrl());
        downloader.registerDownloadObserver(downloadId, observer);
    }

    /**
     * 取消下载进度监听
     *
     * @param theme
     * @author chenyanmin
     * @time 2014-1-23 下午5:43:29
     */
    public void unregisterDownloadObserver(Theme theme) {
        if (theme == null) {
            return;
        }

        MyDownloadManager downloader = MyDownloadManager.getInstance(mContext);
        Long downloadId = downloader.getDownloadId(theme.getStrThemeUrl());
        downloader.unregisterDownloadObserver(downloadId);
    }

    /**
     * 获取当前主题ID
     *
     * @return
     * @author chenyanmin
     * @time 2014-1-24 下午5:56:31
     */
    public String getCurrentThemeID() {
        String id = ThemePreference.getInstance().getStringValue(ThemePreference.KEY_CURRENT_THEME);
        return id;
    }

    /**
     * 设置当前主题ID
     *
     * @return
     * @author chenyanmin
     * @time 2014-1-24 下午5:56:31
     */
    public void setCurrentThemeID(String themeId) {
    	ThemePreference.getInstance().setStringValue(ThemePreference.KEY_CURRENT_THEME, themeId);
    }

    private Theme getSavedTheme(String themeId) {
        Theme result = null;
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = mContext.getContentResolver();
            cursor = contentResolver.query(ThemeLocalTable.LOCAL_THEME_URI, null, ThemeLocalTable.THEME_ID + " = ?", new String[]{themeId}, ThemeLocalTable._ID + " DESC");
            if (cursor != null && cursor.moveToFirst()) {
                result = cursorToTheme(cursor);
            }
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
			CursorUtils.closeCursor(cursor);
        }

        return result;
    }

    public void OnDownloadComplete(String themeId) {
        if (TextUtils.isEmpty(themeId)) {
            return;
        }

        Theme theme = getSavedTheme(themeId);
        if (theme == null) {
            return;
        }

        // 通知launcher刷新本地主题列表
        Intent i = new Intent();
        i.setAction(LiveReceiver.ACTION_THEME_DOWNLOAD);
        i.putExtra("theme", theme);
        mContext.sendBroadcast(i);
    }

	public boolean isCacheExpired(int column){
        Long cacheTime = 0l;
        cacheTime = ThemePreference.getInstance().getLongValue(ThemePreference.KEY_THEME_LIST_CACHE_TIME[column]);
        Long currentTime = new Date().getTime();
    	return currentTime - cacheTime > CACHE_MAX_TIME;
    }

    public boolean isPointTheme(String resId){
        NqLog.i("isPointTheme resId"+resId);
        boolean result = false;
        Cursor c = null;
        try{
            c = mContext.getContentResolver().query(ThemeCacheTable.THEME_CACHE_URI,null,ThemeCacheTable.THEME_ID + " = ?",new String[]{resId},null);
            if(c != null && c.moveToNext()){
                int pointflag = c.getInt(c.getColumnIndex(ThemeCacheTable.THEME_POINTSFLAG));
                NqLog.i("isPointTheme pointflag="+pointflag);
                if(pointflag > 0){
                    result = true;
                }
            }
        }finally {
			CursorUtils.closeCursor(c);
        }
        return result;
    }

    public String getThemeName(String resId){
        String result = "";
        Cursor c = null;
        try{
            c = mContext.getContentResolver().query(ThemeCacheTable.THEME_CACHE_URI,null,ThemeCacheTable.THEME_ID + " = ?",new String[]{resId},null);
            if(c != null && c.moveToNext()){
                result = c.getString(c.getColumnIndex(ThemeCacheTable.THEME_NAME));
            }
        }finally {
			CursorUtils.closeCursor(c);
        }
        return result;
    }

	public void goThemeDetail(Theme theme) {
		if(theme == null)
			return;
		Intent intent = new Intent(mContext, ThemeDetailActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(ThemeDetailActivity.KEY_THEME, theme);
		mContext.startActivity(intent);
	}

    public boolean deleteTheme(String resId){
        NqLog.i("deleteTheme resId=" + resId);
        Theme t = getSavedTheme(resId);
        if (t == null || TextUtils.isEmpty(t.getStrThemePath())) {
            return false;
        }

        String id = t.getStrId();
        mContext.getContentResolver().delete(DownloadTable.TABLE_URI,DownloadTable.DOWNLOAD_RES_ID + " = ?",new String[]{id});
        mContext.getContentResolver().delete(ThemeLocalTable.LOCAL_THEME_URI,ThemeLocalTable.THEME_ID + " = ?",new String[]{id});
        FileUtil.delFile(t.getArrPreviewPath());
        FileUtil.delFile(t.getStrThemePath());
        FileUtil.delFile(t.getStrIconPath());

        List<OnUpdateListener> list = LauncherSDK.getInstance(mContext).getRegisteredOnUpdateListeners();
        for(OnUpdateListener l :list){
            l.onThemeDelete(t);
        }

        return true;
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
                        case MyDownloadManager.DOWNLOAD_TYPE_THEME:
                            ThemeManager.getInstance(context).OnDownloadComplete(resId);
                            boolean pointEnable = PointsPreference.getInstance().getBooleanValue(PointsPreference.KEY_POINT_CENTER_ENABLE);
                            if(ThemeManager.getInstance(context).isPointTheme(resId) && pointEnable){
                            	NotificationUtil.showPointThemeNofi(context,resId);
                            }else{
                            	NotificationUtil.showThemeNotifi(context,resId);
                            }

                            StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
                            		AppActionConstants.ACTION_LOG_1502, resId, StatManager.ACTION_DOWNLOAD,
                                    "1_1");
                            break;
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
	
	public void onEvent(GetThemeDetailSuccessEvent event) {
		ThemeDetailListener listener = (ThemeDetailListener) event.getTag();
		if (event.isSuccess()) {
			 listener.onGetDetailSucc(event.getTheme());
		 }else {
			listener.onErr();
		}
	}
	
	public void onEvent(GetThemeListSuccessEvent event) {
		ThemeListStoreListener listener = (ThemeListStoreListener) event.getTag();
		 if (event.isSuccess()) {
			 listener.onGetThemeListSucc(event.getColumn(), event.getOffset(), event.getThemes());
		 }else {
			listener.onErr();
		}
	}
	
	public void onEvent(DownloadCompleteEvent event) {
		processStoreDownload(mContext, event.mRefer);
	}
	
	
}