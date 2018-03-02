package com.nqmobile.livesdk.modules.daily;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.nqmobile.livesdk.commons.db.DataProvider;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.app.table.AppCacheTable;
import com.nqmobile.livesdk.modules.app.table.AppLocalTable;
import com.nqmobile.livesdk.modules.daily.network.DailyListProtocol.GetDailyListSuccessEvent;
import com.nqmobile.livesdk.modules.daily.network.DailyServiceFactory;
import com.nqmobile.livesdk.modules.daily.table.DailyCacheTable;
import com.nqmobile.livesdk.modules.daily.table.WebCacheTable;
import com.nqmobile.livesdk.modules.feedback.FeedbackModule;
import com.nqmobile.livesdk.modules.locker.Locker;
import com.nqmobile.livesdk.modules.locker.LockerConverter;
import com.nqmobile.livesdk.modules.locker.LockerManager;
import com.nqmobile.livesdk.modules.locker.tables.LockerCacheTable;
import com.nqmobile.livesdk.modules.theme.Theme;
import com.nqmobile.livesdk.modules.theme.ThemeManager;
import com.nqmobile.livesdk.modules.theme.table.ThemeCacheTable;
import com.nqmobile.livesdk.modules.theme.table.ThemeLocalTable;
import com.nqmobile.livesdk.modules.wallpaper.Wallpaper;
import com.nqmobile.livesdk.modules.wallpaper.WallpaperManager;
import com.nqmobile.livesdk.modules.wallpaper.table.WallpaperCacheTable;
import com.nqmobile.livesdk.modules.wallpaper.table.WallpaperLocalTable;
import com.nqmobile.livesdk.utils.CollectionUtils;
import com.nqmobile.livesdk.utils.CursorUtils;

/**
 * 每日推荐业务类
 *
 * @author changxiaofei
 * @time 2013-12-5 下午6:43:37
 */
public class DailyManager extends AbsManager{
	private static final ILogger NqLog = LoggerFactory.getLogger(DailyModule.MODULE_NAME);
	
    private static final String TAG = "DailyManager";
    private Context mContext;
    private static DailyManager mInstance;

	/** store 模块编号，应用、每日推荐列表模块 */
	public static final int STORE_MODULE_TYPE_DAILY = 2;

	public static final int DAILY_CACHE_TYPE_APP = 0;
	public static final int DAILY_CACHE_TYPE_WALLPAPER = 1;
	public static final int DAILY_CACHE_TYPE_THEME = 2;
	public static final int DAILY_CACHE_TYPE_WEB = 3;
	public static final int DAILY_CACHE_TYPE_LOCKER = 4;
	
    public static final long CACHE_MAX_TIME = 24L * 60 * 60 * 1000;//24小时
    private static final String DAILY_APP_QUERY_BY_RES_ID = AppLocalTable.APP_SOURCE_TYPE
            + "=" + STORE_MODULE_TYPE_DAILY + " AND "
            + AppLocalTable.APP_ID + "=?";
    private static final String DAILY_WALLPAPER_QUERY_BY_RES_ID = WallpaperLocalTable.WALLPAPER_SOURCE_TYPE
            + "=" + STORE_MODULE_TYPE_DAILY + " AND "
            + WallpaperLocalTable.WALLPAPER_ID + "=?";
    private static final String DAILY_THEME_QUERY_BY_RES_ID = ThemeLocalTable.THEME_SOURCE_TYPE
            + "=" + STORE_MODULE_TYPE_DAILY + " AND "
            + ThemeLocalTable.THEME_ID + "=?";
    private static final String DAILY_WEB_QUERY_BY_RES_ID = WebCacheTable.WEB_ID + "=?";
    private static final String DAILY_LOCKER_QUERY_BY_RES_ID = LockerCacheTable.LOCKER_SOURCE_TYPE
            + "=" + STORE_MODULE_TYPE_DAILY + " AND "
            + LockerCacheTable.LOCKER_ID + "=?";

    public DailyManager(Context context) {
        super();
        this.mContext = context.getApplicationContext();
    }

    public synchronized static DailyManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DailyManager(context);
        }
        return mInstance;
    }

    /**
     * 联网获取每日推荐列表
     *
     * @param listener
     */
    public void getDailyList(final int size,final DailyListListener listener) {
        // 查询是否有可用的缓存数据。
        if (haveAvailableDailyListCashe(size)) {
            NqLog.d("DailyManager.getDailyList() 有可用的缓存数据");
            List<Daily> dailys = getDailyListFromCache(size);
            if (dailys != null && dailys.size() == size) {
                listener.getDailyListSucc(dailys);
                return;
            } else {
                NqLog.e("Daily cache is in bad state!");
                clearDailyCache();
            }
        } else {
            NqLog.d("DailyManager.getDailyList() 无可用的缓存数据");
        }
        // 没有缓存，或缓存过期，联网查询数据。
        DailyServiceFactory.getService().getDailyList(mContext, size, listener);
        
        /*Utility.getInstance(mContext).getDailyList(new DailyListListener() {
            @Override
            public void onErr() {
                listener.onErr();
            }

            @Override
            public void getDailyListSucc(List<Daily> dailys) {
                //测试
                NqLog.d("DailyManager.getDailyList() getDailyListSucc()");
                if (dailys == null) {
                    listener.onErr();
                } else {
                    List<Daily> result = new ArrayList<Daily>();
                    for(int i=0;i < dailys.size();i++){
                        result.add(dailys.get(i));
                    }
                    PreferenceDataHelper.getInstance(mContext).setIntValue(PreferenceDataHelper.KEY_DAILY_DISPLAY_SEQ, size);
                    listener.getDailyListSucc(result);
                }

            }
        });*/
    }

    /**
     * 检测是否有可用的每日推荐列表缓存数据
     *
     * @return
     */
    public boolean haveAvailableDailyListCashe(int size) {
        long cacheTime = DailyPreference.getInstance().getLongValue(DailyPreference.KEY_DAILY_LIST_CACHE_TIME);
        long currentTime = new Date().getTime();
        if (currentTime - cacheTime < CACHE_MAX_TIME) {//没有过期
            Cursor cursor = null;
            try {
                ContentResolver contentResolver = mContext.getContentResolver();
                cursor = contentResolver.query(DailyCacheTable.DAILY_CACHE_URI, null, null, null, null);
                if (cursor != null && cursor.getCount() >= size) {
                    return true;
                }
            } catch (Exception e) {
                NqLog.e(e);
            } finally {
    			CursorUtils.closeCursor(cursor);
            }
        }
        return false;
    }

    private String getSeqString(int seq,int size,int dailycount){
        StringBuilder sb = new StringBuilder();
        int[] seqs = new int[size];
        for(int i=0;i<size;i++){
            if(seq >= dailycount){
                seqs[i] = seqs[i] % dailycount;
                seq = seqs[i];
            }else{
                seqs[i] = seq;
            }

            seq++;
            sb.append(seqs[i]);
            if(i != size -1){
                sb.append(",");
            }
        }

        return sb.toString();
    }

    /**
     * 返回缓存的每日推荐列表。
     *
     * @return
     */
    public List<Daily> getDailyListFromCache(int size) {
        List<Daily> listDailys = null;
        DailyPreference pref = DailyPreference.getInstance();
        int dailyCount = pref.getIntValue(DailyPreference.KEY_DAILY_COUNT);
        if (dailyCount <= 0)
            return listDailys;

        int dailyDisplaySeq = pref.getIntValue(DailyPreference.KEY_DAILY_DISPLAY_SEQ);

        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(DailyCacheTable.DAILY_CACHE_URI, null,
            		DailyCacheTable.DAILY_CACHE_SEQ + " IN (" + getSeqString(dailyDisplaySeq,size,dailyCount) + ")",
                    null, DailyCacheTable._ID + " asc");
            if (cursor == null || cursor.getCount() != size)
                return null;

            listDailys = new ArrayList<Daily>();
            while (cursor.moveToNext()) {
                int type = cursor.getInt(cursor.getColumnIndex(DailyCacheTable.DAILY_CACHE_TYPE));
                String dailyId = cursor.getString(cursor.getColumnIndex(DailyCacheTable.DAILY_CACHE_ID));
                String resourceId = cursor.getString(cursor.getColumnIndex(DailyCacheTable.DAILY_CACHE_RESOURCEID));
                int seq = cursor.getInt(cursor.getColumnIndex(DailyCacheTable.DAILY_CACHE_SEQ));
                Daily daily = getDailyResource(dailyId, type, seq, resourceId);
                if (daily != null) {
                    listDailys.add(daily);
                }
            }
            int nextDisplaySeq = (dailyDisplaySeq + size) % dailyCount;
            pref.setIntValue(DailyPreference.KEY_DAILY_DISPLAY_SEQ, nextDisplaySeq);
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
			CursorUtils.closeCursor(cursor);
        }
        return listDailys;
    }

    /**
     * 返回每日推荐具体资源
     *
     * @param dailys
     * @param type       资源类型，应用、壁纸、主题、web/webview广告推荐、锁屏
     * @param dailyId    每日推荐的id
     * @param resourceId 资源的id，应用、壁纸、主题、web/webview广告推荐、锁屏
     */
    private Daily getDailyResource(String dailyId, int type, int seq, String resourceId) {
        Cursor cursor = null;
        Daily daily = null;
        daily = new Daily();
        daily.setIntType(type);
        daily.setStrId(dailyId);
        daily.setSeq(seq);
        ContentResolver contentResolver = mContext.getContentResolver();
        try {
            if (type == DAILY_CACHE_TYPE_APP) {
                cursor = contentResolver.query(AppCacheTable.APP_CACHE_URI,
                        null, DAILY_APP_QUERY_BY_RES_ID, new String[]{resourceId},
                        null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    App app = AppManager.getInstance(mContext).cursorToApp(
                            cursor);
                    daily.setApp(app);
                } else {
                    daily = null;
                }
            } else if (type == DAILY_CACHE_TYPE_THEME) {
                cursor = contentResolver.query(ThemeCacheTable.THEME_CACHE_URI,
                        null, DAILY_THEME_QUERY_BY_RES_ID, new String[]{resourceId},
                        null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    Theme theme = ThemeManager.getInstance(mContext)
                            .cursorToTheme(cursor);
                    daily.setTheme(theme);
                } else {
                    daily = null;
                }
            } else if (type == DAILY_CACHE_TYPE_WALLPAPER) {
                cursor = contentResolver.query(
                        WallpaperCacheTable.WALLPAPER_CACHE_URI, null,
                        DAILY_WALLPAPER_QUERY_BY_RES_ID, new String[]{resourceId},
                        null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    Wallpaper wallpaper = WallpaperManager.getInstance(mContext)
                            .cursorToWallpaper(cursor);
                    daily.setWallpaper(wallpaper);
                } else {
                    daily = null;
                }
            } else if (type == DAILY_CACHE_TYPE_WEB) {
                cursor = contentResolver.query(
                		WebCacheTable.WEB_CACHE_URI, null,
                        DAILY_WEB_QUERY_BY_RES_ID, new String[]{resourceId},
                        null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    Web web = WebManager.getInstance(mContext)
                            .cursorToWeb(cursor);
                    daily.setWeb(web);
                } else {
                    daily = null;
                }
            } else if (type == DAILY_CACHE_TYPE_LOCKER) {
                cursor = contentResolver.query(
                        LockerCacheTable.LOCKER_CACHE_URI, null,
                        DAILY_LOCKER_QUERY_BY_RES_ID, new String[]{resourceId},
                        null);
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    Locker locker = LockerManager.getInstance(mContext)
                            .cursorToLocker(cursor);
                    daily.setLocker(locker);
                } else {
                    daily = null;
                }
            } else {
                NqLog.e("Unknown daily resource type");
            }
        } finally {
			CursorUtils.closeCursor(cursor);
        }

        return daily;
    }

    public boolean clearDailyCache() {
        try {
        	DailyPreference pref = DailyPreference.getInstance();
            pref.setLongValue(DailyPreference.KEY_DAILY_LIST_CACHE_TIME, 0);
            pref.setIntValue(DailyPreference.KEY_DAILY_DISPLAY_SEQ, 0);
            pref.setIntValue(DailyPreference.KEY_DAILY_COUNT, 0);

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            Builder b = null;
            // 清除缓存
            b = ContentProviderOperation
                    .newDelete(DailyCacheTable.DAILY_CACHE_URI);
            ops.add(b.build());
            b = ContentProviderOperation.newDelete(AppCacheTable.APP_CACHE_URI)
                    .withSelection(
                    		AppCacheTable.APP_SOURCE_TYPE + "=" + STORE_MODULE_TYPE_DAILY,
                            null);
            ops.add(b.build());
            b = ContentProviderOperation
                    .newDelete(ThemeCacheTable.THEME_CACHE_URI).withSelection(
                    		ThemeCacheTable.THEME_SOURCE_TYPE + "=" + STORE_MODULE_TYPE_DAILY,
                            null);
            ops.add(b.build());
            b = ContentProviderOperation.newDelete(
                    WallpaperCacheTable.WALLPAPER_CACHE_URI).withSelection(
                    		WallpaperCacheTable.WALLPAPER_SOURCE_TYPE + "="
                            + STORE_MODULE_TYPE_DAILY, null);
            ops.add(b.build());
            b = ContentProviderOperation.newDelete(
            		WebCacheTable.WEB_CACHE_URI).withSelection(
                    null, null);
            ops.add(b.build());
            b = ContentProviderOperation.newDelete(
                    LockerCacheTable.LOCKER_CACHE_URI).withSelection(
                            LockerCacheTable.LOCKER_SOURCE_TYPE + "="
                            + STORE_MODULE_TYPE_DAILY, null);
            ops.add(b.build());
            mContext.getContentResolver().applyBatch(
            		DataProvider.DATA_AUTHORITY, ops);

            return true;
        } catch (Exception e) {
            NqLog.e(e);
        }
        return false;
    }


    /**
     * 缓存Daily数据
     *
     * @param dailys
     */
    public boolean saveDailyCache(List<Daily> dailys) {
        try {
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            Builder b = null;
            //清除缓存
            b = ContentProviderOperation.newDelete(DailyCacheTable.DAILY_CACHE_URI);
            ops.add(b.build());
            b = ContentProviderOperation.newDelete(AppCacheTable.APP_CACHE_URI).withSelection(AppLocalTable.APP_SOURCE_TYPE + "=" + STORE_MODULE_TYPE_DAILY, null);
            ops.add(b.build());
            b = ContentProviderOperation.newDelete(ThemeCacheTable.THEME_CACHE_URI).withSelection(ThemeCacheTable.THEME_SOURCE_TYPE + "=" + STORE_MODULE_TYPE_DAILY, null);
            ops.add(b.build());
            b = ContentProviderOperation.newDelete(WallpaperCacheTable.WALLPAPER_CACHE_URI).withSelection(WallpaperCacheTable.WALLPAPER_SOURCE_TYPE + "=" + STORE_MODULE_TYPE_DAILY, null);
            ops.add(b.build());
            b = ContentProviderOperation.newDelete(WebCacheTable.WEB_CACHE_URI).withSelection(null, null);
            ops.add(b.build());
            b = ContentProviderOperation.newDelete(LockerCacheTable.LOCKER_CACHE_URI).withSelection(LockerCacheTable.LOCKER_SOURCE_TYPE + "=" + DailyConstants.STORE_MODULE_TYPE_DAILY, null);
            ops.add(b.build());
            int seq = 0;
            if (dailys != null && dailys.size() > 0) {
                long localTime = new Date().getTime();
                for (int i = 0; i < dailys.size(); i++) {
                    Daily daily = dailys.get(i);
                    if (daily != null) {
                        if (daily.getIntType() == DAILY_CACHE_TYPE_APP) {
                            App app = daily.getApp();
                            app.setLongLocalTime(localTime);
                            app.setIntSourceType(STORE_MODULE_TYPE_DAILY);
                            ContentValues values = AppManager.getInstance(mContext).appToContentValues(0, app);
                            b = ContentProviderOperation.newInsert(
                                    AppCacheTable.APP_CACHE_URI).withValues(
                                    values);
                            ops.add(b.build());

                            ops.add(newDailyCacheInsert(daily.getStrId(), app.getStrId(), daily.getIntType(), app.getStrIconUrl(), seq++));
                        } else if (daily.getIntType() == DAILY_CACHE_TYPE_THEME) {
                            Theme theme = daily.getTheme();
                            theme.setLongLocalTime(localTime);
                            theme.setIntSourceType(STORE_MODULE_TYPE_DAILY);
                            ContentValues values = ThemeManager.getInstance(mContext).themeToContentValues(0, theme);
                            b = ContentProviderOperation.newInsert(
                                    ThemeCacheTable.THEME_CACHE_URI).withValues(
                                    values);
                            ops.add(b.build());

                            ops.add(newDailyCacheInsert(daily.getStrId(), theme.getStrId(), daily.getIntType(), theme.getStrIconUrl(), seq++));
                        } else if (daily.getIntType() == DAILY_CACHE_TYPE_WALLPAPER) {
                            Wallpaper wallpaper = daily.getWallpaper();
                            wallpaper.setLongLocalTime(localTime);
                            wallpaper
                                    .setIntSourceType(STORE_MODULE_TYPE_DAILY);
                            ContentValues values = WallpaperManager.getInstance(mContext)
                                    .wallpaperToContentValues(0, wallpaper);
                            b = ContentProviderOperation.newInsert(
                                    WallpaperCacheTable.WALLPAPER_CACHE_URI)
                                    .withValues(values);
                            ops.add(b.build());

                            ops.add(newDailyCacheInsert(daily.getStrId(), wallpaper.getStrId(), daily.getIntType(), wallpaper.getStrIconUrl(), seq++));
                        } else if (daily.getIntType() == DAILY_CACHE_TYPE_WEB) {
                            Web web = daily.getWeb();
//                            web.setLongLocalTime(localTime);
//                            web.setIntSourceType(ThemeConstants.STORE_MODULE_TYPE_DAILY);
                            ContentValues values = WebManager.getInstance(mContext)
                                    .webToContentValues(web);
                            b = ContentProviderOperation.newInsert(
                                    WebCacheTable.WEB_CACHE_URI)
                                    .withValues(values);
                            ops.add(b.build());

                            ops.add(newDailyCacheInsert(daily.getStrId(), web.getStrId(), daily.getIntType(), web.getStrIconUrl(), seq++));
                        } else if (daily.getIntType() == DAILY_CACHE_TYPE_LOCKER) {
                            Locker locker = daily.getLocker();
                            locker.setLongLocalTime(localTime);
                            locker
                                    .setIntSourceType(STORE_MODULE_TYPE_DAILY);
                            ContentValues values = LockerConverter.toContentValues(0, locker);
                            b = ContentProviderOperation.newInsert(
                                    LockerCacheTable.LOCKER_CACHE_URI)
                                    .withValues(values);
                            ops.add(b.build());

                            ops.add(newDailyCacheInsert(daily.getStrId(), locker.getStrId(), daily.getIntType(), locker.getStrIconUrl(), seq++));
                        }
                    }
                }
            }
            mContext.getContentResolver().applyBatch(DataProvider.DATA_AUTHORITY, ops);
            //更新缓存时间
            DailyPreference.getInstance().setLongValue(DailyPreference.KEY_DAILY_LIST_CACHE_TIME, new Date().getTime());
            //resource is refresh,so set the index to 0
            DailyPreference.getInstance().setIntValue(DailyPreference.KEY_DAILY_DISPLAY_SEQ, 0);
            //save count of daily resource
            DailyPreference.getInstance().setIntValue(DailyPreference.KEY_DAILY_COUNT, seq);
            return true;
        } catch (Exception e) {
            NqLog.e(e);
        }
        return false;
    }

    private ContentProviderOperation newDailyCacheInsert(String dailyId, String resourceId, int type, String iconUrl, int seq) {
        ContentValues values = new ContentValues();
        values.put(DailyCacheTable.DAILY_CACHE_ID,
                dailyId);
        values.put(DailyCacheTable.DAILY_CACHE_RESOURCEID,
                resourceId);
        values.put(DailyCacheTable.DAILY_CACHE_TYPE,
                type);
        values.put(DailyCacheTable.DAILY_CACHE_ICON_URL,
                iconUrl);
        values.put(DailyCacheTable.DAILY_CACHE_SEQ,
                seq);
        Builder b = ContentProviderOperation.newInsert(
                DailyCacheTable.DAILY_CACHE_URI).withValues(
                values);
        return b.build();
    }

	@Override
	public void init() {
		EventBus.getDefault().register(this);
	}
	
	public void onEvent(GetDailyListSuccessEvent event){
		DailyListListener listener = (DailyListListener) event.getTag();
		if (event.isSuccess()) {
			NqLog.d("DailyManager onEvent getDailyListSucc");
			ArrayList<Daily> dailys = event.getDailys();
			if (CollectionUtils.isNotEmpty(dailys)) {
                listener.onErr();
            } else {
                List<Daily> result = new ArrayList<Daily>();
                for(int i=0;i < dailys.size();i++){
                    result.add(dailys.get(i));
                }
                DailyPreference.getInstance().setIntValue(DailyPreference.KEY_DAILY_DISPLAY_SEQ, event.getSize());
                listener.getDailyListSucc(result);
            }
		}else
			listener.onErr();
	}
}
