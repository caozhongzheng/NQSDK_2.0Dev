package com.nqmobile.livesdk.modules.locker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;

import com.nq.interfaces.launcher.TLockerResource;
import com.nqmobile.livesdk.LauncherSDK;
import com.nqmobile.livesdk.commons.ApplicationContext;
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
import com.nqmobile.livesdk.modules.locker.model.LockerEngine;
import com.nqmobile.livesdk.modules.locker.network.GetLockerDetailEvent;
import com.nqmobile.livesdk.modules.locker.network.GetLockerListEvent;
import com.nqmobile.livesdk.modules.locker.network.GetNewLockerEngineProtocol.GetNewLockerEngineFailedEvent;
import com.nqmobile.livesdk.modules.locker.network.GetNewLockerEngineProtocol.GetNewLockerEngineSuccessEvent;
import com.nqmobile.livesdk.modules.locker.network.LockerServiceFactory;
import com.nqmobile.livesdk.modules.locker.tables.AbsLockerTable;
import com.nqmobile.livesdk.modules.locker.tables.LockerCacheTable;
import com.nqmobile.livesdk.modules.locker.tables.LockerLocalTable;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.FileUtil;
import com.nqmobile.livesdk.utils.MD5;
import com.nqmobile.livesdk.utils.MResource;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.NotificationUtil;
import com.nqmobile.livesdk.utils.PreferenceDataHelper;
import com.nqmobile.livesdk.utils.StringUtil;
import com.nqmobile.livesdk.utils.ToastUtils;

/**
 * 锁屏业务类
 *
 * @author caozhongzheng
 * @time 2014/5/7 14:53:39
 */
public class LockerManager extends AbsManager {
	// ===========================================================
	// Constants
	// ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(LockerModule.MODULE_NAME);
	
	public static final String ENGINE_LIB_LD_FILE_NAME = "LdLockerEngine.apk";
	public static final String ENGINE_LIB_LF_FILE_NAME = "LfLockerEngine.apk";

	// ===========================================================
	// Fields
	// ===========================================================
	// ===========================================================
	// Constructors
	// ===========================================================

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
	public void getLockerList(int column, int offset, int length,
			LockerListListener tag) {
		LockerServiceFactory.getService().getLockerList(column, offset, length,
				tag);
	}

	public void getLockerDetail(String id, LockerDetailListener tag) {
		LockerServiceFactory.getService().getLockerDetail(id, tag);
	}

	public void onEvent(GetLockerListEvent e) {
		if (e.getTag() == null) {
			return;
		}
		if (!(e.getTag() instanceof LockerListListener)) {
			return;
		}
		LockerListStoreListener mListener = (LockerListStoreListener) e.getTag();
		if (e.isSuccess()) {
			List<Locker> lockerList = e.getLockerList();
			if (lockerList != null && lockerList.size() > 0) {
				final int ARR_LEN = 3;
				final int lockerListSize = lockerList.size();

				List<Locker[]> arrLockersList = new ArrayList<Locker[]>(
						lockerList.size() / ARR_LEN + 1);
				Locker[] arrLocker = null;
				for (int i = 0; i < lockerListSize; i++) {
					if (i % 3 == 0)
						arrLocker = new Locker[3];
					arrLocker[i % 3] = lockerList.get(i);
					if (i % 3 == 2)
						arrLockersList.add(arrLocker);
				}
				if (lockerListSize % 3 != 0) {
					arrLockersList.add(arrLocker);
				}
				mListener.onGetLockerListSucc(e.getColumn(), e.getOffset(),
						arrLockersList);
				LockerManager.getInstance(ApplicationContext.getContext())
						.saveLockerCache(e.getColumn(), e.getOffset(),
								arrLockersList);
			} else {
				mListener.onGetLockerListSucc(e.getColumn(), e.getOffset(),
						null);
			}
		} else {
			mListener.onErr();
		}
	}

	public void onEvent(GetLockerDetailEvent e) {
		if (e.getTag() == null) {
			return;
		}
		if (!(e.getTag() instanceof LockerDetailListener)) {
			return;
		}
		LockerDetailListener mListener = (LockerDetailListener) e.getTag();
		if (!e.isSuccess()) {
			mListener.onErr();
			return;
		}
		mListener.onGetDetailSucc(e.getLocker());
	}
	
	public void onEvent(GetNewLockerEngineSuccessEvent event){
		Object tag = event.getTag();
		if (tag instanceof NewLockerEngineListener){
			NewLockerEngineListener listener = (NewLockerEngineListener)tag;
			LockerEngine lockerEngine = event.getLockerEngine();
			if (lockerEngine != null) {
            	prepareUpgrade(lockerEngine);
				listener.onGetNewLockerEngineSucc(lockerEngine);
			}
		}
	}
	
	public void onEvent(GetNewLockerEngineFailedEvent event){
		Object tag = event.getTag();
		if (tag instanceof GetNewLockerEngineFailedEvent){
			NewLockerEngineListener listener = (NewLockerEngineListener)tag;
			listener.onErr();
		}
	}

	// ===========================================================
	// 舊版分割線
	// ===========================================================
	public static final long CACHE_MAX_TIME = 24L * 60 * 60 * 1000;// 24小时
	
    private static final int CONNECT_TIMEOUT = 10*1000;
    private static final int READ_TIMEOUT = 10*1000;
    
	private static LockerManager mInstance;
	private Context mContext;
    private boolean mDownloadingEngineLibLd;
    private boolean mDownloadingEngineLibLf;

	public static final int STATUS_UNKNOWN = -1;// 未知
	public static final int STATUS_NONE = 0;// 未下载
	public static final int STATUS_DOWNLOADING = 1;// 下载中
	public static final int STATUS_PAUSED = 2;// 下载暂停中
	public static final int STATUS_DOWNLOADED = 3;// 已下载
	public static final int STATUS_CURRENT_LOCKER = 4;// 当前锁屏

	public static final String DEFAULT_LOCKER = "default";

	public LockerManager(Context context) {
		super();
		mContext = context;
	}

	public synchronized static LockerManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new LockerManager(context);
		}
		return mInstance;
	}

	

	// 锁屏引擎 手动升级 准备
	public void prepareUpgrade(LockerEngine lockerEngine) {
		if (lockerEngine == null)
			return;

		int type = lockerEngine.getType();
		PreferenceDataHelper mHelper = PreferenceDataHelper
				.getInstance(mContext);
		if (type == LockerEngine.LIB_ENGINE_LD) {
			mHelper.setStringValue(
					PreferenceDataHelper.KEY_ENGINE_LIB_LD_SERVER_VERSION,
					StringUtil.nullToEmpty(lockerEngine.getVersion()));
			mHelper.setStringValue(
					PreferenceDataHelper.KEY_ENGINE_LIB_LD_SERVER_URL,
					StringUtil.nullToEmpty(lockerEngine.getDownUrl()));
			mHelper.setStringValue(
					PreferenceDataHelper.KEY_ENGINE_LIB_LD_SERVER_MD5,
					StringUtil.nullToEmpty(lockerEngine.getMd5()));
			mHelper.setStringValue(
					PreferenceDataHelper.KEY_ENGINE_LIB_LD_SERVER_SIZE,
					lockerEngine.getSize() + "");
			mHelper.setBooleanValue(
					PreferenceDataHelper.KEY_ENGINE_LIB_LD_SERVER_WIFI, false);
		} else if (type == LockerEngine.LIB_ENGINE_LF) {
			mHelper.setStringValue(
					PreferenceDataHelper.KEY_ENGINE_LIB_LF_SERVER_VERSION,
					StringUtil.nullToEmpty(lockerEngine.getVersion()));
			mHelper.setStringValue(
					PreferenceDataHelper.KEY_ENGINE_LIB_LF_SERVER_URL,
					StringUtil.nullToEmpty(lockerEngine.getDownUrl()));
			mHelper.setStringValue(
					PreferenceDataHelper.KEY_ENGINE_LIB_LF_SERVER_MD5,
					StringUtil.nullToEmpty(lockerEngine.getMd5()));
			mHelper.setStringValue(
					PreferenceDataHelper.KEY_ENGINE_LIB_LF_SERVER_SIZE,
					lockerEngine.getSize() + "");
			mHelper.setBooleanValue(
					PreferenceDataHelper.KEY_ENGINE_LIB_LF_SERVER_WIFI, false);
		}
	}

	/**
	 * LockerResource转换成Locker对象
	 *
	 * @param lockerResource
	 * @return
	 */
	public Locker lockerResourceToLocker(TLockerResource lockerResource) {
		Locker locker = null;
		if (lockerResource != null) {
			locker = new Locker();
			locker.setStrId(StringUtil.nullToEmpty(lockerResource.getId()));
			locker.setStrName(StringUtil.nullToEmpty(lockerResource.getName()));
			locker.setStrLockerUrl(StringUtil.nullToEmpty(lockerResource
					.getDownloadUrl()));
			locker.setStrPreviewUrl(StringUtil.nullToEmpty(lockerResource
					.getPreview()));
			locker.setStrIconUrl(StringUtil.nullToEmpty(lockerResource
					.getIcon()));
			locker.setStrDailyIconUrl(StringUtil.nullToEmpty(lockerResource
					.getDailyIcon()));
			locker.setLongSize(lockerResource.getSize());
			locker.setLongDownloadCount(lockerResource.getDownloadNum());
			locker.setStrAuthor(StringUtil.nullToEmpty(lockerResource
					.getAuthor()));
			locker.setIntMinVersion(lockerResource.getMinVersion());
			locker.setIntEngineType(lockerResource.getEngType());

			String SDCardPath = CommonMethod.getSDcardPath(mContext);
			if (SDCardPath == null)
				SDCardPath = CommonMethod.getSDcardPathFromPref(mContext);
			// icon本地路径
			if (!TextUtils.isEmpty(locker.getStrIconUrl())) {
				String strImgPath = new StringBuilder().append(SDCardPath)
						.append(LockerConstants.STORE_IMAGE_LOCAL_PATH_LOCKER)
						.toString();
				locker.setStrIconPath(new StringBuilder()
						.append(strImgPath)
						.append(locker.getStrId())
						.append("_icon")
						.append(locker.getStrIconUrl().substring(
								locker.getStrIconUrl().lastIndexOf(".")))
						.toString());
			}
			// locker本地路径
			if (!TextUtils.isEmpty(locker.getStrLockerUrl())) {
				String strPath = new StringBuilder().append(SDCardPath)
						.append(LockerConstants.STORE_IMAGE_LOCAL_PATH_LOCKER)
						.toString();
				locker.setStrLockerPath(new StringBuilder()
						.append(strPath)
						.append(locker.getStrId())
						.append(locker.getStrLockerUrl().substring(
								locker.getStrLockerUrl().lastIndexOf(".")))
						.toString());
			}
			// 预览图缓存、本地路径
			if (!TextUtils.isEmpty(locker.getStrPreviewUrl())) {
				String strPath = new StringBuilder().append(SDCardPath)
						.append(LockerConstants.STORE_IMAGE_LOCAL_PATH_LOCKER)
						.toString();
				locker.setStrPreviewPath(new StringBuilder()
						.append(strPath)
						.append(locker.getStrId())
						.append("_preview")
						.append(locker.getStrPreviewUrl().substring(
								locker.getStrPreviewUrl().lastIndexOf(".")))
						.toString());
			}
		}
		return locker;
	}

	/**
	 * 将cursor对象转换Locker
	 *
	 * @param cursor
	 * @return
	 */
	public Locker cursorToLocker(Cursor cursor) {
		Locker locker = new Locker();
		locker.setStrId(StringUtil.nullToEmpty(cursor.getString(cursor
				.getColumnIndex(AbsLockerTable.LOCKER_ID))));
		locker.setIntSourceType(cursor.getInt(cursor
				.getColumnIndex(AbsLockerTable.LOCKER_SOURCE_TYPE)));
		locker.setStrName(StringUtil.nullToEmpty(cursor.getString(cursor
				.getColumnIndex(AbsLockerTable.LOCKER_NAME))));
		locker.setStrLockerUrl(StringUtil.nullToEmpty(cursor.getString(cursor
				.getColumnIndex(AbsLockerTable.LOCKER_URL))));
		locker.setStrPreviewUrl(StringUtil.nullToEmpty(cursor.getString(cursor
				.getColumnIndex(AbsLockerTable.LOCKER_PREVIEW_URL))));
		locker.setStrIconUrl(StringUtil.nullToEmpty(cursor.getString(cursor
				.getColumnIndex(AbsLockerTable.LOCKER_ICON_URL))));
		locker.setStrDailyIconUrl(StringUtil.nullToEmpty(cursor
				.getString(cursor.getColumnIndex(AbsLockerTable.LOCKER_DAILYICON))));
		locker.setLongSize(cursor.getLong(cursor
				.getColumnIndex(AbsLockerTable.LOCKER_SIZE)));
		locker.setLongDownloadCount(cursor.getLong(cursor
				.getColumnIndex(AbsLockerTable.LOCKER_DOWNLOAD_COUNT)));
		locker.setStrAuthor(StringUtil.nullToEmpty(cursor.getString(cursor
				.getColumnIndex(AbsLockerTable.LOCKER_AUTHOR))));
		locker.setIntMinVersion(cursor.getInt(cursor
				.getColumnIndex(AbsLockerTable.LOCKER_VERSION)));
		locker.setIntEngineType(cursor.getInt(cursor
				.getColumnIndex(AbsLockerTable.LOCKER_TYPE)));
		locker.setStrIconPath(StringUtil.nullToEmpty(cursor.getString(cursor
				.getColumnIndex(AbsLockerTable.LOCKER_ICON_PATH))));
		locker.setStrPreviewPath(StringUtil.nullToEmpty(cursor.getString(cursor
				.getColumnIndex(AbsLockerTable.LOCKER_PREVIEW_PATH))));
		locker.setStrLockerPath(StringUtil.nullToEmpty(cursor.getString(cursor
				.getColumnIndex(AbsLockerTable.LOCKER_PATH))));
		locker.setLongUpdateTime(cursor.getLong(cursor
				.getColumnIndex(AbsLockerTable.LOCKER_UPDATETIME)));
		locker.setLongLocalTime(cursor.getLong(cursor
				.getColumnIndex(AbsLockerTable.LOCKER_LOCALTIME)));
		return locker;
	}

	/**
	 * 获取锁屏引擎最新版本
	 *
	 * @param type
	 * @param Version
	 * @param LockerListListener
	 */
	public void getNewLockerEngine(final int type, final int version,
			final NewLockerEngineListener listener) {
		if (listener == null)
			return;

		// 没有网络
		if (!NetworkUtils.isConnected(mContext)) {
			listener.onNoNetwork();
			return;
		}
		
		LockerServiceFactory.getService().getNewLockerEngine(type, version, listener);
	}

	public void getLockerList(int column, int offset, LockerListStoreListener tag) {
		LockerServiceFactory.getService().getLockerList(column, offset,
				LockerConstants.STORE_LOCKER_LIST_PAGE_SIZE, tag);
	}

	/*    *//**
	 * 获取锁屏列表
	 *
	 * @param column
	 * @param offset
	 * @param LockerListListener
	 */
	/*
	 * public void getLockerList(final int column, final int offset, final
	 * LockerListListener listener) { if(listener == null) return;
	 * 
	 * // 没有网络 if (!NetworkUtils.isConnected(context)) { listener.onNoNetwork();
	 * return; }
	 * 
	 * // 没有缓存，或缓存过期，联网查询数据。 Utility.getInstance(context).getLockerList(column,
	 * offset, new LockerListListener() {
	 * 
	 * @Override public void onErr() {
	 * NqLog.i("LockerManager.getLockerList.onErr"); listener.onErr(); }
	 * 
	 * @Override public void onNoNetwork() { // TODO Auto-generated method stub
	 * NqLog.i("LockerManager.getLockerList.onNoNetwork");
	 * listener.onNoNetwork(); }
	 * 
	 * @Override public void onGetLockerListSucc(int column, int offset,
	 * List<Locker[]> lockers) { // TODO Auto-generated method stub
	 * NqLog.i("LockerManager.getLockerList.onGetLockerListSucc");
	 * listener.onGetLockerListSucc(column, offset, lockers); } }); }
	 */

	/**
	 * 返回本地的锁屏列表。
	 *
	 * @return
	 */
	public List<Locker> getLockerListFromLocal() {
		List<Locker> listLockers = null;
		ContentResolver contentResolver = mContext.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = contentResolver.query(LockerLocalTable.LOCAL_LOCKER_URI, null,
					null, null, AbsLockerTable._ID + " asc");
			if (cursor != null && cursor.getCount() > 0) {
				listLockers = new ArrayList<Locker>();
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
						.moveToNext()) {
					Locker locker = cursorToLocker(cursor);
					LockerStatus status = getStatus(locker);
					if (status.statusCode == STATUS_DOWNLOADED
							|| status.statusCode == STATUS_CURRENT_LOCKER) {
						listLockers.add(cursorToLocker(cursor));
					}
				}
			}
		} catch (Exception e) {
			NqLog.e("getLockerListFromLocal " + e.toString());
		} finally {
			try {
				cursor.close();
			} catch (Exception e2) {
			}
		}
		return listLockers;
	}

	/**
	 * 返回缓存的锁屏列表。
	 *
	 * @param column
	 *            0：新品；1：热门。
	 * @return
	 */
	public List<Locker[]> getArrLockerListFromCache(int column) {
		List<Locker[]> listLockers = null;
		ContentResolver contentResolver = mContext.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = contentResolver.query(LockerCacheTable.LOCKER_CACHE_URI, null,
					AbsLockerTable.LOCKER_SOURCE_TYPE + "="
							+ AppConstant.STORE_MODULE_TYPE_MAIN + " AND "
							+ AbsLockerTable.LOCKER_COLUMN + "=?",
					new String[] { String.valueOf(column) }, AbsLockerTable._ID
							+ " asc");
			if (cursor != null && cursor.getCount() > 0) {
				listLockers = new ArrayList<Locker[]>();
				int index = 0;
				Locker[] lockers = null;
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
						.moveToNext()) {
					if (index == 0) {
						lockers = new Locker[3];
					}
					lockers[index] = cursorToLocker(cursor);
					if (index == 2) {
						listLockers.add(lockers);
						index = 0;
					} else {
						index++;
					}
				}
				if (cursor.getCount() % 3 != 0) {
					listLockers.add(lockers);
				}
			}
		} catch (Exception e) {
			NqLog.e("getArrLockerListFromCache " + e.toString());
		} finally {
			try {
				cursor.close();
			} catch (Exception e2) {
			}
		}
		return listLockers;
	}

	/**
	 * 返回本地的锁屏列表。
	 *
	 * @return
	 */
	public List<Locker> getArrLockerListFromLocal() {
		List<Locker> listLockers = null;
		ContentResolver contentResolver = mContext.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = contentResolver.query(LockerLocalTable.LOCAL_LOCKER_URI, null,
					null, null, AbsLockerTable._ID + " asc");
			if (cursor != null && cursor.getCount() > 0) {
				listLockers = new ArrayList<Locker>();
				Locker locker = null;
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
						.moveToNext()) {
					locker = cursorToLocker(cursor);
					listLockers.add(locker);
				}
			}
		} catch (Exception e) {
			NqLog.e("getLockerListFromLocal " + e.toString());
		} finally {
			try {
				cursor.close();
			} catch (Exception e2) {
			}
		}
		return listLockers;
	}

	/*   *//**
	 * 根据id 获取Locker 详情。
	 *
	 * @param id
	 * @param listener
	 */
	/*
	 * public void getLockerDetail(String id, final LockerDetailListener
	 * listener) { Utility.getInstance(context).getLockerDetail(id, "zh_cn", new
	 * LockerDetailListener() {
	 * 
	 * @Override public void onErr() { listener.onErr(); }
	 * 
	 * @Override public void onGetDetailSucc(Locker locker) {
	 * listener.onGetDetailSucc(locker);
	 * 
	 * } }); }
	 */

	/**
	 * 根据id 从本地库获取Locker详情。
	 *
	 * @param id
	 */
	public Locker getLockerDetailFromLocal(String id) {
		Locker locker = null;
		ContentResolver contentResolver = mContext.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = contentResolver.query(LockerLocalTable.LOCAL_LOCKER_URI, null,
					AbsLockerTable.LOCKER_ID + "=?", new String[] { id },
					AbsLockerTable._ID + " asc");
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToNext();
				locker = cursorToLocker(cursor);
			}
		} catch (Exception e) {
			NqLog.e("getLockerDetailFromLocal " + e.toString());
		} finally {
			try {
				cursor.close();
			} catch (Exception e2) {
			}
		}
		return locker;
	}

	/**
	 * 检测是否有未过期的锁屏缓存数据
	 *
	 * @param column
	 *            0：新品；1：热门；
	 * @return
	 */
	public boolean haveAvailableLockerListCashe(int column) {
		Long cacheTime = 0l;
		cacheTime = PreferenceDataHelper.getInstance(mContext).getLongValue(
				PreferenceDataHelper.KEY_LOCKER_LIST_CACHE_TIME[column]);
		Long currentTime = new Date().getTime();
		if (currentTime - cacheTime < CACHE_MAX_TIME) {// 没有过期
			Cursor cursor = null;
			try {
				ContentResolver contentResolver = mContext.getContentResolver();
				cursor = contentResolver.query(LockerCacheTable.LOCKER_CACHE_URI,
						new String[] { AbsLockerTable.LOCKER_ID },
						AbsLockerTable.LOCKER_COLUMN + "=" + column, null, null);
				if (cursor != null && cursor.getCount() > 0) {
					return true;// 有缓存里，且未过期。
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					cursor.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	/**
	 * 检测是否有锁屏缓存数据
	 *
	 * @param column
	 *            0：新品；1：热门；
	 * @return
	 */
	public boolean hadAvailableLockerListCashe(int column) {
		Cursor cursor = null;
		try {
			ContentResolver contentResolver = mContext.getContentResolver();
			cursor = contentResolver.query(LockerCacheTable.LOCKER_CACHE_URI,
					new String[] { AbsLockerTable.LOCKER_ID },
					AbsLockerTable.LOCKER_COLUMN + "=" + column, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				cursor.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 锁屏是否已下载到本地。包括local数据库、和锁屏zip文件。
	 *
	 * @param locker
	 * @return
	 */
	public boolean isLockerInLocal(Locker locker) {
		if (locker != null && !TextUtils.isEmpty(locker.getStrId())) {
			Cursor cursor = null;
			try {
				ContentResolver contentResolver = mContext.getContentResolver();
				cursor = contentResolver.query(LockerLocalTable.LOCAL_LOCKER_URI,
						new String[] { AbsLockerTable.LOCKER_ID },
						AbsLockerTable.LOCKER_ID + " like '" + locker.getStrId()
								+ "'", null, null);
				if (cursor != null && cursor.getCount() > 0) {
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					try {
						cursor.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return false;
	}

	/**
	 * 缓存锁屏数据
	 *
	 * @param column
	 *            0：新品；1：热门；
	 * @param offset
	 * @param arrLockers
	 * @return
	 */
	public boolean saveLockerCache(int column, int offset,
			List<Locker[]> arrLockers) {
		try {
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			Builder b = null;

			if (offset == 0) {
				// 清除缓存
				b = ContentProviderOperation.newDelete(
				        LockerCacheTable.LOCKER_CACHE_URI).withSelection(
						AbsLockerTable.LOCKER_SOURCE_TYPE + "="
								+ AppConstant.STORE_MODULE_TYPE_MAIN + " AND "
								+ AbsLockerTable.LOCKER_COLUMN + "=" + column,
						null);
				ops.add(b.build());
			}
			ContentResolver contentResolver = mContext.getContentResolver();

			if (arrLockers != null && arrLockers.size() > 0) {
				Locker[] lockers = null;
				long localTime = new Date().getTime();
				for (int i = 0; i < arrLockers.size(); i++) {
					lockers = arrLockers.get(i);
					for (int j = 0; j < lockers.length; j++) {
						if (lockers[j] != null) {
							lockers[j].setLongLocalTime(localTime);
							ContentValues values = LockerConverter.toContentValues(
									column, lockers[j]);
							b = ContentProviderOperation.newInsert(
							        LockerCacheTable.LOCKER_CACHE_URI).withValues(
									values);
							ops.add(b.build());
						}
					}
				}
			}

			contentResolver.applyBatch(AbsLockerTable.DATA_AUTHORITY, ops);

			// 更新缓存时间。
			PreferenceDataHelper.getInstance(mContext).setLongValue(
					PreferenceDataHelper.KEY_LOCKER_LIST_CACHE_TIME[column],
					new Date().getTime());
			return true;
		} catch (Exception e) {
			NqLog.e("saveLockerCache error " + e.toString());
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * 锁屏数据存入本地库
	 *
	 * @param dataType
	 *            0：新品；1：热门；
	 * @param lockers
	 * @return
	 */
	public boolean saveLockerListLocal(int dataType, List<Locker[]> lockers) {
		if (lockers != null && lockers.size() > 0) {
			ContentResolver contentResolver = mContext.getContentResolver();
			for (Locker[] locker : lockers) {
				if (locker != null) {
					for (int i = 0; i < locker.length; i++) {
						if (locker[i] != null) {
							locker[i].setLongLocalTime(new Date().getTime());
							ContentValues values = LockerConverter.toContentValues(
									dataType, locker[i]);
							contentResolver.insert(
							        LockerLocalTable.LOCAL_LOCKER_URI, values);
						}
					}
				}
			}
		}
		return true;
	}

	private boolean saveImageToFile(final String srcUrl,
			final String destFilePath) {
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
					FileUtil.writeBmpToFile(drawable.getBitmap(), new File(
							destFilePath));
				}
			}
		});

		return true;
	}

	/**
	 * 保存icon、预览图到本地文件夹
	 *
	 * @param locker
	 * @return
	 */
	private boolean saveImages(Locker locker) {
		boolean result = false;
		if (locker != null) {
			boolean r1 = saveImageToFile(locker.getStrIconUrl(),
					locker.getStrIconPath());
			boolean r2 = saveImageToFile(locker.getStrPreviewUrl(),
					locker.getStrPreviewPath());
			result = r1 && r2;
		}
		return result;
	}

	/**
	 * 锁屏数据存入本地库
	 *
	 * @param locker
	 * @return
	 */
	private void saveLocker(final Locker locker) {
		(new Thread() {
			@Override
			public void run() {
				try {
					ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
					Builder deleteOp = ContentProviderOperation.newDelete(
					        LockerLocalTable.LOCAL_LOCKER_URI).withSelection(
							AbsLockerTable.LOCKER_ID + " = ?",
							new String[] { locker.getStrId() });
					ops.add(deleteOp.build());

					locker.setLongLocalTime(System.currentTimeMillis());
					ContentValues values = LockerConverter.toContentValues(-1, locker);
					Builder insertOp = ContentProviderOperation.newInsert(
					        LockerLocalTable.LOCAL_LOCKER_URI).withValues(values);
					ops.add(insertOp.build());

					mContext.getContentResolver().applyBatch(
							AbsLockerTable.DATA_AUTHORITY, ops);

					saveImages(locker);
				} catch (Exception e) {
					NqLog.e(e);
				}
			}
		}).start();
	}

	/**
	 * 获取默认的锁屏图标
	 *
	 * @param context
	 * @return
	 */
	public Bitmap getDefaultIcon(Context context) {
		return BitmapFactory.decodeResource(context.getResources(),
				MResource.getIdByName(context, "drawable", "nq_load_default"));
	}

	/**
	 * 根据id 从缓存获取Locker详情。
	 *
	 * @param lockerId
	 * @return Locker对象或null。null表示没有找到。
	 */
	public Locker getLockerDetailFromCache(String lockerId) {
		Locker locker = null;
		ContentResolver contentResolver = mContext.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = contentResolver.query(LockerCacheTable.LOCKER_CACHE_URI, null,
					AbsLockerTable.LOCKER_ID + "=?", new String[] { lockerId },
					AbsLockerTable._ID + " desc");
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToNext();
				locker = cursorToLocker(cursor);
			}
		} catch (Exception e) {
			NqLog.e(e);
		} finally {
			try {
				if (cursor != null) {
					cursor.close();
				}
			} catch (Exception e) {
				NqLog.e(e);
			}
		}
		return locker;
	}

	public static int getCount(List<Locker[]> lockers) {
		if (null == lockers)
			return 0;
		int count = 0;
		for (int i = 0; i < lockers.size(); i++) {
			for (int j = 0; j < lockers.get(i).length; j++) {
				if (null != lockers.get(i)[j]
						&& !TextUtils.isEmpty(lockers.get(i)[j].getStrId()))
					count++;
			}

		}
		NqLog.d("locker count=" + count);
		return count;
	}

	/**
	 * 删除 本地锁屏
	 * 
	 * @return
	 */
	public boolean deleteLocalLoker(Locker locker) {
		if (locker == null)
			return false;
		String id = locker.getStrId();
		mContext.getContentResolver().delete(DownloadTable.TABLE_URI,
				DownloadTable.DOWNLOAD_RES_ID + " = ?", new String[] { id });
		mContext.getContentResolver().delete(LockerLocalTable.LOCAL_LOCKER_URI,
				AbsLockerTable.LOCKER_ID + " = ?", new String[] { id });
		deleteLokerInDB(locker);
		deleteLokerFile(locker);
		onLockerDelete(locker);
		if (id.equalsIgnoreCase(getCurrentLockerID()))
			setCurrentLockerID("");

		return true;
	}

	/** 删除 本地锁屏文件 */
	public void deleteLokerFile(Locker locker) {
		if (locker == null)
			return;
		FileUtil.delFile(locker.getStrIconPath());
		FileUtil.delFile(locker.getStrPreviewPath());
		FileUtil.delFile(locker.getStrLockerPath());
	}

	/** 删除 本地库中锁屏 */
	public void deleteLokerInDB(Locker locker) {
		if (locker == null)
			return;
		try {
			ContentResolver contentResolver = mContext.getContentResolver();
			contentResolver.delete(LockerLocalTable.LOCAL_LOCKER_URI,
					AbsLockerTable.LOCKER_ID + "='" + locker.getStrId()
							+ "' AND " + AbsLockerTable.LOCKER_SOURCE_TYPE + "='"
							+ locker.getIntSourceType() + "'", null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 删除 本地库中锁屏list */
	public int deleteLockerInDB(Context context, List<Locker> delLockerList2) {
		int delete = 0;
		if (delLockerList2 == null || delLockerList2.isEmpty())
			return delete;

		try {
			ContentResolver contentResolver = context.getContentResolver();
			int size = delLockerList2.size();
			for (int i = 0; i < size; i++) {
				contentResolver.delete(LockerLocalTable.LOCAL_LOCKER_URI,
						AbsLockerTable.LOCKER_ID + "='"
								+ delLockerList2.get(i).getStrId() + "'", null);
				delete++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return delete;
	}

	/** 删除 缓存 库中锁屏list */
	public int deleteLockerCacheInDB(Context context,
			List<Locker> delLockerList2) {
		int delete = 0;
		if (delLockerList2 == null || delLockerList2.isEmpty())
			return delete;

		try {
			ContentResolver contentResolver = context.getContentResolver();
			int size = delLockerList2.size();
			for (int i = 0; i < size; i++) {
				contentResolver.delete(LockerCacheTable.LOCKER_CACHE_URI,
						AbsLockerTable.LOCKER_ID + "='"
								+ delLockerList2.get(i).getStrId() + "'", null);
				delete++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return delete;
	}

	private boolean isDefaultLocker(Locker locker) {
		return locker != null
				&& DEFAULT_LOCKER.equals(locker.getStrLockerPath());
	}

	private boolean isCurrentLocker(Locker locker) {
		String currentLockerId = getCurrentLockerID();
		if ("".equals(currentLockerId)) {
			return isDefaultLocker(locker);// 默认锁屏
		}

		return currentLockerId.equals(locker.getStrId());
	}

	public static class LockerStatus {
		public int statusCode;
		public long downloadedBytes;
		public long totalBytes;
	}

	/**
	 * 返回Locker的下载、安装状态
	 *
	 * @param app
	 * @return 总是返回非null
	 */
	public LockerStatus getStatus(Locker locker) {
		LockerStatus result = new LockerStatus();
		result.statusCode = STATUS_UNKNOWN;

		if (locker == null) {
			return result;
		}

		if (isCurrentLocker(locker)) {
			result.statusCode = STATUS_CURRENT_LOCKER;
		} else {
			MyDownloadManager downloader = MyDownloadManager
					.getInstance(mContext);
			Long downloadId = downloader
					.getDownloadId(locker.getStrLockerUrl());
			int[] bytesAndStatus = downloader.getBytesAndStatus(downloadId);
			if (bytesAndStatus[0] == 1) {
				result.statusCode = convertStatus(bytesAndStatus[1]);
				if (result.statusCode == STATUS_DOWNLOADED) {
					String filePath = locker.getStrLockerPath();
					if (!TextUtils.isEmpty(filePath)) {
						File file = new File(filePath);
						if (!file.exists()) {
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
	 * 应用锁屏
	 *
	 * @param locker
	 */
	public boolean applyLocker(Locker locker) {
		boolean result = false;

		if (locker == null || TextUtils.isEmpty(locker.getStrLockerPath())) {
			return result;
		}

		Intent i = new Intent();
		i.setAction(LiveReceiver.ACTION_APPLY_LOCKER);
		i.putExtra("locker", locker);
		mContext.sendBroadcast(i);
		return result;
	}

	/**
	 * 体验锁屏
	 *
	 * @param locker
	 */
	public boolean previewLocker(Locker locker) {
		boolean result = false;

		if (locker == null || TextUtils.isEmpty(locker.getStrLockerPath())) {
			return result;
		}

		Intent i = new Intent();
		i.setAction(LiveReceiver.ACTION_PREVIEW_LOCKER);
		i.putExtra("locker", locker);
		mContext.sendBroadcast(i);
		return result;
	}

	/** 检查锁屏功能是否开启 */
	private boolean isLockerEnabled() {
		return true;
	}

	/** 检查锁屏引擎版本能否处理该锁屏资源 */
	public boolean checkEngineVersion(Locker locker) {
		boolean result = false;
		// 根据locker.getIntEngineType()
		// 调用锁屏SDK接口,得到这个类型的本地引擎版本,然后和locker.getIntMinVersion()做比较
		LockerEngine le = LauncherSDK.getInstance(mContext).getLockerEngine(
				locker.getIntEngineType());
		if (le != null && locker.getIntMinVersion() <= le.getVersion())
			result = true;
		return result;
	}

	/**
	 * 下载Locker
	 *
	 * @param locker
	 *            Locker对象
	 */
	public Long downloadLocker(Locker locker) {
		Long result = null;

		if (locker == null || TextUtils.isEmpty(locker.getStrLockerUrl())
				|| DEFAULT_LOCKER.equals(locker.getStrLockerPath())) {
			return result;
		}

		if (!NetworkUtils.isConnected(mContext)) {
			ToastUtils.toast(mContext, "nq_nonetwork");
			return result;
		}

		try {
			Long downloadId = MyDownloadManager.getInstance(mContext)
					.downloadLocker(locker);
			if (downloadId != null) {
				saveLocker(locker);
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
	 * @param locker
	 * @param observer
	 */
	public void registerDownloadObserver(Locker locker,
			IDownloadObserver observer) {
		if (locker == null || observer == null) {
			return;
		}

		MyDownloadManager downloader = MyDownloadManager.getInstance(mContext);
		Long downloadId = downloader.getDownloadId(locker.getStrLockerUrl());
		downloader.registerDownloadObserver(downloadId, observer);
	}

	/**
	 * 取消下载进度监听
	 *
	 * @param locker
	 */
	public void unregisterDownloadObserver(Locker locker) {
		if (locker == null) {
			return;
		}

		MyDownloadManager downloader = MyDownloadManager.getInstance(mContext);
		Long downloadId = downloader.getDownloadId(locker.getStrLockerUrl());
		downloader.unregisterDownloadObserver(downloadId);
	}

	/**
	 * 返回桌面的默认，和郎趣团队商定默认锁屏的LockerPath=default
	 *
	 * @return Locker对象
	 */
	public Locker getDefaultLocker() {
		Locker result = new Locker();
		result.setStrLockerPath(DEFAULT_LOCKER);
		return result;
	}

	/**
	 * 获取当前锁屏ID
	 *
	 * @return
	 */
	public String getCurrentLockerID() {
		String id = PreferenceDataHelper.getInstance(mContext).getStringValue(
				PreferenceDataHelper.KEY_CURRENT_LOCKER);
		return id;
	}

	/**
	 * 设置当前锁屏ID
	 *
	 * @return
	 */
	public void setCurrentLockerID(String lockerId) {
		PreferenceDataHelper.getInstance(mContext).setStringValue(
				PreferenceDataHelper.KEY_CURRENT_LOCKER, lockerId);
	}

	private Locker getSavedLocker(String lockerId) {
		Locker result = null;
		Cursor cursor = null;
		try {
			ContentResolver contentResolver = mContext.getContentResolver();
			cursor = contentResolver.query(LockerLocalTable.LOCAL_LOCKER_URI, null,
					AbsLockerTable.LOCKER_ID + " = ?", new String[] { lockerId },
					AbsLockerTable._ID + " DESC");
			if (cursor != null && cursor.moveToFirst()) {
				result = cursorToLocker(cursor);
			}
		} catch (Exception e) {
			NqLog.e(e);
		} finally {
			try {
				cursor.close();
			} catch (Exception e) {
				NqLog.e(e);
			}
		}

		return result;
	}

	public void OnDownloadComplete(String lockerId) {
		if (TextUtils.isEmpty(lockerId)) {
			return;
		}

		Locker locker = getSavedLocker(lockerId);
		if (locker == null) {
			return;
		}

		onLockerDown(locker);
	}

	/**
	 * 通知launcher刷新本地锁屏列表
	 * 
	 * @return
	 */
	private void onLockerDown(Locker locker) {
		Intent i = new Intent();
		i.setAction(LiveReceiver.ACTION_LOCKER_DOWNLOAD);
		i.putExtra("locker", locker);
		mContext.sendBroadcast(i);
	}

	/**
	 * 通知launcher刷新本地锁屏列表
	 * 
	 * @return
	 */
	private void onLockerDelete(Locker locker) {
		Intent i = new Intent();
		i.setAction(LiveReceiver.ACTION_LOCKER_DELETED);
		i.putExtra("locker", locker);
		mContext.sendBroadcast(i);
	}

	public boolean isCacheExpired(int column) {
		Long cacheTime = 0l;
		cacheTime = PreferenceDataHelper.getInstance(mContext).getLongValue(
				PreferenceDataHelper.KEY_LOCKER_LIST_CACHE_TIME[column]);
		Long currentTime = new Date().getTime();
		return currentTime - cacheTime > CACHE_MAX_TIME;
	}
	
	public void onEvent(DownloadCompleteEvent event) {
		processStoreDownload(event.mRefer);
	}
	
	private void processStoreDownload(long refer){
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = mContext.getContentResolver();
            cursor = contentResolver.query(DownloadTable.TABLE_URI, null,
                    DownloadTable.DOWNLOAD_DOWNLOAD_ID + " = " + refer, null,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                int type = cursor.getInt(cursor.getColumnIndex(DownloadTable.DOWNLOAD_TYPE));
                String resId = cursor.getString(cursor.getColumnIndex(DownloadTable.DOWNLOAD_RES_ID));
                String destPath = cursor.getString(cursor.getColumnIndex(DownloadTable.DOWNLOAD_DEST_PATH));
                if (FileUtil.isFileExists(destPath) && !TextUtils.isEmpty(resId)){//确保文件存在
                    switch(type) {
	                    case MyDownloadManager.DOWNLOAD_TYPE_LOCKER:
	                        LockerManager.getInstance(mContext).OnDownloadComplete(resId);
	                        NotificationUtil.showLockerNotifi(mContext,resId);
	                        StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
	                        		AppActionConstants.ACTION_LOG_1502, resId, StatManager.ACTION_DOWNLOAD,
	                                "3");
	                        break;
                    }
                }
            }
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
	
    /** 升级游戏应用预制库,锁屏引擎库等 */
    public boolean upgradeLibFile(int lockerEngineType) {
		// TODO Auto-generated method stub
    	PreferenceDataHelper mHelper = PreferenceDataHelper.getInstance(mContext);
    	
    	if(!needUpgrade(lockerEngineType)){
    		NqLog.e("upgradeLibFile NO latest engine lib ,lockerEngineType=" + lockerEngineType);
    		return false;
    	}
    	if(!downloadFile(lockerEngineType)){
    		NqLog.e("upgradeLibFile download failed");
    		return false;
    	}
    	/*通知桌面动态加载引擎库apk
    	}*/
    	if(LockerEngine.LIB_ENGINE_LD == lockerEngineType){
    		mHelper.setStringValue(PreferenceDataHelper.KEY_ENGINE_LIB_LD_VERSION, mHelper.getStringValue(PreferenceDataHelper.KEY_ENGINE_LIB_LD_SERVER_VERSION));
    		LockerEngine le = new LockerEngine();
    		le.setType(LockerEngine.LIB_ENGINE_LD);
    		le.setVersion(Integer.parseInt(mHelper.getStringValue(PreferenceDataHelper.KEY_ENGINE_LIB_LD_SERVER_VERSION)));
    		le.setPath(getLibFilePath(LockerEngine.LIB_ENGINE_LD));
    		LauncherSDK.getInstance(mContext).upgLocker(le);
    	} else if(LockerEngine.LIB_ENGINE_LF == lockerEngineType){
    		mHelper.setStringValue(PreferenceDataHelper.KEY_ENGINE_LIB_LF_VERSION, mHelper.getStringValue(PreferenceDataHelper.KEY_ENGINE_LIB_LF_SERVER_VERSION));
    		LockerEngine le = new LockerEngine();
    		le.setType(LockerEngine.LIB_ENGINE_LF);
    		le.setVersion(Integer.parseInt(mHelper.getStringValue(PreferenceDataHelper.KEY_ENGINE_LIB_LF_SERVER_VERSION)));
    		le.setPath(getLibFilePath(LockerEngine.LIB_ENGINE_LF));
    		LauncherSDK.getInstance(mContext).upgLocker(le);
    	}
    	
    	return true;
	}
    
    private boolean needUpgrade(int lockerEngineType) {
		// TODO Auto-generated method stub
    	PreferenceDataHelper mHelper = PreferenceDataHelper.getInstance(mContext);
    	String serVer = null;
    	String localVer = null;
    	if(LockerEngine.LIB_ENGINE_LD == lockerEngineType){
    		serVer = mHelper.getStringValue(PreferenceDataHelper.KEY_ENGINE_LIB_LD_SERVER_VERSION);
    		localVer = mHelper.getStringValue(PreferenceDataHelper.KEY_ENGINE_LIB_LD_VERSION);
    		NqLog.i("LIB ENGINE LD local ver is: " + localVer + " ,serVer is: " + serVer);
    	} else if(LockerEngine.LIB_ENGINE_LF == lockerEngineType){
    		serVer = mHelper.getStringValue(PreferenceDataHelper.KEY_ENGINE_LIB_LF_SERVER_VERSION);
    		localVer = mHelper.getStringValue(PreferenceDataHelper.KEY_ENGINE_LIB_LF_VERSION);
    		NqLog.i("LIB ENGINE LF local ver is: " + localVer + " ,serVer is: " + serVer);
    	}
    	
    	if(localVer == null || localVer.isEmpty())
    		return true;
    	if(serVer == null || serVer.isEmpty())
    		return false;
    	int lDate = Integer.parseInt(localVer.substring(0, Math.min(localVer.length(), 8)));
    	int sDate = Integer.parseInt(serVer.substring(0, Math.min(serVer.length(), 8)));
    	int lNum = 0;
    	if(localVer.length() > 8)
    		lNum = Integer.parseInt(localVer.substring(8));
    	int sNum = 0;
    	if(serVer.length() > 8)
    		sNum = Integer.parseInt(serVer.substring(8));
    	if(lDate > sDate)
    		return false;
    	else if(lDate < sDate)
    		return true;
    	if(lNum < sNum)
    		return true;
    	
    	return false;
	}

	public String getLibFilePath(int type) {
    	PreferenceDataHelper helper = PreferenceDataHelper.getInstance(mContext);
    	String updateName = null;
    	if(type == LockerEngine.LIB_ENGINE_LD)
    		updateName = "LdLockerEngine.apk";//helper.getStringValue(PreferenceDataHelper.KEY_ENGINE_LIB_LD_FILE_NAME);// LdLockerEngine.apk
    	else if(type == LockerEngine.LIB_ENGINE_LF)
    		updateName = "LfLockerEngine.apk";//helper.getStringValue(PreferenceDataHelper.KEY_ENGINE_LIB_LF_FILE_NAME);// LfLockerEngine.apk

        String strLibParentPath = mContext.getFilesDir().getAbsolutePath()+"/";
        String strLibPath = new StringBuilder().append(strLibParentPath).append(updateName).toString();
        
        return strLibPath;
    }
	
    /**
     * @param type 游戏应用库更新, 引擎库更新
     * */
    private boolean downloadFile(int type) {
    	NqLog.i("downloadFile：" + type);
    	boolean result = false;
    	String url = null;
    	String md5 = null;
    	PreferenceDataHelper helper = PreferenceDataHelper.getInstance(mContext);

    	switch (type) {
		case LockerEngine.LIB_ENGINE_LD:
			if(helper.getBooleanValue(PreferenceDataHelper.KEY_ENGINE_LIB_LD_SERVER_WIFI)
					&& !NetworkUtils.isWifi(mContext)){
				NqLog.i("download LD EngineLib can't start cause no wifi");
	    		return false;
			} else if(mDownloadingEngineLibLd) {
	    		NqLog.i("download LD EngineLib in progress,no need to start twice");
	    		return false;
	    	}
			url = helper.getStringValue(PreferenceDataHelper.KEY_ENGINE_LIB_LD_SERVER_URL);
			md5 = helper.getStringValue(PreferenceDataHelper.KEY_ENGINE_LIB_LD_SERVER_MD5);
			break;

		case LockerEngine.LIB_ENGINE_LF:
			if(helper.getBooleanValue(PreferenceDataHelper.KEY_ENGINE_LIB_LF_SERVER_WIFI)
					&& !NetworkUtils.isWifi(mContext)){
				NqLog.i("download LF EngineLib can't start cause no wifi");
	    		return false;
			} else if(mDownloadingEngineLibLf) {
	    		NqLog.i("download LF EngineLib in progress,no need to start twice");
	    		return false;
	    	}
			url = helper.getStringValue(PreferenceDataHelper.KEY_ENGINE_LIB_LF_SERVER_URL);
			md5 = helper.getStringValue(PreferenceDataHelper.KEY_ENGINE_LIB_LF_SERVER_MD5);
			break;

		default:
			break;
		}
    	
    	if (TextUtils.isEmpty(url) || TextUtils.isEmpty(md5)){
    		return false;
    	}
    	
    	NqLog.i("LibFileUrl=" + url);    	
        
    	HttpURLConnection conn = null;
		InputStream is = null;
		OutputStream os = null;
        try {
        	if(type == LockerEngine.LIB_ENGINE_LD){
            	mDownloadingEngineLibLd = true;
            } else if(type == LockerEngine.LIB_ENGINE_LF){
            	mDownloadingEngineLibLf = true;
            }
        	
        	String strLibPath = getLibFilePath(type);
        	String tmpFileName = strLibPath + ".tmp";
            File tmpFile = new File(tmpFileName);
			if (!tmpFile.exists()) {
				tmpFile.createNewFile();
			}
			
            URL libUrl = new URL(url);
            conn = (HttpURLConnection) libUrl.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setInstanceFollowRedirects(true);
            if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            	is = conn.getInputStream();
                try {
                    os = new FileOutputStream(tmpFileName);
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while( (len=is.read(buffer)) != -1){
                    	os.write(buffer, 0, len);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally{
                	if (os != null) {
                		os.close();
                	}
                	if (is != null) {
                		is.close();
                	}
                	if (conn != null){
                		conn.disconnect();
                	}
                }
            }
            
			if (MD5.checkMD5(md5, tmpFile)) {
				File file = new File(strLibPath);
				if (file.exists()){
					file.delete();
				}
				tmpFile.renameTo(file);
				NqLog.i("new applib file size:" + tmpFile.length());
				result = true;
			} else {
				NqLog.i("checkMD5 failed");
				tmpFile.delete();
			}
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally{
        	if(type == LockerEngine.LIB_ENGINE_LD){
            	mDownloadingEngineLibLd = false;
            } else if(type == LockerEngine.LIB_ENGINE_LF){
            	mDownloadingEngineLibLf = false;
            }
        }
        
        return result;
    }
}