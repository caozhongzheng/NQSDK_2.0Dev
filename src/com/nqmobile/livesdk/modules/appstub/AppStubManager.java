/**
 * 
 */
package com.nqmobile.livesdk.modules.appstub;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Process;

import com.nqmobile.livesdk.LauncherSDK;
import com.nqmobile.livesdk.OnUpdateListener;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LogLevel;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.mydownloadmanager.IDownloadObserver;
import com.nqmobile.livesdk.commons.mydownloadmanager.MyDownloadManager;
import com.nqmobile.livesdk.commons.net.AbsProtocolEvent;
import com.nqmobile.livesdk.commons.prefetch.event.CancelSilentDownloadEvent;
import com.nqmobile.livesdk.commons.prefetch.event.PrefetchEvent;
import com.nqmobile.livesdk.commons.prefetch.event.PrefetchRequest;
import com.nqmobile.livesdk.commons.prefetch.event.PrefetchSuccessEvent;
import com.nqmobile.livesdk.commons.receiver.LiveReceiver;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.app.AppManager.Status;
import com.nqmobile.livesdk.modules.app.AppStatus;
import com.nqmobile.livesdk.modules.appstub.network.AppStubProtocol.AppStubFailedEvent;
import com.nqmobile.livesdk.modules.appstub.network.AppStubProtocol.AppStubSuccessEvent;
import com.nqmobile.livesdk.modules.appstub.network.AppStubServiceFactory;
import com.nqmobile.livesdk.modules.appstub.table.AppStubCacheTable;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.ToastUtils;

/**
 * @author chenyanmin
 * @time 2014-7-2 下午6:43:02
 */
public class AppStubManager extends AbsManager {
	private static final ILogger NqLog = LoggerFactory.getLogger(AppStubModule.MODULE_NAME);
	static {
		NqLog.setLogLevel(LogLevel.VERBOSE);
	}
	private Context mContext;
	private static AppStubManager mInstance;

	private AppStubPreference mPreference;

	private AppStubManager(Context context) {
		super();
		this.mContext = context.getApplicationContext();
		mPreference = AppStubPreference.getInstance();
	}

	public synchronized static AppStubManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new AppStubManager(context.getApplicationContext());
		}
		return mInstance;
	}

	@Override
	public void init() {
		EventBus.getDefault().register(this);
	}

	public boolean isAppStubEnabled() {
		return mPreference.getBooleanValue(AppStubPreference.KEY_APP_STUB_ENABLE);
	}

	public void processGetAppStub() {
		AppStubServiceFactory.getService().getAppStubList(null);
	}

	/**
	 * 应用列表存入虚框缓存库
	 * 
	 * @param apps
	 * @return
	 */
	public boolean cacheAppStub(List<App> apps) {
		try {
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			Builder b = null;
			// 清楚缓存
			// b =
			// ContentProviderOperation.newDelete(GameCacheTable.TABLE_URI).withSelection(null,
			// null);
			// ops.add(b.build());

			ContentResolver contentResolver = mContext.getContentResolver();

			if (apps != null && apps.size() > 0) {
				App app = null;
				long localTime = new Date().getTime();
				for (int i = 0; i < apps.size(); i++) {
					app = apps.get(i);
					if (app != null) {
						app.setLongLocalTime(localTime);
						ContentValues values = appToContentValues(AppStubConstants.STORE_MODULE_TYPE_APPSTUB, app);
						b = ContentProviderOperation.newInsert(
								AppStubCacheTable.TABLE_URI).withValues(values);
						ops.add(b.build());
					}
				}
			}

			contentResolver.applyBatch(AppStubCacheTable.DATA_AUTHORITY, ops);
			return true;
		} catch (Exception e) {
			NqLog.e("cacheApp error " + e.toString());
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 将app对象转换成数据库操作的ContentValues对象
	 * 
	 * @param column
	 *            8：虚框
	 * @param app
	 * @return
	 */
	public ContentValues appToContentValues(int column, App app) {
		ContentValues values = null;
		if (app != null) {
			values = new ContentValues();
			values.put(AppStubCacheTable.APP_ID, app.getStrId());
			values.put(AppStubCacheTable.APP_SOURCE_TYPE, app.getIntSourceType());
			values.put(AppStubCacheTable.APP_COLUMN, column);
			values.put(AppStubCacheTable.APP_TYPE, app.getType());
			values.put(AppStubCacheTable.APP_CATEGORY1, app.getStrCategory1());
			values.put(AppStubCacheTable.APP_CATEGORY2, app.getStrCategory2());
			values.put(AppStubCacheTable.APP_NAME, app.getStrName());
			values.put(AppStubCacheTable.APP_DESCRIPTION, app.getStrDescription());
			values.put(AppStubCacheTable.APP_DEVELOPERS, app.getStrDevelopers());
			values.put(AppStubCacheTable.APP_RATE, app.getFloatRate());
			values.put(AppStubCacheTable.APP_VERSION, app.getStrVersion());
			values.put(AppStubCacheTable.APP_SIZE, app.getLongSize());
			values.put(AppStubCacheTable.APP_DOWNLOAD_COUNT, app.getLongDownloadCount());
			values.put(AppStubCacheTable.APP_PACKAGENAME, app.getStrPackageName());
			values.put(AppStubCacheTable.APP_ICON_URL, app.getStrIconUrl());
			values.put(AppStubCacheTable.APP_IMAGE_URL, app.getStrImageUrl());
			// 预览图网址
			StringBuilder previewUrl = new StringBuilder();
			List<String> previewUrls = app.getArrPreviewUrl();
			if (previewUrls != null && previewUrls.size() > 0) {
				for (int j = 0; j < previewUrls.size(); j++) {
					previewUrl.append(previewUrls.get(j)).append(";");
				}
			}
			if (previewUrl.length() > 1) {
				values.put(AppStubCacheTable.APP_PREVIEW_URL,
						previewUrl.substring(0, previewUrl.length() - 1));
			} else {
				values.put(AppStubCacheTable.APP_PREVIEW_URL, "");
			}
			values.put(AppStubCacheTable.APP_URL, app.getStrAppUrl());
			values.put(AppStubCacheTable.APP_CLICK_ACTION_TYPE, app.getIntClickActionType());
			values.put(AppStubCacheTable.APP_DOWNLOAD_ACTION_TYPE, app.getIntDownloadActionType());
			values.put(AppStubCacheTable.APP_ICON_PATH, app.getStrIconPath());
			values.put(AppStubCacheTable.APP_IMAGE_PATH, app.getStrImagePath());
			// 预览图本地路径
			StringBuilder previewPath = new StringBuilder();
			List<String> previewPaths = app.getArrPreviewPath();
			if (previewPaths != null && previewPaths.size() > 0) {
				for (int j = 0; j < previewPaths.size(); j++) {
					previewPath.append(previewPaths.get(j)).append(";");
				}
			}
			if (previewPath.length() > 1) {
				values.put(AppStubCacheTable.APP_PREVIEW_PATH,
						previewPath.substring(0, previewPath.length() - 1));
			} else {
				values.put(AppStubCacheTable.APP_PREVIEW_PATH, "");
			}
			values.put(AppStubCacheTable.APP_PATH, app.getStrAppPath());
			values.put(AppStubCacheTable.APP_UPDATETIME, app.getLongUpdateTime());
			values.put(AppStubCacheTable.APP_LOCALTIME, app.getLongLocalTime());
			values.put(AppStubCacheTable.APP_REWARDPOINTS, app.getRewardPoints());
			values.put(AppStubCacheTable.APP_TRACKID, app.getTrackId());
		}
		return values;
	}

	public void addAppStub(List<OnUpdateListener> listeners, App app) {
		if (listeners == null || listeners.size() == 0 || app == null) {
			return;
		}

		Intent intent = getAppStubIntent(app);
		for (Iterator<OnUpdateListener> it = listeners.iterator(); it.hasNext();) {
			OnUpdateListener l = null;
			try {
				l = it.next();
			} catch (Exception e) {
				NqLog.e("addAppStub " + e.toString());
				e.printStackTrace();
			}
			if (l == null)
				continue;
			l.onAppStubAdd(app, intent);
		}
        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
        		AppStubConstants.ACTION_LOG_2301, app.getStrId(), 0,
				app.getStrPackageName());
	}

	public App getAppStubFromCache(String appId) {
		App app = null;
		ContentResolver contentResolver = mContext.getContentResolver();
		Cursor cursor = null;

		try {
			cursor = contentResolver.query(AppStubCacheTable.TABLE_URI, null,
					AppStubCacheTable.APP_SOURCE_TYPE + "="
							+ AppStubConstants.STORE_MODULE_TYPE_APPSTUB + " AND "
							+ AppStubCacheTable.APP_ID + "=?",
					new String[] { String.valueOf(appId) },
					AppStubCacheTable._ID + " asc");
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToNext();
				app = AppManager.getInstance(mContext).cursorToApp(cursor);
			}
		} catch (Exception e) {
			NqLog.e("getAppStubFromCache " + e.toString());
			e.printStackTrace();
		} finally {
			try {
				if (cursor != null) {
					cursor.close();
				}
			} catch (Exception e2) {
			}
		}
		return app;
	}

	public Intent getAppStubIntent(App app) {
		NqLog.v("getAppStubIntent()");
		if (app == null)
			return null;
		Intent intent = new Intent();
		intent.setClassName(mContext.getPackageName(),
				AppStubDetailActivity.class.getName());
		// intent.setClass(mContext, AppStubDetailActivity.class);
		intent.setAction(AppStubDetailActivity.INTENT_ACTION);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(AppStubDetailActivity.KEY_APP, app);
		intent.putExtra(AppStubDetailActivity.KEY_APP_ID, app.getStrId());

		return intent;
	}

	public void unregisterAppStub(Long downloadId, App app) {
		NqLog.i("LauncherSDK. unregisterAppStub AppStub: downId=" + downloadId
						+ ",app=" + app.getStrId() + "/" + app.getStrName());
		MyDownloadManager.getInstance(mContext).unregisterDownloadObserver(
				downloadId);
	}

	public void registerAppStub(Long downloadId, App mApp) {
		final App app = mApp;
		final Long downId = downloadId;
		MyDownloadManager.getInstance(mContext).registerDownloadObserver(
				downloadId, new IDownloadObserver() {

					@Override
					public void onChange() {
						updateProgress(downId, app);
					}

				});
	}

	private void updateProgress(Long downloadId, App mApp) {
		NqLog.d("updateAppStubProgress: pid=" + Process.myPid() + ",uid="
				+ Process.myUid() + ",CallingPid=" + Binder.getCallingPid()
				+ ",CallingUid=" + Binder.getCallingUid());
		Status mStatus = AppManager.getInstance(mContext).getStatus(mApp);
		AppStatus mAppStatus = new AppStatus();
		mAppStatus.setTotalBytes(mStatus.totalBytes);
		mAppStatus.setStatusCode(mStatus.statusCode);
		NqLog.d(mApp.toString());
		// NqLog.d("statusCode=" + mStatus.statusCode + ",downloadedBytes=" +
		// mStatus.downloadedBytes + ",totalBytes" + mStatus.totalBytes);
		switch (mStatus.statusCode) {
		case AppManager.STATUS_UNKNOWN: // 按STATUS_NONE处理
		case AppManager.STATUS_NONE:
			unregisterAppStub(downloadId, mApp);
			break;
		case AppManager.STATUS_DOWNLOADING:
			if (mStatus.totalBytes > 0 && mStatus.downloadedBytes >= 0
					&& mStatus.downloadedBytes <= mStatus.totalBytes) {

				int percentage = (int) ((0.01 + mStatus.downloadedBytes)
						/ mStatus.totalBytes * 100);
				mAppStatus.setDownloadedBytes(mStatus.downloadedBytes);
				mAppStatus.setDownloadProgress(percentage);
				if (percentage == 100) {
					ToastUtils.toast(mContext, "nq_label_download_success");
				}
			} else {// pending状态
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
			} else {// pending状态
				mAppStatus.setDownloadedBytes(mStatus.downloadedBytes);
				mAppStatus.setDownloadProgress(0);
			}
			break;
		case AppManager.STATUS_DOWNLOADED:
			mAppStatus.setDownloadedBytes(mStatus.downloadedBytes);
			mAppStatus.setDownloadProgress(100);
			break;
		case AppManager.STATUS_INSTALLED:
			unregisterAppStub(downloadId, mApp);
			break;
		default:
			break;
		}
		NqLog.d(downloadId + " =downloadId AppStub STATUS: " + mAppStatus);

		sendAppStubProgress(mApp, mAppStatus);
		if (mAppStatus.downloadProgress >= 100
				|| mStatus.statusCode == AppManager.STATUS_NONE) {
			unregisterAppStub(downloadId, mApp);
		}
	}

	private void sendAppStubProgress(App app, AppStatus status) {
		List<OnUpdateListener> listeners = LauncherSDK.getInstance(
                mContext).getRegisteredOnUpdateListeners();
		Intent intent = getIntent();
		if (listeners == null || listeners.size() == 0) {
		} else {
			for (OnUpdateListener l : listeners) {
				intent.putExtra(AppStubDetailActivity.KEY_APP, app);
				intent.putExtra(AppStubDetailActivity.KEY_APP_ID,
						app.getStrId());
				l.onAppStubStatusUpdate(app, status);
			}
		}
	}

	private Intent getIntent() {
		Intent intent = new Intent();
		intent.setClass(mContext, AppStubDetailActivity.class);
		intent.setAction(AppStubDetailActivity.INTENT_ACTION);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return intent;
	}

	public void registerAppStubAfterReboot() {
		AppManager aMgr = AppManager.getInstance(mContext);
		List<App> apps = aMgr
				.getAppListFromLocal(AppStubConstants.STORE_MODULE_TYPE_APPSTUB);
		if (apps == null || apps.size() == 0)
			return;
		for (App app : apps) {
			if (app != null) {
				int statusCode = aMgr.getStatus(app).statusCode;
				if (statusCode == AppManager.STATUS_INSTALLED
						|| statusCode == AppManager.STATUS_DOWNLOADED) {
					continue;
				} else {
					long downId = MyDownloadManager.getInstance(mContext)
							.getDownloadId(app.getStrAppUrl());
					registerAppStub(downId, app);
				}
			}
		}
	}

	public void onEvent(AppStubSuccessEvent event) {
		NqLog.d("onEvent(AppStubSuccessEvent)");
		List<App> apps = event.getApps();
		if (apps == null || apps.size() == 0) {
			NqLog.w("AppStubSuccessEvent: apps="+apps);
			return;
		}
		
		cacheAppStub(apps);
		PrefetchEvent prefetchEvent = new PrefetchEvent();
		ArrayList<PrefetchRequest> requests = new ArrayList<PrefetchRequest>(apps.size());
		for (App app : apps) {
			if (app == null) {
				continue;
			}
			NqLog.d("get AppStub=" + app.toString());
			Intent i = new Intent();
			i.setAction(LiveReceiver.ACTION_APPSTUB_ADD);
			i.putExtra("stub_app", app);
			mContext.sendBroadcast(i);
			if (app.isSlientDownload()) {
				String resId = app.getStrId();
				int type = PrefetchRequest.TYPE_APK;
				String url = app.getStrAppUrl();
				String path = app.getStrAppPath();
				long size = app.getLongSize();
				PrefetchRequest req = new PrefetchRequest(prefetchEvent,
						resId, type, url, path, size);
				req.setPackageName(app.getStrPackageName());
				
				requests.add(req);
			}
		}
		if (requests.size() > 0) {
			prefetchEvent.setFeatureId(113);
			prefetchEvent
					.setSourceType(AppStubConstants.STORE_MODULE_TYPE_APPSTUB);
			prefetchEvent.setRequests(requests);

			EventBus.getDefault().post(prefetchEvent);
		}
	}

	public void onEvent(AppStubFailedEvent event) {
		// do nothing\
		NqLog.w("AppStubFailedEvent");
	}
	public void onEvent(PrefetchSuccessEvent event) {
		//首先过滤掉不是自己发的event
		if (event.getFeatureId() != 113) {
			return;
		}
		// 将虚框变为实框
		App stub = new App();
		stub.setStrPackageName(event.getPackageName());
		AppStatus status = new AppStatus();
		status.setStatusCode(AppStatus.STATUS_DOWNLOADED);
		status.setDownloadProgress(100);
		sendAppStubProgress(stub, status);
	}

	public static class AppStubDeleteEvent extends AbsProtocolEvent {
		private String resID;
		private String packageName;
    	
    	public AppStubDeleteEvent(String resID, String packageName, Object tag){
    		setTag(tag);
    		setResID(resID);
    		setPackageName(packageName);
    	}

		public String getResID() {
			return resID;
		}

		public void setResID(String resID) {
			this.resID = resID;
		}

		public String getPackageName() {
			return packageName;
		}

		public void setPackageName(String packageName) {
			this.packageName = packageName;
		}

    }
	public void onEvent(AppStubDeleteEvent event) {
		EventBus.getDefault().post(new CancelSilentDownloadEvent(event.getResID()));
	}
}
