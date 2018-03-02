package com.nqmobile.livesdk.commons.mydownloadmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import com.nqmobile.livesdk.commons.db.DataProvider;
import com.nqmobile.livesdk.commons.downloadmanager.Constants;
import com.nqmobile.livesdk.commons.downloadmanager.DownloadManager;
import com.nqmobile.livesdk.commons.downloadmanager.DownloadManager.Request;
import com.nqmobile.livesdk.commons.downloadmanager.Downloads;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.info.ClientInfo;
import com.nqmobile.livesdk.commons.log.NqLog;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.mydownloadmanager.table.DownloadTable;
import com.nqmobile.livesdk.commons.receiver.DownloadCompleteEvent;
import com.nqmobile.livesdk.commons.receiver.LauncherResumeEvent;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.font.NqFont;
import com.nqmobile.livesdk.modules.locker.Locker;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.modules.theme.Theme;
import com.nqmobile.livesdk.modules.wallpaper.Wallpaper;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NotificationUtil;
import com.nqmobile.livesdk.utils.ToastUtils;

/**
 * 下载类
 * 
 * @author chenyanmin
 * @time 2014-01-22
 */
public class MyDownloadManager extends AbsManager{
	private static MyDownloadManager sDlMgr;
	private Context mContext;
	private DownloadManager mDownloader;
	private ContentObserver mContentObserver; 
	private Map<Long, ObserverWrapper> mObservers;
	private static final Uri DOWNLOAD_MANAGER_CONTENT_URI = Downloads.ALL_DOWNLOADS_CONTENT_URI;

	public static final int STATUS_NONE = 0;// 无效的downloadId，没有找到该下载
	public static final int STATUS_PENDING = 1;// 尚未开始下载，等待中
	public static final int STATUS_RUNNING = 2;// 下载中
	public static final int STATUS_PAUSED = 3;// 下载暂停
	public static final int STATUS_FAILED = 4;// 下载失败
	public static final int STATUS_SUCCESSFUL = 5;// 已下载
	
	public static final String MIME_TYPE_APK = "application/vnd.android.package-archive";
	public static final int NO_NETWORK = 0;
	
	public static final int DOWNLOAD_TYPE_APP = 0;
	public static final int DOWNLOAD_TYPE_WALLPAPER = 1;
	public static final int DOWNLOAD_TYPE_THEME = 2;
	public static final int DOWNLOAD_TYPE_LOCKER = 3;
	public static final int DOWNLOAD_TYPE_FONT = 4;
	public static final int DOWNLOAD_TYPE_SLIENT = -100;

    public static final int SHOW_INSTALL_FLAG = 0;
    public static final int NOT_SHOW_INSTALL_FLAG = 1;

    public static final int DOWNLOAD_NOT_FINISH = 0;
    public static final int DOWNLOAD_FINISH = 1;
    

	private static final long CHECK_DOWNLOAD = 3 * DateUtils.DAY_IN_MILLIS;
	public static final int DOWNLOAD_TYPE_lqWidget = 0123;
	
    private Set<Long> mExistedDownloadIds = new HashSet<Long>();

    @Override
    public void init() {
        EventBus.getDefault().register(this);
    }

    private class ObserverWrapper{
        public Set<IDownloadObserver> observer;
        public long bytesSoFar;
        public long status;

        public ObserverWrapper(){
            observer = new HashSet<IDownloadObserver>();
            bytesSoFar = -1;//unknown
            status = -1;//unknown
        }

        public void addObserver(IDownloadObserver ob){
            observer.add(ob);
        }
    }
	
	private MyDownloadManager(Context context) {
		mContext = context;
		//mDownloader = (DownloadManager) mContext
		//		.getSystemService(Context.DOWNLOAD_SERVICE);
		mDownloader = DownloadManager.getInstance(context.getContentResolver(), context.getPackageName());
		mContentObserver = new MyObserver();
		mObservers = new ConcurrentHashMap<Long, ObserverWrapper>();
        EventBus.getDefault().register(this);
	}

	public synchronized static MyDownloadManager getInstance(Context context) {
		if (sDlMgr == null) {
			sDlMgr = new MyDownloadManager(context.getApplicationContext());
		}
		return sDlMgr;
	}

    public static class DownloadParameter{
        public int type;
        public String resId;
        public String downloadUrl;
        public String iconUrl;
        public String desPath;
        public String name;
        public long totalSize;
        public int networkflag;
        public int order;
        public boolean background;
        public String packageName;
    }
	
    private int getDownloadNetwork() {
		ConnectivityManager connectivity = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (null == connectivity) {
			return NO_NETWORK;
		}
		
		NetworkInfo info = connectivity.getActiveNetworkInfo();
		if (null == info) {
			return NO_NETWORK;
		}

		// if current network is WIFI, set download network type to NETWORK_WIFI, indicates download will only proceed under WIFI
		// if current network is MOBIE(2g/3g) , set download network type to NETWORK_WIFI|NETWORK_MOBILE, indicates download will always proceed
		if (info.getType() == ConnectivityManager.TYPE_WIFI) {
			NqLog.i("current network: WIFI");
			return Request.NETWORK_WIFI;
		} else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
			NqLog.i("current network: 3g");
			return Request.NETWORK_MOBILE | Request.NETWORK_WIFI;
		} else {
			return NO_NETWORK;
		}

    }

	public Long downloadApp(App app) {
        return downloadApp(app,getDownloadNetwork());
	}

    public Long downloadApp(App app,int networkflag) {
        if (app == null) {
            return null;
        }

        DownloadParameter para = new DownloadParameter();
        para.type = DOWNLOAD_TYPE_APP;
        para.resId = app.getStrId();
        para.downloadUrl = app.getStrAppUrl();
        para.iconUrl = app.getStrIconUrl();
        para.desPath = app.getStrAppPath();
        para.name = app.getStrName();
        para.totalSize = app.getLongSize();
        para.networkflag = networkflag;
        para.order = app.order;
        para.background = false;
        para.packageName = app.getStrPackageName();

        return download(para);
    }

	public Long downloadApp_background(App app) {
        return downloadApp_background(app,getDownloadNetwork());
	}

	/**
	 * 静默下载app,和非静默下载基本一致，暂时为不影响别的模块先不重构，以后需要
	 * @param app
	 * @param networkflag
	 * @return
	 */
    public Long downloadApp_background(App app,int networkflag) {
        if (app == null) {
            return null;
        }

        DownloadParameter para = new DownloadParameter();
        para.type = DOWNLOAD_TYPE_APP;
        para.resId = app.getStrId();
        para.downloadUrl = app.getStrAppUrl();
        para.iconUrl = app.getStrIconUrl();
        para.desPath = app.getStrAppPath();
        para.name = app.getStrName();
        para.totalSize = app.getLongSize();
        para.networkflag = networkflag;
        para.order = app.order;
        para.background = true;
        para.packageName = app.getStrPackageName();

        return download(para);
    }

	public Long downloadTheme(Theme theme) {
		if (theme == null) {
			return null;
		}

        DownloadParameter para = new DownloadParameter();
        para.type = DOWNLOAD_TYPE_THEME;
        para.resId = theme.getStrId();
        para.downloadUrl = theme.getStrThemeUrl();
        para.desPath = theme.getStrThemePath();
        //para.iconUrl = theme.getStrIconUrl();
        //显示在下载管理页面的图标，取每日推荐的图标
        para.iconUrl = theme.getDailyIcon();
        para.name = theme.getStrName();
        para.totalSize = theme.getLongSize();
        para.networkflag = getDownloadNetwork();
        para.order = 0;
        para.background = false;

		return download(para);
	}
	
	public Long downloadFont(NqFont font) {
		if (font == null) {
			return null;
		}

        DownloadParameter para = new DownloadParameter();
        para.type = DOWNLOAD_TYPE_FONT;
        para.resId = font.getStrId();
        para.downloadUrl = font.getStrFontUrl();
        para.desPath = font.getStrFontPath();
        //显示在下载管理页面的图标，取每日推荐的图标
        para.name = font.getStrName();
        para.totalSize = font.getLongSize();
        para.networkflag = getDownloadNetwork();
        para.order = 0;
        para.background = false;

		return download(para);
	}

	public Long downloadWallpaper(Wallpaper wallpaper) {
		if (wallpaper == null) {
			return null;
		}

        DownloadParameter para = new DownloadParameter();
        para.type = DOWNLOAD_TYPE_WALLPAPER;
        para.resId = wallpaper.getStrId();
        para.downloadUrl = wallpaper.getStrWallpaperUrl();
        para.desPath =  wallpaper.getStrWallpaperPath();
        para.iconUrl = wallpaper.getStrIconUrl();
        para.name = wallpaper.getStrName();
        para.totalSize = wallpaper.getLongSize();
        para.networkflag = getDownloadNetwork();
        para.order = 0;
        para.background = false;

		return download(para);
	}

	public Long downloadLocker(Locker locker) {
		if (locker == null) {
			return null;
		}

        DownloadParameter para = new DownloadParameter();
        para.type = DOWNLOAD_TYPE_LOCKER;
        para.resId = locker.getStrId();
        para.downloadUrl = locker.getStrLockerUrl();
        para.desPath =  locker.getStrLockerPath();
        //para.iconUrl = locker.getStrIconUrl();
        //显示在下载管理页面的图标，取每日推荐的图标
        para.iconUrl = locker.getStrDailyIconUrl();
        para.name = locker.getStrName();
        para.totalSize = locker.getLongSize();
        para.networkflag = getDownloadNetwork();
        para.order = 0;
        para.background = false;

		return download(para);
	}

    public void flagDownloadFinish(long downloadId){
        ContentValues value = new ContentValues();
        value.put(DownloadTable.DOWNLOAD_IS_FINISH,DOWNLOAD_FINISH);
        mContext.getContentResolver().update(DownloadTable.TABLE_URI,value,DownloadTable.DOWNLOAD_DOWNLOAD_ID + " = ?",new String[]{String.valueOf(downloadId)});
    }

    public void flagInstallShowFlag(long downloadId,int flag){
        ContentValues value = new ContentValues();
        value.put(DownloadTable.DOWNLOAD_SHOWFLAG,flag);
        mContext.getContentResolver().update(DownloadTable.TABLE_URI,value,DownloadTable.DOWNLOAD_DOWNLOAD_ID + " = ?",new String[]{String.valueOf(downloadId)});
    }

	/**para isBackground: true 静默下载，不在通知栏显示进度信息
	 *
	*/
	public Long download(DownloadParameter para) {
		Long result = null;

		if (para.downloadUrl == null) {
			return result;
		}

		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
				|| para.desPath != null && para.desPath.startsWith("null")) {
			if (Looper.myLooper() == Looper.getMainLooper()) {
				ToastUtils.toast(mContext, "nq_label_no_sdcard");
			}
			NqLog.i("MyDownloadManager download failed: no sdcard, url=" + para.downloadUrl + " ,destPath= " + para.desPath);
			return result;
		}
		try {
			Uri uri = Uri.parse(para.downloadUrl);
			Request request = new Request(uri);
			//request.setMimeType(MIME_TYPE_APK);
			request.setAllowedNetworkTypes(para.networkflag);
			if (!para.background) {
				request.setTitle(para.name);
				request.setDescription(" ");//为了使DownloadManager在通知栏显示的进度条包括百分比，好像Description需要设置内容
				request.setVisibleInDownloadsUi(true);
				request.setShowRunningNotification(true);
			} else{
				request.setVisibleInDownloadsUi(false);
				request.setShowRunningNotification(false);								
			}
            String dir = para.desPath.substring(0, para.desPath.lastIndexOf("/"));
            File downloadDir = new File(dir);
            if(!downloadDir.exists()){
                downloadDir.mkdirs();
            }

			File file = new File(para.desPath);
            if (file.exists()){
				file.delete();
			}
			request.setDestinationUri(Uri.fromFile(file));
        	
			Long id = getDownloadIdByResID(para.resId);
			// 如果此资源已经下载过，从系统下载管理器表中remove
			if (id != null) {
				cancelDownload(id);
			}
				
			long downloadId = mDownloader.enqueue(request);

			Builder b = null;
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			ContentValues values = new ContentValues();
			values.put(DownloadTable.DOWNLOAD_DOWNLOAD_ID, downloadId);
			values.put(DownloadTable.DOWNLOAD_TYPE, para.type);
            values.put(DownloadTable.DOwNLOAD_ICON_URL,para.iconUrl);
            values.put(DownloadTable.DOWNLOAD_NAME,para.name);				
			if (id == null) {
				values.put(DownloadTable.DOWNLOAD_DEST_PATH, para.desPath);
				values.put(DownloadTable.DOWNLOAD_TOTAL_SIZE, para.totalSize);
				values.put(DownloadTable.DOWNLOAD_URL, para.downloadUrl);
				values.put(DownloadTable.DOWNLOAD_RES_ID, para.resId);
	            values.put(DownloadTable.DOWNLOAD_PACKAGENAME,para.packageName);
	            b = ContentProviderOperation.newInsert(DownloadTable.TABLE_URI).withValues(values);
			} else {
				values.put(DownloadTable.DOWNLOAD_IS_FINISH, 0);
				values.put(DownloadTable.DOWNLOAD_SHOWFLAG, 0);			
				b = ContentProviderOperation.newUpdate(DownloadTable.TABLE_URI)
                .withValues(values)
                .withSelection(DownloadTable.DOWNLOAD_RES_ID + " =? ", new String[]{para.resId});
			}
			ops.add(b.build());
			mContext.getContentResolver().applyBatch(DataProvider.DATA_AUTHORITY, ops);
			
			NqLog.d("MyDownloadManager downloadId:" + downloadId + ", url= "+ para.downloadUrl + " ,destPath = " + para.desPath);
			result = downloadId;
		} catch (Exception e) {
			NqLog.d("MyDownloadManager download failed: url= "+ para.downloadUrl + " ,destPath = " + para.desPath);
			e.printStackTrace();
			NqLog.e(e);
		}
		return result;
	}
	
	public Long getDownloadId(String url) {
		Long result = null;
		Cursor cursor = null;
		try {
			ContentResolver contentResolver = mContext.getContentResolver();
			cursor = contentResolver.query(DownloadTable.TABLE_URI, null,
                    DownloadTable.DOWNLOAD_URL + " = ?", new String[] { url },
                    DownloadTable._ID + " DESC LIMIT 1 OFFSET 0");
			if (cursor != null && cursor.moveToNext()) {
                result = cursor.getLong(DownloadTable.DOWNLOAD_DOWNLOAD_ID_INDEX);
			}
		} catch (Exception e) {
			NqLog.e(e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return result;
	}
	
	public Long getDownloadIdByResID(String resId) {
		Long result = null;
		Cursor cursor = null;
		try {
			ContentResolver contentResolver = mContext.getContentResolver();
			cursor = contentResolver.query(DownloadTable.TABLE_URI, null,
                    DownloadTable.DOWNLOAD_RES_ID + " = ?", new String[] { resId },
                    DownloadTable._ID + " DESC LIMIT 1 OFFSET 0");
			if (cursor != null && cursor.moveToNext()) {
				result = cursor.getLong(DownloadTable.DOWNLOAD_DOWNLOAD_ID_INDEX);
			}
		} catch (Exception e) {
			NqLog.e(e);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		
		return result;
	}
	
	// 暂停一个下载
	public void pauseDownload(long downloadId) {
		mDownloader.pauseDownload(downloadId);
	}
	
	// 恢复一个已暂停的下载
	public void resumeDownload(long downloadId) {
		mDownloader.resumeDownload(downloadId);
	}
	
	// 取消一个下载
	public void cancelDownload(long downloadId){
		mDownloader.remove(downloadId);
		NqLog.d("cancelDownload downloadId=" + downloadId);
		ObserverWrapper observerWrapper = mObservers.get(downloadId);
		if (observerWrapper != null) {
			for (IDownloadObserver ob : observerWrapper.observer) {
				ob.onChange();
			}
		}
		
	}
	
	// 修改下载任务的允许下载网络类型
	public void modifyDownloadNetwork(int networkType, long downloadId) {
		mDownloader.modifyDownloadNetwork(networkType, downloadId);
	}
	
	/**
	 * 将DownLoadManager的Status转成MyDownLoadManager的Status
	 * @author chenyanmin
	 * @time 2014-1-23 下午5:07:37
	 * @param status
	 * @return MyDownLoadManager的STATUS_*
	 */
	private int convertStatus(int status){
		int result = STATUS_NONE;
		
		switch (status) {
		case DownloadManager.STATUS_FAILED:
			result = STATUS_FAILED;
			break;
		case DownloadManager.STATUS_PAUSED:
			result = STATUS_PAUSED;
			break;
		case DownloadManager.STATUS_PENDING:
			result = STATUS_PENDING;
			break;
		case DownloadManager.STATUS_RUNNING:
			result = STATUS_RUNNING;
			break;
		case DownloadManager.STATUS_SUCCESSFUL:
			result = STATUS_SUCCESSFUL;
			break;
		default:
			break;
		}
		
		return result;
	}

    public void registerDownloadObserver(Long downloadId, IDownloadObserver observer){
        if (downloadId == null){
            return;
        }

        int[] bytesAndStatus = getBytesAndStatus(downloadId);
        //Status为STATUS_SUCCESSFUL的就不用监控了
        if (bytesAndStatus[0] == 1 && bytesAndStatus[1] != STATUS_SUCCESSFUL){
            ObserverWrapper ob = mObservers.get(downloadId);
            if(ob != null){
                ob.addObserver(observer);
                mObservers.put(downloadId, ob);
            }else{
                ObserverWrapper o = new ObserverWrapper();
                o.addObserver(observer);
                mObservers.put(downloadId, o);
            }

            mContext.getContentResolver().registerContentObserver(DOWNLOAD_MANAGER_CONTENT_URI, true,
                    mContentObserver);
        }
    }
	
	/**
	 * 去除一个下载进度监听
	 * @author chenyanmin
	 * @time 2014-1-23 下午5:01:16
	 * @param downloadId
	 */
	public void unregisterDownloadObserver(Long downloadId){	
		if (downloadId == null){
			return;
		}
		
		mObservers.remove(downloadId);
		if (mObservers.size() <= 0) {
			mContext.getContentResolver().unregisterContentObserver(mContentObserver);
		}
	}
	
	
	/**
	 * 下载进度状态监听器
	 * @author chenyanmin
	 * @time 2014-1-23 下午5:10:07
	 */
	class MyObserver extends ContentObserver {

		public MyObserver(){
			super(null);
		}

		@Override
		public void onChange(boolean selfChange) {
            notifyChange();
		}

	}
	
	private long[] toPrimitiveArray(Set<Long> values) {
	    long[] result = new long[values.size()];
	    int i = 0;
	    for (Iterator<Long> iter = values.iterator();iter.hasNext();){
            result[i++] = iter.next();
        }

	    return result;
	}
	
    public void notifyChange(){
        long[]  downloadIds = toPrimitiveArray(mObservers.keySet());
        if (downloadIds == null || downloadIds.length == 0) 
        	return;
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadIds);
        Cursor c = null;
		if(query == null)
			return;
		
		try {
			c = mDownloader.query(query);
            if (c == null){
				return;
			}
            mExistedDownloadIds.clear();
			while(c.moveToNext()){
				Long id = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID));
				mExistedDownloadIds.add(id);
				long bytesSoFar = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
	            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

				if (mObservers.containsKey(id)){
					ObserverWrapper wrapper = mObservers.get(id);
					long oldBytesSoFar = wrapper.bytesSoFar;
					long oldStatus = wrapper.status;
					NqLog.d(" id=" + id + " oldBytesSoFar:" + oldBytesSoFar + " bytesSoFar" + bytesSoFar +
							" oldStatus" + oldStatus + ", status:" + status);
					if (bytesSoFar != oldBytesSoFar || status != oldStatus){
						wrapper.bytesSoFar = bytesSoFar;
						wrapper.status = status;
//                        for(IDownloadObserver ob : wrapper.observer){
//                            ob.onChange();
//                        }

                        for(Iterator<IDownloadObserver> iter = wrapper.observer.iterator();iter.hasNext();){
                            IDownloadObserver ob = iter.next();
                            try {
                            	if(ob instanceof IDownloadObserver2){
                            		IDownloadObserver2 callback2 = (IDownloadObserver2)ob;
                            		callback2.onChange(id, bytesSoFar, status);
                            	}else{
                            		ob.onChange();
                            	}
    						} catch (Exception e) {
    							NqLog.e(e);
    						}
                        }
						//NqLog.d("cym", "threadName=" + Thread.currentThread().getName() + "notifyChange: size=" + mObservers.size() + " id=" + id + " lastModifiedTimestamp=" + wrapper.bytesSoFar);
					}
					// remove observer when the download is failed or completed
					if (status == DownloadManager.STATUS_FAILED || status == DownloadManager.STATUS_SUCCESSFUL){
		            	mObservers.remove(id);
		            }	
				}
			}			
		}catch (Exception e) {
			NqLog.e("MyDownloadManager notifyChange err:" + e);
            e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
			}
		}
		
		//通知已被删除的下载
		for(Long id : downloadIds){
			if (!mExistedDownloadIds.contains(id)){
				if (mObservers.containsKey(id)) {
					ObserverWrapper wrapper = mObservers.get(id);
					for (Iterator<IDownloadObserver> iter = wrapper.observer
							.iterator(); iter.hasNext();) {
						IDownloadObserver ob = iter.next();
						try {
							ob.onChange();
						} catch (Exception e) {
							NqLog.e(e);
						}
					}
					mObservers.remove(id);
				}
			}
		}
    }
	
	
	/**
	 * 获取一个下载的进度信息
	 * @author chenyanmin
	 * @time 2014-1-23 下午5:03:24
	 * @param downloadId
	 * @return int[4]{能否查到进度信息（1能,0不能），MyDownloadManager的Status，已下载字节数，总字节数}
	 */
	public int[] getBytesAndStatus(Long downloadId) {
		int[] bytesAndStatus = new int[] {0, -1, -1, 0 };
		if (downloadId == null){
			return bytesAndStatus;
		}	

		DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
		Cursor c = null;
		try {
			c = mDownloader.query(query);
			if (c != null && c.moveToFirst()) {
				bytesAndStatus[0] = 1;
				bytesAndStatus[1] = convertStatus(c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)));
				bytesAndStatus[2] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
				bytesAndStatus[3] = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
				NqLog.d("mydownmanager downloadId:" + downloadId + ", staus:" + bytesAndStatus[1]
						+ ", downsize:" + bytesAndStatus[2] + " tot:" + bytesAndStatus[3]);
			}
			
			if (bytesAndStatus[1] == STATUS_SUCCESSFUL){
				String local_uri = c.getString((c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
				if (local_uri != null) {
					Uri uri = Uri.parse(local_uri);
					File destFile = new File(uri.getPath());
					if (!destFile.exists()) {// SD卡上的文件不存在、SD卡拔除、换SD卡等情况
						bytesAndStatus[0] = 0;
					}
				}
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return bytesAndStatus;
	}

    public void onEvent(DownloadCompleteEvent event){
        flagDownloadFinish(event.getRefer());
    }
    
    /**
     * 收到定期检查事件，看是否有下载未安装的应用，如果有则发push通知
     * @param event
     */
    public void onEvent(LauncherResumeEvent event){
    	//如果是gp版本则不提示push
    	Log.i("MyDownload","in onEvent.");
		MyDownloadPreference preference = MyDownloadPreference.getInstance();
		if (!preference.getBooleanValue(MyDownloadPreference.KEY_MYDOWNLOAD_ENABLE)) {
			Log.i("MyDownload","MyDownloadPreference  KEY_MYDOWNLOAD_ENABLE is false.");
			return;
		} else{
			Log.i("MyDownload","MyDownloadPreference  KEY_MYDOWNLOAD_ENABLE is true.");
		}
    	
		long now = SystemFacadeFactory.getSystem().currentTimeMillis();

		Log.i("MyDownload", "MyDonwloadManager received LauncherResumeEvent...");
		if (now - preference.getLastCheckDownload() > CHECK_DOWNLOAD) {
			Log.i("MyDownload","MyDonwloadManager > 3 days.");
			if (checkIfHasNotInstalledApp() == true) {
				Log.i("MyDownload","checkIfHasNotInstalledApp is true.");
				sendcheckDownloadNotification();
				preference.setLastCheckDownload(now);

				// 记录展示的行为日志
				StatManager.getInstance().onAction(
						StatManager.TYPE_STORE_ACTION,
						MyDownloadConstants.ACTION_LOG_1908, null, 0, null);
			}else{
				Log.i("MyDownload","checkIfHasNotInstalledApp is false.");
			}
		}
	}

    private boolean checkIfHasNotInstalledApp() {
        Cursor cursor = null;
        boolean ret = false;
        try{
            cursor = mContext.getContentResolver().query(DownloadTable.TABLE_URI,null,
                    DownloadTable.DOWNLOAD_IS_FINISH + " = ? AND " + DownloadTable.DOWNLOAD_TYPE + "  = ? AND " + DownloadTable.DOWNLOAD_SHOWFLAG + " = ?",
                    new String[]{String.valueOf(DOWNLOAD_FINISH),
                            String.valueOf(DOWNLOAD_TYPE_APP),
                            String.valueOf(SHOW_INSTALL_FLAG)},null);
            while (cursor != null && cursor.moveToNext()){
                String resId = cursor.getString(cursor.getColumnIndex(DownloadTable.DOWNLOAD_RES_ID));
                if (TextUtils.isEmpty(resId)) {
                	continue;
                }
                
                if(!AppManager.getInstance(mContext).isAppInstallByResId(resId)){
                	String path = cursor.getString(cursor.getColumnIndex(DownloadTable.DOWNLOAD_DEST_PATH));
                    File file = new File(path);
                    if(file.exists()){
	                	ret = true;
	                	break;
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();            
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return ret;
    }
    
    private void sendcheckDownloadNotification() {
		if (!MyDownloadPreference.getInstance().getBooleanValue(
				MyDownloadPreference.KEY_MYDOWNLOAD_ENABLE)) {
			NqLog.d("MyDownloadPreference  KEY_MYDOWNLOAD_ENABLE is false.");
			return;
		} 
		
		NqLog.d("MyDownloadPreference  KEY_MYDOWNLOAD_ENABLE is true.");
        Intent intent = new Intent(Constants.ACTION_LIST);
        intent.putExtra(DownloadActivity.KEY_FROM, DownloadActivity.KEY_VALUE);        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);

        NotificationUtil.showNoti(mContext, MResource.getIdByName(
        		mContext, "drawable", "nq_download_notinstall"),
                MResource.getString(mContext, "nq_download_notinstall_titile"), 
                MResource.getString(mContext, "nq_download_notinstall_tip"),
                intent,
                NotificationUtil.NOTIF_ID_DOWNLOAD_NOTINSTALL);
    }
}
