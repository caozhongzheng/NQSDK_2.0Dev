package com.nqmobile.livesdk.modules.wallpaper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
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
import com.nqmobile.livesdk.commons.image.ImageManager;
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
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.modules.wallpaper.network.WallpaperDetailProtocol.GetWallpaperDetailSuccessEvent;
import com.nqmobile.livesdk.modules.wallpaper.network.WallpaperListProtocol.GetWallpaperListSuccessEvent;
import com.nqmobile.livesdk.modules.wallpaper.network.WallpaperServiceFactory;
import com.nqmobile.livesdk.modules.wallpaper.table.WallpaperCacheTable;
import com.nqmobile.livesdk.modules.wallpaper.table.WallpaperLocalTable;
import com.nqmobile.livesdk.utils.CursorUtils;
import com.nqmobile.livesdk.utils.FileUtil;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.NotificationUtil;
import com.nqmobile.livesdk.utils.StringUtil;
import com.nqmobile.livesdk.utils.ToastUtils;

/**
 * 壁纸业务类
 *
 * @author changxiaofei
 * @time 2013-12-5 下午3:19:07
 */
public class WallpaperManager extends AbsManager{
	// ===========================================================
	// Constants
	// ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(WallpaperModule.MODULE_NAME);
	
	public static final int COLUMN_PICKS = 0;
    public static final int COLUMN_TOP_GAMES = 1;
    public static final int COLUMN_TOP_APPS = 2;
    public static final long CACHE_MAX_TIME = 24L * 60 * 60 * 1000;//24小时

    public static final int STATUS_UNKNOWN = -1;//未知
    public static final int STATUS_NONE = 0;//未下载
    public static final int STATUS_DOWNLOADING = 1;//下载中
    public static final int STATUS_PAUSED = 2;//下载暂停中
    public static final int STATUS_DOWNLOADED = 3;//已下载
    public static final int STATUS_CURRENT_WALLPAPER = 4;//当前壁纸

    public static final String DEFAULT_WALLPAPER = "default";
	// ===========================================================
	// Fields
	// ===========================================================
    private Context mContext;
    private static WallpaperManager mInstance;
    public boolean WP_APPLYING = false;
    
	// ===========================================================
	// Constructors
	// ===========================================================
    private WallpaperManager(Context context) {
        super();
        this.mContext = context.getApplicationContext();

        EventBus.getDefault().register(this);
    }

    public synchronized static WallpaperManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new WallpaperManager(context);
        }
        return mInstance;
    }
    
	// ===========================================================
	// Getter & Setter
	// ===========================================================
    public boolean isWP_APPLYING() {
		return WP_APPLYING;
	}

	public void setWP_APPLYING(boolean wP_APPLYING) {
		WP_APPLYING = wP_APPLYING;
	}
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
    @Override
    public void init() {
    	EventBus.getDefault().register(this);
    }

	// ===========================================================
	// Methods
	// ===========================================================
    
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

    /**
     * 将cursor对象转换wallpaper
     *
     * @param cursor
     * @return
     */
    public Wallpaper cursorToWallpaper(Cursor cursor) {
        Wallpaper wallpaper = new Wallpaper();
        wallpaper.setStrId(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(WallpaperLocalTable.WALLPAPER_ID))));
        wallpaper.setStrName(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(WallpaperLocalTable.WALLPAPER_NAME))));
        wallpaper.setIntSourceType(cursor.getColumnIndex(WallpaperLocalTable.WALLPAPER_SOURCE_TYPE));
        wallpaper.setIntType(cursor.getColumnIndex(WallpaperLocalTable.WALLPAPER_TYPE));
        wallpaper.setStrAuthor(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(WallpaperLocalTable.WALLPAPER_AUTHOR))));
        wallpaper.setStrVersion(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(WallpaperLocalTable.WALLPAPER_VERSION))));
        wallpaper.setStrSource(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(WallpaperLocalTable.WALLPAPER_SOURCE))));
        wallpaper.setLongSize(cursor.getLong(cursor.getColumnIndex(WallpaperLocalTable.WALLPAPER_SIZE)));
        wallpaper.setLongDownloadCount(cursor.getLong(cursor.getColumnIndex(WallpaperLocalTable.WALLPAPER_DOWNLOAD_COUNT)));
        wallpaper.setStrIconUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(WallpaperLocalTable.WALLPAPER_ICON_URL))));
        wallpaper.setStrIconPath(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(WallpaperLocalTable.WALLPAPER_ICON_PATH))));
        wallpaper.setStrWallpaperUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(WallpaperLocalTable.WALLPAPER_URL))));
        wallpaper.setStrWallpaperPath(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(WallpaperLocalTable.WALLPAPER_PATH))));
        wallpaper.setLongUpdateTime(cursor.getLong(cursor.getColumnIndex(WallpaperLocalTable.WALLPAPER_UPDATETIME)));
        wallpaper.setLongLocalTime(cursor.getLong(cursor.getColumnIndex(WallpaperLocalTable.WALLPAPER_LOCALTIME)));
        wallpaper.setPreviewPicture(cursor.getString(cursor.getColumnIndex(WallpaperLocalTable.WALLPAPER_PREVIEW_URL)));
        wallpaper.setPreviewPicturePath(cursor.getString(cursor.getColumnIndex(WallpaperLocalTable.WALLPAPER_PREVIEW_PATH)));
        wallpaper.setDailyIcon(cursor.getString(cursor.getColumnIndex(WallpaperLocalTable.WALLPAPER_DAILYICON)));
        NqLog.i("cursor/wallpaper=" + wallpaper.toString());
        return wallpaper;
    }

    /**
     * 将wallpaper对象转换层数据库操作的ContentValues对象
     *
     * @param column    0：新品；1：热门；-1：未知（下载壁纸，保存数据到Local表时，栏目字段为-1）
     * @param wallpaper
     * @return
     */
    public ContentValues wallpaperToContentValues(int column, Wallpaper wallpaper) {
        ContentValues values = null;
        if (wallpaper != null) {
            values = new ContentValues();
            values.put(WallpaperLocalTable.WALLPAPER_ID, wallpaper.getStrId());
            values.put(WallpaperLocalTable.WALLPAPER_NAME, wallpaper.getStrName());
            values.put(WallpaperLocalTable.WALLPAPER_SOURCE_TYPE, wallpaper.getIntSourceType());
            values.put(WallpaperLocalTable.WALLPAPER_TYPE, wallpaper.getIntType());
            values.put(WallpaperLocalTable.WALLPAPER_COLUMN, column);
            values.put(WallpaperLocalTable.WALLPAPER_AUTHOR, wallpaper.getStrAuthor());
            values.put(WallpaperLocalTable.WALLPAPER_VERSION, wallpaper.getStrVersion());
            values.put(WallpaperLocalTable.WALLPAPER_SOURCE, wallpaper.getStrSource());
            values.put(WallpaperLocalTable.WALLPAPER_SIZE, wallpaper.getLongSize());
            values.put(WallpaperLocalTable.WALLPAPER_DOWNLOAD_COUNT, wallpaper.getLongDownloadCount());
            values.put(WallpaperLocalTable.WALLPAPER_ICON_URL, wallpaper.getStrIconUrl());
            values.put(WallpaperLocalTable.WALLPAPER_ICON_PATH, wallpaper.getStrIconPath());
            values.put(WallpaperLocalTable.WALLPAPER_PREVIEW_URL,wallpaper.getPreviewPicture());
            values.put(WallpaperLocalTable.WALLPAPER_PREVIEW_PATH, wallpaper.getPreviewPicturePath());
            values.put(WallpaperLocalTable.WALLPAPER_URL, wallpaper.getStrWallpaperUrl());
            values.put(WallpaperLocalTable.WALLPAPER_PATH, wallpaper.getStrWallpaperPath());
            values.put(WallpaperLocalTable.WALLPAPER_UPDATETIME, wallpaper.getLongUpdateTime());
            values.put(WallpaperLocalTable.WALLPAPER_LOCALTIME, wallpaper.getLongLocalTime());
            values.put(WallpaperLocalTable.WALLPAPER_DAILYICON,wallpaper.getDailyIcon());
        }
        return values;
    }
    
    /**
     * 获取壁纸列表
     *
     * @param column
     * @param offset
     * @param WallpaperListListener
     */
    public void getWallpaperList(final int column, final int offset, final WallpaperListStoreListener listener) {
    	if(listener == null)
    		return;
    	
    	// 没有网络
    	if (!NetworkUtils.isConnected(mContext)) {
    		listener.onNoNetwork();
    		return;
    	}
    	
    	// 没有缓存，或缓存过期，联网查询数据。
    	WallpaperServiceFactory.getService().getWallpaperListList(mContext, column, offset, listener);
    }
    
    /**
     * 根据id 联网获取Wallpaper 详情。
     *
     * @param id
     * @param listener
     */
    public void getWallpaperDetail(String id, final WallpaperDetailListener listener) {
    	WallpaperServiceFactory.getService().getWallpaperDetailt(mContext, id, listener);
    }


    /**
     * 检测是否有壁纸缓存数据
     *
     * @param column 0：新品；1：热门；
     * @return
     */
    public boolean hadAvailableWallpaperListCashe(int column) {
    	Cursor cursor = null;
        try {
            ContentResolver contentResolver = mContext.getContentResolver();
            cursor = contentResolver.query(WallpaperCacheTable.WALLPAPER_CACHE_URI, new String[]{WallpaperCacheTable.WALLPAPER_ID}, WallpaperCacheTable.WALLPAPER_COLUMN + "=" + column, null, null);
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
     * 返回缓存的壁纸列表。
     *
     * @param column 0：新品；1：热门。
     * @return
     */
    public List<Wallpaper[]> getArrWallpaperListFromCache(int column) {
        List<Wallpaper[]> listWallpapers = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(WallpaperCacheTable.WALLPAPER_CACHE_URI, null,
            		WallpaperCacheTable.WALLPAPER_SOURCE_TYPE + "="
            	            + AppConstant.STORE_MODULE_TYPE_MAIN + " AND "
            	            + WallpaperCacheTable.WALLPAPER_COLUMN + "=?", new String[]{String.valueOf(column)}, WallpaperCacheTable._ID + " asc");
            if (cursor != null && cursor.getCount() > 0) {
                listWallpapers = new ArrayList<Wallpaper[]>();
                int index = 0;
                Wallpaper[] wallpapers = null;
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    if(index==0){
                    	wallpapers = new Wallpaper[3];
					}
                    wallpapers[index] = cursorToWallpaper(cursor);
                    if(index==2){
                    	listWallpapers.add(wallpapers);
						index = 0;
					}else{
						index++;
					}
                }
                if(cursor.getCount()%3!=0){
                	listWallpapers.add(wallpapers);
				}
            }
        } catch (Exception e) {
            NqLog.e("getArrWallpaperListFromCache " + e.toString());
        } finally {
			CursorUtils.closeCursor(cursor);
        }
        return listWallpapers;
    }

    /**
     * 返回本地的壁纸列表。
     *
     * @return
     */
    public List<Wallpaper> getWallpaperListFromLocal() {
        List<Wallpaper> listWallpapers = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(WallpaperLocalTable.LOCAL_WALLPAPER_URI, null, null, null, WallpaperLocalTable._ID + " asc");
            if (cursor != null && cursor.getCount() > 0) {
                listWallpapers = new ArrayList<Wallpaper>();
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    listWallpapers.add(cursorToWallpaper(cursor));
                }
            }
        } catch (Exception e) {
            NqLog.e("getWallpaperListFromLocal " + e.toString());
        } finally {
			CursorUtils.closeCursor(cursor);
        }
        return listWallpapers;
    }

    /**
     * 缓存壁纸数据
     *
     * @param column        0：新品；1：热门；
     * @param offset
     * @param arrWallpapers
     * @return
     */
    public boolean saveWallpaperCache(int column, int offset, List<Wallpaper[]> arrWallpapers) {
        long start = System.currentTimeMillis();
        try {
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            Builder b = null;
            if(offset == 0){
            	// 清除缓存
            	b = ContentProviderOperation.newDelete(WallpaperCacheTable.WALLPAPER_CACHE_URI)
                        .withSelection(WallpaperCacheTable.WALLPAPER_SOURCE_TYPE + "=" + AppConstant.STORE_MODULE_TYPE_MAIN + " AND " + WallpaperCacheTable.WALLPAPER_COLUMN + "=" + column, null);
                ops.add(b.build());
            }
            ContentResolver contentResolver = mContext.getContentResolver();
            
            if (arrWallpapers != null && arrWallpapers.size() > 0) {
                Wallpaper[] wallpapers = null;
                long localTime = new Date().getTime();
                for (int i = 0; i < arrWallpapers.size(); i++) {
                    wallpapers = arrWallpapers.get(i);
                    for (int j = 0; j < wallpapers.length; j++) {
                    	if (wallpapers[j] != null) {
                            wallpapers[j].setLongLocalTime(localTime);
                            ContentValues values = wallpaperToContentValues(column, wallpapers[j]);
                            b = ContentProviderOperation.newInsert(WallpaperCacheTable.WALLPAPER_CACHE_URI).withValues(values);
                            ops.add(b.build());
                        }
                    }
                    
                }
            }

            contentResolver.applyBatch(DataProvider.DATA_AUTHORITY,ops);
            //更新缓存时间。
            WallpaperPreference.getInstance().setLongValue(WallpaperPreference.KEY_WALLPAPER_LIST_CACHE_TIME[column], new Date().getTime());
            NqLog.i("saveWallpaperCache cost="+(System.currentTimeMillis() - start));
            return true;
        } catch (Exception e) {
            NqLog.e("saveWallpaperCache error " + e.toString());
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
     * @param wallpaper
     * @return
     */
    private boolean saveImages(Wallpaper wallpaper) {
        boolean result = false;
        if (wallpaper != null) {
            boolean r1 = saveImageToFile(wallpaper.getStrIconUrl(), wallpaper.getStrIconPath());
            boolean r2 = saveImageToFile(wallpaper.getPreviewPicture(), wallpaper.getPreviewPicturePath());
            result = r1 && r2;
        }
        return result;
    }

    /**
     * 壁纸数据存入本地库
     *
     * @param wallpaper
     * @return
     */
    private void saveWallpaper(final Wallpaper wallpaper) {
        (new Thread() {
            @Override
            public void run() {
                try {
                    ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                    Builder deleteOp = ContentProviderOperation.newDelete(
                            WallpaperLocalTable.LOCAL_WALLPAPER_URI).withSelection(
                            WallpaperLocalTable.WALLPAPER_ID + " = ?",
                            new String[]{wallpaper.getStrId()});
                    ops.add(deleteOp.build());

                    wallpaper.setLongLocalTime(System.currentTimeMillis());
                    ContentValues values = wallpaperToContentValues(-1,
                            wallpaper);
                    Builder insertOp = ContentProviderOperation.newInsert(
                            WallpaperLocalTable.LOCAL_WALLPAPER_URI)
                            .withValues(values);
                    ops.add(insertOp.build());

                    mContext.getContentResolver().applyBatch(
                    		DataProvider.DATA_AUTHORITY, ops);

                    saveImages(wallpaper);
                } catch (Exception e) {
                    NqLog.e(e);
                }
            }
        }).start();
    }

    /**
     * 根据id 从本地库获取Wallpaper详情。
     *
     * @param id
     * @return Wallpaper对象或null。null表示没有找到。
     */
    public Wallpaper getWallpaperDetailFromLocal(String id) {
        Wallpaper wallpaper = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(WallpaperLocalTable.LOCAL_WALLPAPER_URI, null,
                    WallpaperLocalTable.WALLPAPER_ID + "=?", new String[]{id}, WallpaperLocalTable._ID + " desc");
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToNext();
                wallpaper = cursorToWallpaper(cursor);
            }
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
			CursorUtils.closeCursor(cursor);
        }
        return wallpaper;
    }

    public static int getCount(List<Wallpaper[]> wallpapers) {
        if (null == wallpapers)
            return 0;
        int count = 0;
        for (int i = 0; i < wallpapers.size(); i++) {
        	for(int j=0; j<wallpapers.get(i).length; j++){
        		if (null != wallpapers.get(i)[j] &&
                        !TextUtils.isEmpty(wallpapers.get(i)[j].getStrId()))
                    count++;
        	}
            
        }
        NqLog.d("wallpaper count=" + count);
        return count;
    }

    /**
     * 获取当前使用壁纸的ID
     *
     * @return
     */
    public String getCurrentWallpaperID() {
        String id = WallpaperPreference.getInstance().getStringValue(WallpaperPreference.KEY_CURRENT_WALLPAPER);
        return id;
    }

    /**
     * @param wallpaperId 壁纸资源ID
     * @author chenyanmin
     * @time 2014-1-26 下午10:12:01
     */
    public void setCurrentWallpaperID(String wallpaperId) {
    	WallpaperPreference.getInstance().setStringValue(WallpaperPreference.KEY_CURRENT_WALLPAPER,
                wallpaperId);
    }

    private boolean isDefaultWallpaper(Wallpaper wallpaper) {
        return wallpaper != null && DEFAULT_WALLPAPER.equals(wallpaper.getStrWallpaperPath());
    }

    private boolean isCurrentWallpaper(Wallpaper wallpaper) {
        String currentWallpaperId = getCurrentWallpaperID();
        if ("".equals(currentWallpaperId)) {
            if (isDefaultWallpaper(wallpaper)) {// 默认壁纸
                return true;
            } else {
                return false;
            }
        }

        return currentWallpaperId.equals(wallpaper.getStrId());
    }

    public static class WallpaperStatus {
        public int statusCode;
        public long downloadedBytes;
        public long totalBytes;
    }

    /**
     * 返回Wallpaper的下载、安装状态
     *
     * @param app
     * @return 总是返回非null
     * @author chenyanmin
     * @time 2014-1-23 上午11:06:17
     */
    public WallpaperStatus getStatus(Wallpaper wallpaper) {
        WallpaperStatus result = new WallpaperStatus();
        result.statusCode = STATUS_UNKNOWN;

        if (wallpaper == null) {
            return result;
        }

        if (isCurrentWallpaper(wallpaper)) {
            result.statusCode = STATUS_CURRENT_WALLPAPER;
        } else {
            MyDownloadManager downloader = MyDownloadManager.getInstance(mContext);
            Long downloadId = downloader.getDownloadId(wallpaper.getStrWallpaperUrl());
            int[] bytesAndStatus = downloader.getBytesAndStatus(downloadId);
            if (bytesAndStatus[0] == 1) {
                result.statusCode = convertStatus(bytesAndStatus[1]);
                if(result.statusCode == STATUS_DOWNLOADED) {
                	String filePath = wallpaper.getStrWallpaperPath();
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
     * 应用壁纸
     *
     * @param wallpaper
     */
    public synchronized boolean applyWallpaper(final Wallpaper wallpaper,final boolean needToast) {
        NqLog.i("applyWallpaper wallpaper="+wallpaper);
        setWP_APPLYING(true);
    	boolean result = false;

    	File file = new File(wallpaper.getStrWallpaperPath());
        if (wallpaper == null || TextUtils.isEmpty(wallpaper.getStrWallpaperPath())
        		|| !file.exists()) {
        	setWP_APPLYING(false);
            return result;
        }

        try {
        	new Thread(new Runnable() {
				
				@Override
				public void run() {
					InputStream is = ImageManager.getInstance(mContext).getSavedWallpaper2(wallpaper.getStrWallpaperPath());
		            if (is != null) {
		                android.app.WallpaperManager wallpaperManager = android.app.WallpaperManager.getInstance(mContext);
		                try {
							wallpaperManager.setStream(is);
						} catch (IOException e) {
							e.printStackTrace();
						}finally{
							if(is != null){
								try {
									is.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
		                setCurrentWallpaperID(wallpaper.getStrId());
		                List<OnUpdateListener> listeners = LauncherSDK.getInstance(mContext).getRegisteredOnUpdateListeners();
		                if (listeners != null) {
		                    for (int i = 0; i < listeners.size(); i++) {
		                        listeners.get(i).onCurrentWallpaperUpdate(wallpaper);// 通知刷新
		                    }
		                }

		                String APPLY_WALLPAPER_ACTION = "com.live.store.wallpaper.apply";
		                Intent intent = new Intent(APPLY_WALLPAPER_ACTION);
		                intent.putExtra("wallpaperid", wallpaper.getStrId());
		                mContext.sendBroadcast(intent);
		            }
				}
			}).start();
            
        } catch (Exception e) {
            NqLog.e(e);
        }

        result = true;
        if(needToast){
        	Activity a = new Activity();
        	a.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					ToastUtils.toast(mContext, "nq_set_theme_succ");
				}
			});
        	if(a != null){
        		a = null;
        	}
        	
        }
        setWP_APPLYING(false);
        return result;
    }

    public boolean deleteWallpaper(Wallpaper w){
        NqLog.i("deleteWallpaper w="+w);
        boolean result = false;
        if (w == null || TextUtils.isEmpty(w.getStrWallpaperPath())) {
            return result;
        }

        String id = w.getStrId();
        mContext.getContentResolver().delete(DownloadTable.TABLE_URI,DownloadTable.DOWNLOAD_RES_ID + " = ?",new String[]{id});
        mContext.getContentResolver().delete(WallpaperLocalTable.LOCAL_WALLPAPER_URI,WallpaperLocalTable.WALLPAPER_ID + " = ?",new String[]{id});
        FileUtil.delFile(w.getPreviewPicturePath());
        FileUtil.delFile(w.getStrWallpaperPath());
        FileUtil.delFile(w.getStrIconPath());
        if(id.equalsIgnoreCase(getCurrentWallpaperID()))
        	setCurrentWallpaperID("");

        result = true;
        List<OnUpdateListener> onUpdateListeners = LauncherSDK.getInstance(mContext).getRegisteredOnUpdateListeners();
        if (onUpdateListeners != null && onUpdateListeners.size() > 0) {
            for(OnUpdateListener l : onUpdateListeners){
                l.onWallpaperDelete(w);
            }
        }
        return result;
    }


    /**
     * 下载Wallpaper
     *
     * @param wallpaper Wallpaper对象
     * @author chenyanmin
     * @time 2014-1-23 下午2:39:51
     */
    public Long downloadWallpaper(Wallpaper wallpaper) {
        Long result = null;

        if (wallpaper == null || TextUtils.isEmpty(wallpaper.getStrWallpaperUrl())
                || DEFAULT_WALLPAPER.equals(wallpaper.getStrWallpaperPath())) {
            return result;
        }

        if (!NetworkUtils.isConnected(mContext)) {
        	ToastUtils.toast(mContext, "nq_nonetwork");
            return result;
        }

        try {
            Long downloadId = MyDownloadManager.getInstance(mContext).downloadWallpaper(wallpaper);
            if (downloadId != null) {
                saveWallpaper(wallpaper);
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
     * @param wallpaper
     * @param observer
     * @author chenyanmin
     * @time 2014-1-23 下午5:43:00
     */
    public void registerDownloadObserver(Wallpaper wallpaper,
                                         IDownloadObserver observer) {
        if (wallpaper == null || observer == null) {
            return;
        }

        MyDownloadManager downloader = MyDownloadManager.getInstance(mContext);
        Long downloadId = downloader.getDownloadId(wallpaper
                .getStrWallpaperUrl());
        downloader.registerDownloadObserver(downloadId, observer);
    }

    /**
     * 取消下载进度监听
     *
     * @param wallpaper
     * @author chenyanmin
     * @time 2014-1-23 下午5:43:29
     */
    public void unregisterDownloadObserver(Wallpaper wallpaper) {
        if (wallpaper == null) {
            return;
        }

        MyDownloadManager downloader = MyDownloadManager.getInstance(mContext);
        Long downloadId = downloader.getDownloadId(wallpaper
                .getStrWallpaperUrl());
        downloader.unregisterDownloadObserver(downloadId);
    }

    private Wallpaper getSavedWallpaper(String wallpaperId) {
        Wallpaper result = null;
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = mContext.getContentResolver();
            cursor = contentResolver.query(WallpaperLocalTable.LOCAL_WALLPAPER_URI, null, WallpaperLocalTable.WALLPAPER_ID + " = ?", new String[]{wallpaperId}, WallpaperLocalTable._ID + " DESC");
            if (cursor != null && cursor.moveToFirst()) {
                result = cursorToWallpaper(cursor);
            }
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
			CursorUtils.closeCursor(cursor);
        }

        return result;
    }

    public void OnDownloadComplete(String wallpaperId) {
        if (TextUtils.isEmpty(wallpaperId)) {
            return;
        }

        Wallpaper wallpaper = getSavedWallpaper(wallpaperId);
        if (wallpaper == null) {
            return;
        }

        Intent i = new Intent();
        i.setAction(LiveReceiver.ACTION_WALLPAPER_DOWNLOAD);
        i.putExtra("wallpaper", wallpaper);
        mContext.sendBroadcast(i);
    }
    
    public boolean isCacheExpired(int column){
        Long cacheTime = 0l;
        cacheTime = WallpaperPreference.getInstance().getLongValue(WallpaperPreference.KEY_WALLPAPER_LIST_CACHE_TIME[column]);
        Long currentTime = new Date().getTime();
    	return currentTime - cacheTime > CACHE_MAX_TIME;
    }

	public void goWallpaperDetail(Wallpaper wallpaper) {
		if(wallpaper == null)
			return;
		ArrayList<Wallpaper> wallpapers = new ArrayList<Wallpaper>();
		wallpapers.add(wallpaper);
		Intent intent = new Intent(mContext, WallpaperDetailActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(WallpaperDetailActivity.KEY_WALLPAPERS, wallpapers);
		intent.putExtra(WallpaperDetailActivity.KEY_WALLPAPER_POS, 0);
		mContext.startActivity(intent);
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
                        case MyDownloadManager.DOWNLOAD_TYPE_WALLPAPER:
                            WallpaperManager.getInstance(context).OnDownloadComplete(resId);
                            NotificationUtil.showWallpaperNotifi(context,resId);
                            StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
                            		AppActionConstants.ACTION_LOG_1502, resId, StatManager.ACTION_DOWNLOAD,
                                    "2_1");
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
	
	 public void onEvent(GetWallpaperDetailSuccessEvent event){
		 WallpaperDetailListener listener = (WallpaperDetailListener) event.getTag();
		 if (event.isSuccess()) {
			 listener.onGetDetailSucc(event.getWallpaper());
		 }else {
			listener.onErr();
		}
	 }
	 
	 public void onEvent(GetWallpaperListSuccessEvent event){
		 WallpaperListStoreListener listener = (WallpaperListStoreListener) event.getTag();
		 if (event.isSuccess()) {
			 listener.onGetWallpaperListSucc(event.getColumn(), event.getOffset(), event.getWallpapers());
		 }else {
			listener.onErr();
		}
	 }	
	 
	public void onEvent(DownloadCompleteEvent event) {
		processStoreDownload(mContext, event.mRefer);
	}
	
}
