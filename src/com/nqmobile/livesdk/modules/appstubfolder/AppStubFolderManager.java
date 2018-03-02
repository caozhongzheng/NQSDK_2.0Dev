package com.nqmobile.livesdk.modules.appstubfolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.text.TextUtils;

import com.nq.interfaces.launcher.TAppResource;
import com.nq.interfaces.launcher.TVirtualFolder;
import com.nq.interfaces.launcher.TVirtualFolderAppResp;
import com.nqmobile.livesdk.OnUpdateListener;
import com.nqmobile.livesdk.commons.concurrent.PriorityExecutor;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.prefetch.event.CancelSilentDownloadEvent;
import com.nqmobile.livesdk.commons.prefetch.event.PrefetchEvent;
import com.nqmobile.livesdk.commons.prefetch.event.PrefetchRequest;
import com.nqmobile.livesdk.commons.receiver.ConnectivityChangeEvent;
import com.nqmobile.livesdk.commons.receiver.LiveReceiver;
import com.nqmobile.livesdk.commons.receiver.PackageAddedEvent;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppConstant;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.app.AppManager.Status;
import com.nqmobile.livesdk.modules.appstub.model.AppStub;
import com.nqmobile.livesdk.modules.appstubfolder.model.AppStubFolder;
import com.nqmobile.livesdk.modules.appstubfolder.network.AppStubFolderProtocol.AppStubFolderPrefChangeEvent;
import com.nqmobile.livesdk.modules.appstubfolder.network.AppStubFolderProtocol.GetAppStubFolderListFailedEvent;
import com.nqmobile.livesdk.modules.appstubfolder.network.AppStubFolderProtocol.GetAppStubFolderListSuccEvent;
import com.nqmobile.livesdk.modules.appstubfolder.network.AppStubFolderServiceFactory;
import com.nqmobile.livesdk.modules.appstubfolder.table.AppStubFolderAppCacheTable;
import com.nqmobile.livesdk.modules.appstubfolder.table.AppStubFolderCacheTable;
import com.nqmobile.livesdk.modules.appstubfolder.table.AppStubFolderResTable;
import com.nqmobile.livesdk.modules.gamefolder_v2.PreloadListener;
import com.nqmobile.livesdk.modules.gamefolder_v2.PreloadRunnable;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.CollectionUtils;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.StringUtil;

public class AppStubFolderManager extends AbsManager {
	private static final ILogger NqLog = LoggerFactory.getLogger(AppStubFolderModule.MODULE_NAME);
	
	private Context mContext;
	private static AppStubFolderManager mInstance;
	private AppStubFolderPreference mPreference;
	public static final int MAX_APPSTUBFOLDER_SIZE = Integer.MAX_VALUE;

	public AppStubFolderManager(Context context) {
		this.mContext = context.getApplicationContext();
		this.mPreference = AppStubFolderPreference.getInstance();
	}

	public synchronized static AppStubFolderManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new AppStubFolderManager(context);
		}
		return mInstance;
	}

	@Override
	public void init() {
		EventBus.getDefault().register(this);
        onEnabled();
	}

	private void createAppStubFolder(long folderID) {
		NqLog.i("虚框文件夹准备展示, id=" + folderID);
		final List<AppStubFolder> appStubFolderList = getAppStubFolderListFromCache(0, folderID, true);

		// 打日志
		if(appStubFolderList != null) {
			int i=1;
			for(AppStubFolder asf:appStubFolderList) {
				if(asf != null && asf.getStubList() != null) {
					NqLog.i("第" + i + "个虚框文件夹[" + asf.getFolderId() + "/"
							+ asf.getName() + "]有虚框应用个数 = "
							+ asf.getStubList().size());
				}
			}
		}

		if(appStubFolderList != null && !appStubFolderList.isEmpty()) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						NqLog.i("虚框文件夹sleep15000 start " + System.currentTimeMillis());
						Thread.sleep(15000);
						NqLog.i("虚框文件夹sleep15000 over" + System.currentTimeMillis());
						sendBroadcast(appStubFolderList);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
			
		}
	}

	private void sendBroadcast(List<AppStubFolder> appStubFolderList) {
		for(AppStubFolder sf:appStubFolderList) {
			if(sf != null && sf.getStubList() != null && !sf.getStubList().isEmpty()) {
				NqLog.d("get AppStubFolder=" + sf.toString());
				Intent i = new Intent();
				i.setAction(LiveReceiver.ACTION_APPSTUB_FOLDER_ADD);
				i.putExtra("stub_folder_id", sf.getFolderId());
				i.putExtra("stub_folder_name", sf.getName());
				i.putExtra("stub_folder_icon_url", sf.getIconUrl());
				i.putExtra("stub_folder_icon_path", sf.getIconPath());
				i.putExtra("stub_folder_type", sf.getType());
				i.putExtra("stub_folder_editable", sf.isDeletable());
				i.putExtra("stub_folder_deletable", sf.isEditable());
				i.putExtra("stub_foldr_show_redPoint",sf.isShowRedPoint());
				int index = 0;
				for(AppStub as : sf.getStubList()) {
					NqLog.d(sf.getFolderId() + " , " + sf.getName()
							+ " setApp " + index + ": " + as.getApp().toString());
					i.putExtra("stub_folder_apps_" + index, as.getApp());
					index++;
				}
				mContext.sendBroadcast(i);
			}
		}
	}

	public void addAppStubFolderToLauncher(List<OnUpdateListener> listeners,
			AppStubFolder asf) {
        if (listeners == null || listeners.size() == 0 || asf == null) {
			return;
		}

		for(int i=0; i<listeners.size(); i++) {
			OnUpdateListener l = null;
			try {
				l = listeners.get(i);
            } catch (Exception e) {
				e.printStackTrace();
			}
			if(l == null)
				continue;
			
			if(isFolderCreated(asf)) {
				NqLog.i("onAppStubFolderUpdate id= " + asf.getFolderId()
						+ " ,name= " + asf.getName() + " ,res= " + asf.getStubList());
				l.onAppStubFolderUpdate(asf);
			} else {
				NqLog.i("onAppStubFolderAdd id= " + asf.getFolderId()
						+ " ,name= " + asf.getName() + " ,res= " + asf.getStubList());
				l.onAppStubFolderAdd(asf);
			}
		}
	}

	/** 虚框文件夹开关打开 */
	public void onEnabled() {
		if(!mPreference.isStubFolderFirstEnabled()) {
			getStubFolderFromServer(0, getCreatedStubFolderIds(), null);
		}
	}

	public void getStubFolderFromServer(int scene, List<String> folderIDList,
			StubFolderListener listener) {
		if (!isAppStubFolderEnable()) {
			return;
		}
		NqLog.i("scene= " + scene);
		AppStubFolderServiceFactory.getService().getAppStubFolderList(scene,
				folderIDList, listener);
	}

	public int getBossStubFolderShowCount() {
		return mPreference
				.getIntValue(AppStubFolderPreference.KEY_STUB_FOLDER_SHOW_COUNT);
	}

	public boolean isFolderOpratable() {
		return mPreference
				.getBooleanValue(AppStubFolderPreference.KEY_STUB_FOLDER_OPRATABLE);
	}

	public boolean isFolderResDeletable() {
		return mPreference
				.getBooleanValue(AppStubFolderPreference.KEY_STUB_FOLDER_RES_DELETABLE);
	}

	public AppStubFolder vfResourceToStubFolder(TVirtualFolder t) {
		AppStubFolder sf = null;
		if(t != null) {
			sf = new AppStubFolder();
			long fid = 0;
			try {
				fid = Long.parseLong(t.getFolderId());
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(fid == 0){
				fid = t.getFolderId().hashCode();
			}
			if(isFolderDeteleted(fid)) {
				return null;
			}
			sf.setFolderId(fid == 0 ? -60001L : fid);
			sf.setDeletable(isFolderResDeletable());
			sf.setEditable(isFolderOpratable());
			sf.setIconId(0);
			sf.setIconUrl(StringUtil.nullToEmpty(t.getIcon()));
			sf.setName(StringUtil.nullToEmpty(t.getFolderName()));
			sf.setScreen(0);
			sf.setX(0);
			sf.setY(0);
			sf.setType(1);
			sf.setShowRedPoint(true);
			String SDCardPath = CommonMethod.getSDcardPath(mContext);
			if(SDCardPath == null)
				SDCardPath = CommonMethod.getSDcardPathFromPref(mContext);
			//虚框文件夹icon本地路径
			if (!TextUtils.isEmpty(t.getIcon())) {
				String strImgPath = new StringBuilder().append(SDCardPath)
						.append(AppConstant.STORE_IMAGE_LOCAL_PATH_APP).toString();
				sf.setIconPath(new StringBuilder().append(strImgPath)
						.append(sf.getFolderId()).append("_stubFolder_icon")
						.append(StringUtil.getExt(t.getIcon())).toString());
			}
			ArrayList<AppStub> stubList = null;
			if(t.virtualApps != null && !t.virtualApps.isEmpty()) {
				stubList = new ArrayList<AppStub>();
				AppManager am = AppManager.getInstance(mContext);
				int i=0;
				for(TAppResource ar:t.virtualApps) {
					App app = new App(ar, mContext);
					NqLog.i("StubFolderProtocol ok " + t.getFolderName()
							+ ":" + ++i + "/" + app.toString());
					app.setIntSourceType(AppStubFolderConstants.STORE_MODULE_TYPE_STUB_FOLDER);
					if(app != null) {
						Intent intent_appstub = new Intent();
//						Intent intent_appstub = AppStubManager.getInstance(mContext)
//								.getAppStubIntent(app);
						stubList.add(new AppStub(app, intent_appstub));
					}
				}
			}
			sf.setStubList(stubList);
		}

		return sf;
	}

	private boolean isFolderDeteleted(long fid) {
        Cursor c = null;
        try{
            c = mContext.getContentResolver().query(AppStubFolderCacheTable.TABLE_URI,null,
                    AppStubFolderCacheTable.STUBFOLDER_ID + " = ? AND " + AppStubFolderCacheTable.STUBFOLDER_DELETED + " = 1",
                    new String[]{String.valueOf(fid)},null);
            if(c != null && c.moveToNext()){
                return true;
            }
        }finally {
            if(c != null){
                c.close();
            }
        }
		return false;
	}

	public boolean cacheStubFolder(List<AppStubFolder> stubFolderList) {
		if (stubFolderList == null || stubFolderList.isEmpty()) {
			return false;
		}
		try {
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			Builder b = null;
			ContentResolver contentResolver = mContext.getContentResolver();
			
			if (stubFolderList != null && stubFolderList.size() > 0) {
				AppStubFolder sf = null;
				for (int i = 0; i < stubFolderList.size(); i++) {
					sf = stubFolderList.get(i);
					if (sf != null) {
						if(obnj!=null){
							obnj.put(sf.getFolderId(),sf );
						}
						// 清除缓存
						b = ContentProviderOperation.newDelete(AppStubFolderCacheTable.TABLE_URI)
								.withSelection(AppStubFolderCacheTable.STUBFOLDER_ID + "=?",
										new String[]{String.valueOf(sf.getFolderId())});
						ops.add(b.build());
						
						ContentValues values = stubfolderToContentValues(sf);
						
						b = ContentProviderOperation.newInsert(AppStubFolderCacheTable.TABLE_URI)
								.withValues(values);
						ops.add(b.build());
						try {
							NqLog.i("cacheStubFolder ok " + sf.getFolderId()
									 + "/" + sf.getName());
						} catch (Exception e) {
							NqLog.e(e);
						}
					}
				}
			}

			contentResolver.applyBatch(AppStubFolderCacheTable.DATA_AUTHORITY, ops);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private ContentValues stubfolderToContentValues(AppStubFolder sf) {
		ContentValues values = null;
		if (sf != null) {
			values = new ContentValues();
			values.put(AppStubFolderCacheTable.STUBFOLDER_ID, sf.getFolderId());
			values.put(AppStubFolderCacheTable.STUBFOLDER_NAME, sf.getName());
			values.put(AppStubFolderCacheTable.STUBFOLDER_TYPE, sf.getType());
			values.put(AppStubFolderCacheTable.STUBFOLDER_ENABLE, 0);
			values.put(AppStubFolderCacheTable.STUBFOLDER_SCR, sf.getScreen());
			values.put(AppStubFolderCacheTable.STUBFOLDER_X, sf.getX());
			values.put(AppStubFolderCacheTable.STUBFOLDER_Y, sf.getY());
			values.put(AppStubFolderCacheTable.STUBFOLDER_ICON_ID, sf.getIconId());
			values.put(AppStubFolderCacheTable.STUBFOLDER_ICON_URL, sf.getIconUrl());
			values.put(AppStubFolderCacheTable.STUBFOLDER_ICON_PATH, sf.getIconPath());
			values.put(AppStubFolderCacheTable.STUBFOLDER_SHOW_COUNT, 0);
			values.put(AppStubFolderCacheTable.STUBFOLDER_SHOW_TIME, 0);
			values.put(AppStubFolderCacheTable.STUBFOLDER_EDITABLE, sf.isEditable()?1:0);
			values.put(AppStubFolderCacheTable.STUBFOLDER_DELETABLE, sf.isDeletable()?1:0);
			values.put(AppStubFolderCacheTable.STUBFOLDER_SHOW_RED_POINT,sf.isShowRedPoint()?1:0);
		}
		return values;
	}

	/** 预存从服务器端获取的资源列表到STUBFOLDER_APP_CACHE表，等到预取全部OK后再转移到APP_CACHE表 */

	public boolean cacheStubFolderApp(List<AppStubFolder> stubFolderList) {
		try {
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			Builder b = null;
			ContentResolver contentResolver = mContext.getContentResolver();
			
			if (stubFolderList != null && stubFolderList.size() > 0) {
				List<AppStub> stubres = null;
				long localTime = new Date().getTime();
				for (int i = 0; i < stubFolderList.size(); i++) {
					stubres = stubFolderList.get(i).getStubList();
					long folderID = stubFolderList.get(i).getFolderId();
					NqLog.i("cacheStubFolderApp folderID=" + folderID
							+ " , " + stubFolderList.get(i).getName());
					// 清除缓存
					b = ContentProviderOperation.newDelete(AppStubFolderAppCacheTable.TABLE_URI)
							.withSelection(AppStubFolderAppCacheTable.APP_COLUMN + "=?", new String[]{String.valueOf(folderID)});
					ops.add(b.build());
					App app = null;
					for (int j = 0; j < stubres.size(); j++) {
						app = stubres.get(j).getApp();
						if (app != null) {
							NqLog.i("cacheStubFolderApp " + j + " appInfo:" + app.toString());
							app.setLongLocalTime(localTime);
							ContentValues values = appToContentValues(folderID, app);
							b = ContentProviderOperation.newInsert(AppStubFolderAppCacheTable.TABLE_URI)
									.withValues(values);
							ops.add(b.build());
						}
					}
				}
			}

			contentResolver.applyBatch(AppStubFolderAppCacheTable.DATA_AUTHORITY,ops);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean isFolderCreated(AppStubFolder asf) {
		if(asf == null) {
			return false;
		}

        Cursor c = null;
        try{
            c = mContext.getContentResolver().query(AppStubFolderCacheTable.TABLE_URI,null,
                    AppStubFolderCacheTable.STUBFOLDER_ID + " = ? AND " + AppStubFolderCacheTable.STUBFOLDER_CREATED +" = 1",
                    new String[]{String.valueOf(asf.getFolderId())},null);
            if(c != null && c.moveToNext()){
                return true;
            }
        }finally {
            if(c != null){
                c.close();
            }
        }

        return false;
	}

	/** 桌面创建虚框文件夹 */
    public void onFolderCreate(long folderId){
        flagFolderCreate(folderId);
        String name = getAppStubFolderName(folderId);
        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
				AppStubFolderConstants.ACTION_LOG_2601, null, 0, name);
    }

    /** 桌面打开虚框文件夹 */

	public void onFolderClick(final long folderId) {
		new Thread(){
            @Override
            public void run() {
                onStubFolderOpen(folderId);
                onStubFolderResShow(folderId);
            }
        }.start();
	}

	/** 桌面删除虚框文件夹 */
	public void onFolderDelete(long folderId) {
        flagFolderDelete(folderId);
		
		String name = getAppStubFolderName(folderId);
		StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
				AppStubFolderConstants.ACTION_LOG_2604, null, 0, name);

		cancelSilentDownload(folderId);
		deleteStubResInAppCacheNew(folderId);
		deleteStubResInStubFolderCache(folderId);
		deleteStubResInStubFolderAppCache(folderId);
	}

	private void onStubFolderOpen(long folderId) {
		String name = getAppStubFolderName(folderId);
        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
        		AppStubFolderConstants.ACTION_LOG_2602, null, 0, name);
		updateShowTimeAndCount(folderId);
	}

    private void flagFolderCreate(long fid){
        ContentValues value = new ContentValues();
        value.put(AppStubFolderCacheTable.STUBFOLDER_CREATED,1);
        mContext.getContentResolver().update(AppStubFolderCacheTable.TABLE_URI,value,
                AppStubFolderCacheTable.STUBFOLDER_ID +" = ?",new String[]{String.valueOf(fid)});
    }

    private void flagFolderDelete(long fid){
        ContentValues value = new ContentValues();
        value.put(AppStubFolderCacheTable.STUBFOLDER_DELETED,1);
        mContext.getContentResolver().update(AppStubFolderCacheTable.TABLE_URI,value,
                AppStubFolderCacheTable.STUBFOLDER_ID +" = ?",new String[]{String.valueOf(fid)});
    }
	
	/** 获取文件夹名, 如果文件夹名字为空，返回folderID */
	private String getAppStubFolderName(long folderId) {
		String name = "";
		Cursor cursor = null;
		try {
			ContentResolver contentResolver = mContext.getContentResolver();
			cursor = contentResolver.query(AppStubFolderCacheTable.TABLE_URI, null,
							AppStubFolderCacheTable.STUBFOLDER_ID + "=?",
							new String[]{String.valueOf(folderId)}, null);
			if(cursor != null && cursor.moveToNext()){
				name = cursor.getString(cursor.getColumnIndex(AppStubFolderCacheTable.STUBFOLDER_NAME));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(cursor != null)
					cursor.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return TextUtils.isEmpty(name) ? folderId+"" : name;
	}


	private void updateShowTimeAndCount(long folderId) {
		int count = getFolderShowCount(folderId) + 1;
		
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		ContentValues values = new ContentValues();
		ContentProviderOperation.Builder b = null;
		NqLog.e("stubFolder " + folderId + " newShowCount=" + count);
		values.put(AppStubFolderCacheTable.STUBFOLDER_SHOW_COUNT, count);
		values.put(AppStubFolderCacheTable.STUBFOLDER_SHOW_TIME, System.currentTimeMillis());
		values.put(AppStubFolderCacheTable.STUBFOLDER_SHOW_RED_POINT, 0);
		b = ContentProviderOperation.newUpdate(AppStubFolderCacheTable.TABLE_URI)
			  .withValues(values)
			  .withSelection(AppStubFolderCacheTable.STUBFOLDER_ID + "=?",new String[]{String.valueOf(folderId)});
		ops.add(b.build());
		try {
			mContext.getContentResolver().applyBatch(AppStubFolderCacheTable.DATA_AUTHORITY, ops);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(count >= getBossStubFolderShowCount()
				&& hasNoAppDownloading(folderId)) {
			List<String> folderIdList = new ArrayList<String>();
			folderIdList.add(String.valueOf(folderId));
			getStubFolderFromServer(1, folderIdList, null);
		}
	}

	/** 虚框文件夹中是否有资源在下载，或下载完还未安装 */
	private boolean hasNoAppDownloading(long folderId) {
		List<App> applist = getAppList(folderId);
		if(applist == null || applist.isEmpty()) {
			return true;
		}
		for(App app:applist) {
			if(isDownloadingOrDownloaded(app)) {
				NqLog.d(folderId + " 's " + app.getStrName() + " is in Downloading");
				return false;
			}
		}
		
		return true;
	}
	

	private boolean isDownloadingOrDownloaded(App app) {
		if(app == null)
			return false;
		boolean result = false;
		Status mStatus = AppManager.getInstance(mContext).getStatus(app);
		switch (mStatus.statusCode) {
		case AppManager.STATUS_UNKNOWN: // 按STATUS_NONE处理
		case AppManager.STATUS_NONE:
			break;
		case AppManager.STATUS_DOWNLOADING:
		case AppManager.STATUS_PAUSED:
			result = true;
			break;
		case AppManager.STATUS_DOWNLOADED:
		case AppManager.STATUS_INSTALLED:
			break;
		default:
			break;
		}
		
		return result;
	}


	private List<App> getAppList(long folderId) {
		List<App> applist = new ArrayList<App>();
		Cursor cursor = null;
		try {
			ContentResolver contentResolver = mContext.getContentResolver();
			cursor = contentResolver.query(AppStubFolderResTable.TABLE_URI, null,
					AppStubFolderResTable.APP_SOURCE_TYPE + "=" + AppStubFolderConstants.STORE_MODULE_TYPE_STUB_FOLDER
					 + " AND " + AppStubFolderResTable.APP_COLUMN + " = " + folderId,
					null, null);
			if (cursor != null  && cursor.getCount() > 0) {
				AppManager am = AppManager.getInstance(mContext);
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
					App app = am.cursorToApp(cursor);
					if(app != null) {
						applist.add(app);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(cursor != null) {
					cursor.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return applist;
	}

	private int getFolderShowCount(long folderId) {
		int count = 0;
		Cursor cursor = null;
		try {
			ContentResolver contentResolver = mContext.getContentResolver();
			cursor = contentResolver.query(AppStubFolderCacheTable.TABLE_URI, null,
							AppStubFolderCacheTable.STUBFOLDER_ID + "=?",
							new String[]{String.valueOf(folderId)}, null);
			if(cursor != null && cursor.moveToNext()) {
				count = cursor.getInt(cursor.getColumnIndex(AppStubFolderCacheTable.STUBFOLDER_SHOW_COUNT));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(cursor != null) {
					cursor.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return count;
	}

	private void onStubFolderResShow(long folderId) {
		Cursor cursor = null;
		try {
			ContentResolver contentResolver = mContext.getContentResolver();
			cursor = contentResolver.query(AppStubFolderResTable.TABLE_URI, null,
				AppStubFolderResTable.APP_SOURCE_TYPE + "=" + AppStubFolderConstants.STORE_MODULE_TYPE_STUB_FOLDER
				+ " AND "
				+ AppStubFolderResTable.APP_COLUMN + "=?", new String[]{String.valueOf(folderId)}, null);
			if (cursor == null || cursor.getCount() == 0)
				return;

			AppManager am = AppManager.getInstance(mContext);
			while (cursor.moveToNext()) {
				App app = am.cursorToApp(cursor);
				if (app != null) {
                    StatManager.getInstance().onAction(
							StatManager.TYPE_STORE_ACTION,
							AppStubFolderConstants.ACTION_LOG_2603, app.getStrId(), 0,
							app.getStrPackageName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(cursor != null) {
					cursor.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/** 获取已创建的文件夹ID集合 */
	public List<String> getCreatedStubFolderIds() {
		List<String> folderIDs = new ArrayList<String>();
        Cursor c = null;
        try{
            c = mContext.getContentResolver().query(AppStubFolderCacheTable.TABLE_URI,null,
                    AppStubFolderCacheTable.STUBFOLDER_CREATED + " = 1",null,null);
            while(c != null && c.moveToNext()){
                String fid = String.valueOf(c.getLong(c.getColumnIndex(AppStubFolderCacheTable.STUBFOLDER_ID)));
                folderIDs.add(fid);
            }
        }finally {
            if(c != null){
                c.close();
            }
        }

		return folderIDs;
	}
	
	/** 获取虚框文件夹列表 
	 * @param type 文件夹icon预取失败的  1：文件夹icon预取成功的  2：全部*/
	public List<AppStubFolder> getStubFolderCache(int type) {
		List<AppStubFolder> appStubFolderList = new ArrayList<AppStubFolder>();
		Cursor cursor = null;
		try {
			ContentResolver contentResolver = mContext.getContentResolver();
			if(type == 2) {
				cursor = contentResolver.query(
						AppStubFolderCacheTable.TABLE_URI, null, null, null,
						AppStubFolderCacheTable._ID + " asc");
			} else {
				cursor = contentResolver.query(AppStubFolderCacheTable.TABLE_URI, null, 
						AppStubFolderCacheTable.STUBFOLDER_ENABLE + "=?", new String[]{type == 1 ? "1" : "0"},
						AppStubFolderCacheTable._ID + " asc");
			}
			if (cursor != null  && cursor.getCount() > 0) {
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
					AppStubFolder asf = cursorToAppStubFolder(cursor);
					if(asf != null) {
						appStubFolderList.add(asf);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(cursor != null) {
					cursor.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return appStubFolderList;
	}
	
	private AppStubFolder cursorToAppStubFolder(Cursor cursor) {
		if(cursor == null)
			return null;
		AppStubFolder asf = new AppStubFolder();
		asf.setDeletable(cursor.getInt(cursor.getColumnIndex(AppStubFolderCacheTable.STUBFOLDER_DELETABLE)) == 1);
		asf.setEditable(cursor.getInt(cursor.getColumnIndex(AppStubFolderCacheTable.STUBFOLDER_EDITABLE)) == 1);
		asf.setFolderId(cursor.getLong(cursor.getColumnIndex(AppStubFolderCacheTable.STUBFOLDER_ID)));
		asf.setIconId(cursor.getInt(cursor.getColumnIndex(AppStubFolderCacheTable.STUBFOLDER_ICON_ID)));
		asf.setIconPath(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AppStubFolderCacheTable.STUBFOLDER_ICON_PATH))));
		asf.setIconUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AppStubFolderCacheTable.STUBFOLDER_ICON_URL))));
		asf.setName(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AppStubFolderCacheTable.STUBFOLDER_NAME))));
		asf.setScreen(cursor.getInt(cursor.getColumnIndex(AppStubFolderCacheTable.STUBFOLDER_SCR)));
		asf.setX(cursor.getInt(cursor.getColumnIndex(AppStubFolderCacheTable.STUBFOLDER_X)));
		asf.setY(cursor.getInt(cursor.getColumnIndex(AppStubFolderCacheTable.STUBFOLDER_Y)));
		asf.setStubList(null);
		asf.setType(cursor.getInt(cursor.getColumnIndex(AppStubFolderCacheTable.STUBFOLDER_TYPE)));
        asf.setShowRedPoint(cursor.getInt(cursor.getColumnIndex(AppStubFolderCacheTable.STUBFOLDER_SHOW_RED_POINT)) == 1);
		
		return asf;
	}

	
	/**
	 * 将app对象转换层数据库操作的ContentValues对象
	 *
	 * @param column
	 * @param app
	 * @return
	 */
	private ContentValues appToContentValues(long column, App app) {
		ContentValues values = null;
		if (app != null) {
			values = new ContentValues();
			values.put(AppStubFolderAppCacheTable.APP_ID, app.getStrId());
			values.put(AppStubFolderAppCacheTable.APP_SOURCE_TYPE, app.getIntSourceType());
			values.put(AppStubFolderAppCacheTable.APP_COLUMN, column);
			values.put(AppStubFolderAppCacheTable.APP_TYPE, app.getType());
			values.put(AppStubFolderAppCacheTable.APP_CATEGORY1, app.getStrCategory1());
			values.put(AppStubFolderAppCacheTable.APP_CATEGORY2, app.getStrCategory2());
			values.put(AppStubFolderAppCacheTable.APP_NAME, app.getStrName());
			values.put(AppStubFolderAppCacheTable.APP_DESCRIPTION, app.getStrDescription());
			values.put(AppStubFolderAppCacheTable.APP_DEVELOPERS, app.getStrDevelopers());
			values.put(AppStubFolderAppCacheTable.APP_RATE, app.getFloatRate());
			values.put(AppStubFolderAppCacheTable.APP_VERSION, app.getStrVersion());
			values.put(AppStubFolderAppCacheTable.APP_SIZE, app.getLongSize());
			values.put(AppStubFolderAppCacheTable.APP_DOWNLOAD_COUNT, app.getLongDownloadCount());
			values.put(AppStubFolderAppCacheTable.APP_PACKAGENAME, app.getStrPackageName());
			values.put(AppStubFolderAppCacheTable.APP_ICON_URL, app.getStrIconUrl());
			values.put(AppStubFolderAppCacheTable.APP_IMAGE_URL, app.getStrImageUrl());
			//预览图网址
			StringBuilder previewUrl = new StringBuilder();
			List<String> previewUrls = app.getArrPreviewUrl();
			if (previewUrls != null && previewUrls.size() > 0) {
				for (int j = 0; j < previewUrls.size(); j++) {
					previewUrl.append(previewUrls.get(j)).append(";");
				}
			}
			if (previewUrl.length() > 1) {
				values.put(AppStubFolderAppCacheTable.APP_PREVIEW_URL,
						previewUrl.substring(0, previewUrl.length() - 1));
			} else {
				values.put(AppStubFolderAppCacheTable.APP_PREVIEW_URL, "");
			}
			values.put(AppStubFolderAppCacheTable.APP_URL, app.getStrAppUrl());
			values.put(AppStubFolderAppCacheTable.APP_CLICK_ACTION_TYPE, app.getIntClickActionType());
			values.put(AppStubFolderAppCacheTable.APP_DOWNLOAD_ACTION_TYPE, app.getIntDownloadActionType());
			values.put(AppStubFolderAppCacheTable.APP_ICON_PATH, app.getStrIconPath());
			values.put(AppStubFolderAppCacheTable.APP_IMAGE_PATH, app.getStrImagePath());
			//预览图本地路径
			StringBuilder previewPath = new StringBuilder();
			List<String> previewPaths = app.getArrPreviewPath();
			if (previewPaths != null && previewPaths.size() > 0) {
				for (int j = 0; j < previewPaths.size(); j++) {
					previewPath.append(previewPaths.get(j)).append(";");
				}
			}
			if (previewPath.length() > 1) {
				values.put(AppStubFolderAppCacheTable.APP_PREVIEW_PATH,
						previewPath.substring(0, previewPath.length() - 1));
			} else {
				values.put(AppStubFolderAppCacheTable.APP_PREVIEW_PATH, "");
			}
			values.put(AppStubFolderAppCacheTable.APP_PATH, app.getStrAppPath());
			values.put(AppStubFolderAppCacheTable.APP_UPDATETIME, app.getLongUpdateTime());
			values.put(AppStubFolderAppCacheTable.APP_LOCALTIME, app.getLongLocalTime());
			values.put(AppStubFolderAppCacheTable.APP_REWARDPOINTS,app.getRewardPoints());
			values.put(AppStubFolderAppCacheTable.APP_TRACKID,app.getTrackId());
		}
		return values;
	}

	/** 预取虚框文件夹的icon开始 */

	public void preloadStubFolderIcon(List<AppStubFolder> stubFolderList) {
		if(stubFolderList == null || stubFolderList.isEmpty()) {
			return;
		}
		for(AppStubFolder asf:stubFolderList) {
			final AppStubFolder tmp = asf;
			NqLog.i("预取文件夹图标 start id= " + tmp.getFolderId() + " ,name= " + tmp.getName());
			App app = AppStubFolderToApp(tmp);
			new Thread(new PreloadRunnable(mContext, app, new PreloadListener() {
				
				@Override
				public void onErr() {
					setLastPreloadStubFolderTimeM();
				}
				
				@Override
				public void onNoNetwork() {
				}
				
				@Override
				public void onPreloadSucc(App app) {
					setLastPreloadStubFolderTimeM();
					setStubFolderEnable(tmp);
				}
			})).start();
		}
	}


	private long getLastPreloadStubFolderTimeM() {
		return mPreference.getLongValue(
				AppStubFolderPreference.KEY_LAST_PRELOAD_STUB_FOLDER_TIME);
	}


	protected void setLastPreloadStubFolderTimeM() {
		mPreference.setLongValue(
				AppStubFolderPreference.KEY_LAST_PRELOAD_STUB_FOLDER_TIME,
				System.currentTimeMillis());
	}


	private long getLastPreloadStubFolderAppTimeM() {
		return mPreference.getLongValue(
				AppStubFolderPreference.KEY_LAST_PRELOAD_STUB_FOLDER_APP_TIME);
	}

	
	protected void setLastPreloadStubFolderAppTimeM() {
		mPreference.setLongValue(
				AppStubFolderPreference.KEY_LAST_PRELOAD_STUB_FOLDER_APP_TIME,
				System.currentTimeMillis());
	}

	/** 预取虚框文件夹的icon成功 */
	
	protected void setStubFolderEnable(AppStubFolder asf) {
		if(asf == null) {
			return;
		}
		NqLog.e("预取文件夹图标ok id= " + asf.getFolderId() + " ,name= " + asf.getName());
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		ContentValues values = new ContentValues();
		ContentProviderOperation.Builder b = null;
		values.put(AppStubFolderCacheTable.STUBFOLDER_ENABLE, 1);
		b = ContentProviderOperation.newUpdate(AppStubFolderCacheTable.TABLE_URI)
			  .withValues(values)
			  .withSelection(AppStubFolderCacheTable.STUBFOLDER_ID + " =?",
					  new String[]{String.valueOf(asf.getFolderId())});
		ops.add(b.build());

		try {
			mContext.getContentResolver().applyBatch(AppStubFolderCacheTable.DATA_AUTHORITY, ops);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	private App AppStubFolderToApp(AppStubFolder asf) {
		App app = new App();
		app.setStrIconUrl(asf.getIconUrl());
		app.setStrIconPath(asf.getIconPath());
		
		return app;
	}

	/** wifi情况下预取未成功的资源 */
	
	public void onWifiEnabled() {
		if(!isAppStubFolderEnable())
			return;
		NqLog.e("onWifiEnabled wifi情况下预取未成功的资源");
		long lastTime = getLastPreloadStubFolderTimeM();
		if((System.currentTimeMillis() - lastTime) > 1000 * 10) {
			List<AppStubFolder> stubFolderlist = getStubFolderCache(0);
			if(stubFolderlist != null && !stubFolderlist.isEmpty()) {
				preloadStubFolderIcon(stubFolderlist);
			}
		}
		
		long lastAppTime = getLastPreloadStubFolderAppTimeM();
		if((System.currentTimeMillis() - lastAppTime) > 1000 * 10) {
			// 获取全部需要预取的虚框应用资源
			List<AppStubFolder> stubFolderlist = getAppStubFolderListFromCache(1, 0 ,false);
			if(stubFolderlist != null && !stubFolderlist.isEmpty()) {
				preloadStubFolderAppIcon(stubFolderlist);
			}
		}
		
	}

	private volatile HashMap<Long,AppStubFolder> obnj=new HashMap<Long, AppStubFolder>();
	/**获取虚框文件夹缓存
	 * 
	 * @param from 0:获取APP_CACHE中的缓存 ,给桌面展示用
	 * 1:获取STUBFOLDER_APP_CACHE中的缓存 ,做预取资源和转移资源用
	 * @param stubFolderID 如果为0表示搜索全部虚框文件夹数据
	 * @param enable: true:所有预取成功的数据   false：所有未预取成功的数据
	 * */
	public List<AppStubFolder> getAppStubFolderListFromCache(int from, long stubFolderID, boolean enable) {
		List<AppStubFolder> appStubFolderList = null;
		Cursor cursor = null;
		try {
			cursor = getCursor(from, stubFolderID, enable);
			if (cursor != null && cursor.getCount() > 0) {
				appStubFolderList = new ArrayList<AppStubFolder>();
				AppStubFolder asf = new AppStubFolder();
				AppStubFolder tmp = null;
				ArrayList<AppStub> listAppStubs = new ArrayList<AppStub>();
				long lastFolderId = -1;
				AppManager am = AppManager.getInstance(mContext);
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
					long folderID = cursor.getLong(cursor.getColumnIndex(AppStubFolderAppCacheTable.APP_COLUMN));
					App app = am.cursorToApp(cursor);
					if(folderID != lastFolderId) {
						lastFolderId = folderID;
						tmp = asf;
						if(tmp != null && tmp.getFolderId() != 0) {
							appStubFolderList.add(tmp);
						}
						// 处理每个虚框文件夹的第一条
						asf = null;
						listAppStubs.clear();
						asf = getAppStubFolderInfo(folderID);
						if(asf == null) {
							if(obnj!=null){
								asf = obnj.get(stubFolderID);
								if(asf != null) {
									cacheStubFolder(Collections.singletonList(asf));
								} else {
									NqLog.e("appstubfolder cache is null ouch!");
									asf = new AppStubFolder();
									asf.setFolderId(stubFolderID);
									asf.setName("AppStubFolder");
								}
							}
							
						}
						AppStub as = new AppStub();
						as.setApp(app);
						listAppStubs.add(as);
					} else {
						AppStub as = new AppStub();
						as.setApp(app);
						listAppStubs.add(as);
					}
					asf.setStubList(listAppStubs);
				}
				// 处理最后一个虚框文件夹的最后一个资源
				tmp = asf;
				if(tmp != null && tmp.getFolderId() != 0) {
					appStubFolderList.add(tmp);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(cursor != null) {
					cursor.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return appStubFolderList;
	}
	
	private Cursor getCursor(int from, long stubFolderID, boolean enable) {
		ContentResolver contentResolver = mContext.getContentResolver();
		Cursor cursor = null;
		try {
			if(from == 1) {
				if(stubFolderID != 0) {
					cursor = contentResolver.query(AppStubFolderAppCacheTable.TABLE_URI, null,
							AppStubFolderAppCacheTable.APP_COLUMN + "=" + stubFolderID
									+ " AND " + AppStubFolderAppCacheTable.APP_STUB_ENABLE
									+ "=?",
							new String[] { enable ? "1" : "0" },
							AppStubFolderAppCacheTable.APP_COLUMN + " asc, " + AppStubFolderAppCacheTable._ID + " asc");
				} else {
					cursor = contentResolver.query(AppStubFolderAppCacheTable.TABLE_URI, null,
							AppStubFolderAppCacheTable.APP_STUB_ENABLE + "=?", new String[]{enable ? "1" : "0"},
							AppStubFolderAppCacheTable.APP_COLUMN + " asc, " + AppStubFolderAppCacheTable._ID + " asc");
				}
			} else if(from == 0) {
				if(stubFolderID != 0) {
					cursor = contentResolver.query(AppStubFolderResTable.TABLE_URI, null,
							AppStubFolderResTable.APP_SOURCE_TYPE
									+ "="
									+ AppStubFolderConstants.STORE_MODULE_TYPE_STUB_FOLDER
									+ " AND " + AppStubFolderResTable.APP_COLUMN
									+ "=?",
							new String[] { String.valueOf(stubFolderID) },
							AppStubFolderResTable.APP_COLUMN + " asc, " + AppStubFolderResTable._ID + " asc");
							
				} else {
					cursor = contentResolver.query(AppStubFolderResTable.TABLE_URI, null,
							AppStubFolderResTable.APP_SOURCE_TYPE
									+ "="
									+ AppStubFolderConstants.STORE_MODULE_TYPE_STUB_FOLDER,
							null,
							AppStubFolderResTable.APP_COLUMN + " , " + AppStubFolderResTable._ID);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cursor;
	}

	
	private AppStubFolder getAppStubFolderInfo(long folderID) {
		AppStubFolder asf = null;
		Cursor cursor = null;
		try {
			ContentResolver contentResolver = mContext.getContentResolver();
			cursor = contentResolver.query(AppStubFolderCacheTable.TABLE_URI,
					null, AppStubFolderCacheTable.STUBFOLDER_ID + "=?",
					new String[] { String.valueOf(folderID) },
					AppStubFolderCacheTable._ID + " asc");

			if (cursor != null && cursor.moveToNext()) {
				asf = cursorToAppStubFolder(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (cursor != null) {
					cursor.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return asf;
	}

	
	private boolean isAppStubFolderEnable() {
		return mPreference.getBooleanValue(
				AppStubFolderPreference.KEY_STUB_FOLDER_ENABLE);
	}

	
	private Object mLock = new Object();
	
	private ConcurrentHashMap<Long, Integer> mapAll = new ConcurrentHashMap<Long, Integer>();
	
	private ConcurrentHashMap<Long, Integer> mapGo = new ConcurrentHashMap<Long, Integer>();
	
	private ConcurrentHashMap<Long, Integer> mapSucc = new ConcurrentHashMap<Long, Integer>();

	/** 预取虚框文件夹的App icon开始 */
	public void preloadStubFolderAppIcon(List<AppStubFolder> stubFolderList) {
		if(stubFolderList == null || stubFolderList.isEmpty()) {
			return;
		}
		for(AppStubFolder asf:stubFolderList) {
			if(asf.getStubList() == null || asf.getStubList().isEmpty())
				continue;
			final long folderID = asf.getFolderId();
			mapAll.put(folderID, asf.getStubList().size());
			mapGo.put(folderID, 0);
			mapSucc.put(folderID, 0);
			final AppStubFolder tmp = asf;
			for(AppStub appStub:asf.getStubList()) {
				final App app = appStub.getApp();
				NqLog.i("预取应用图标 start id= " + asf.getFolderId() + " ,name= " + asf.getName()
						+" appid=" + app.getStrId() + " ,appname=" + app.getStrName());
				PreloadRunnable preload = new PreloadRunnable(mContext, app, new PreloadListener() {
					
					@Override
					public void onErr() {
						setLastPreloadStubFolderAppTimeM();
						NqLog.i("预取应用图标失败  id= " + tmp.getFolderId() + " ,name= " + tmp.getName()
								+" appid=" + app.getStrId() + " ,appname=" + app.getStrName());
						setStubFolderAppFailed(folderID, app);
						checkPreloadAppStatus(folderID, false);
					}
					
					@Override
					public void onNoNetwork() {
						checkPreloadAppStatus(folderID, false);
					}
					
					@Override
					public void onPreloadSucc(App app) {
						setLastPreloadStubFolderAppTimeM();
						NqLog.i("预取应用图标 ok id= " + tmp.getFolderId() + " ,name= " + tmp.getName()
								+" appid=" + app.getStrId() + " ,appname=" + app.getStrName());
						setStubFolderAppEnable(folderID, app);
						checkPreloadAppStatus(folderID, true);
					}
				});
				preload.setB_LOAD_ICON(true);
				preload.setB_LOAD_PREVIEW(false);
				preload.setB_LOAD_AUDIO(false);
				new Thread(preload).start();
			}
		}
	}
	
	protected void checkPreloadAppStatus(long folderID, boolean succ) {
		synchronized (mLock) {
			if(mapGo.get(folderID) != null) {
				int oldCount = mapGo.get(folderID);
				mapGo.put(folderID, oldCount+1);
				
				if(succ) {
					int oldSuccCount = mapSucc.get(folderID);
					mapSucc.put(folderID, oldSuccCount+1);
				}
				
				checkCallBackTime(folderID);
			}
		}
	}

	
	private void checkCallBackTime(long folderID) {
		if (mapGo.get(folderID) == mapAll.get(folderID)) {
			if (mapSucc.get(folderID) == mapAll.get(folderID)) {
				 NqLog.i("虚框文件夹的资源全部预取成功, id= " + folderID);

				// 更新虚框文件夹APP_CACHE资源列表
				moveCacheFromStubFolderAppToAppCache(folderID);
				createAppStubFolder(folderID);
			}
		}
	}

	/**将虚框文件夹预取成功的资源转移到appCache表中*/
	
	private void moveCacheFromStubFolderAppToAppCache(long folderID) {
		deleteStubResInAppCache(folderID);
		List<App> apps = getAppListFromStubAppCache(folderID, true);
		insertAppCache(folderID, apps);
	}

	private void cancelSilentDownload(long folderId) {
		List<App> applist = getAppList(folderId);
		for(App app:applist) {
			if(app != null) {
				NqLog.i("cancelSilentDownload [folderId=" + folderId + ", app: "
        				+ app.getStrId() + "/" + app.getStrName()
        				+ "]");
				EventBus.getDefault().post(new CancelSilentDownloadEvent(app.getStrId()));
			}
		}
	}

	/**
     * 删除除了已下载未安装以外状态的应用信息
     * @param folderID
     * @return
     */
	private boolean deleteStubResInAppCache(long folderID) {
		Cursor c = null;
        try{
            c = mContext.getContentResolver().query(AppStubFolderResTable.TABLE_URI, null,
            		AppStubFolderResTable.APP_SOURCE_TYPE + "=" + AppStubFolderConstants.STORE_MODULE_TYPE_STUB_FOLDER
                            + " AND "
                            + AppStubFolderResTable.APP_COLUMN + "=?", new String[]{String.valueOf(folderID)}, null);
            while(c != null && c.moveToNext()){
                App app = AppManager.getInstance(mContext).cursorToApp(c);
                long id = c.getInt(c.getColumnIndex(AppStubFolderResTable._ID));
                mContext.getContentResolver().delete(AppStubFolderResTable.TABLE_URI,
                        AppStubFolderResTable._ID + " = ?", new String[]{String.valueOf(id)});
            }
        }finally {
            if(c != null){
                c.close();
            }
        }

		return true;
	}

	/**
     * 删除应用信息
     * @param folderID
     * @return
     */
	private boolean deleteStubResInAppCacheNew(long folderID) {
		try {
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			Builder b = null;
			// 清除resCache中缓存
			b = ContentProviderOperation.newDelete(
					AppStubFolderResTable.TABLE_URI).withSelection(
							AppStubFolderResTable.APP_SOURCE_TYPE
									+ "="
									+ AppStubFolderConstants.STORE_MODULE_TYPE_STUB_FOLDER
									+ " AND "
									+ AppStubFolderResTable.APP_COLUMN + "=?",
							new String[] { String.valueOf(folderID) });
			ops.add(b.build());

			ContentResolver contentResolver = mContext.getContentResolver();

			contentResolver.applyBatch(AppStubFolderResTable.DATA_AUTHORITY,ops);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	private boolean deleteStubResInStubFolderCache(long folderID) {
		try {
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			Builder b = null;
			// 清除appCache中缓存
			b = ContentProviderOperation.newDelete(
					AppStubFolderCacheTable.TABLE_URI).withSelection(
							AppStubFolderCacheTable.STUBFOLDER_ID + "=" + folderID, null);
			ops.add(b.build());

			ContentResolver contentResolver = mContext.getContentResolver();

			contentResolver.applyBatch(AppStubFolderCacheTable.DATA_AUTHORITY,ops);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}
	
	
	private boolean deleteStubResInStubFolderAppCache(long folderID) {
		try {
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			Builder b = null;
			// 清除appCache中缓存
			b = ContentProviderOperation.newDelete(
					AppStubFolderAppCacheTable.TABLE_URI).withSelection(
							AppStubFolderAppCacheTable.APP_COLUMN + "=" + folderID, null);
			ops.add(b.build());

			ContentResolver contentResolver = mContext.getContentResolver();

			contentResolver.applyBatch(AppStubFolderAppCacheTable.DATA_AUTHORITY,ops);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	/** 从StubFolderApp缓存中获取列表 */
	
	private List<App> getAppListFromStubAppCache(long folderID, boolean enable) {
		List<App> appList = new ArrayList<App>();
		ContentResolver contentResolver = mContext.getContentResolver();
		Cursor cursor = null;
		AppManager am = AppManager.getInstance(mContext);
		try {
			cursor = contentResolver.query(AppStubFolderAppCacheTable.TABLE_URI, null,
					AppStubFolderAppCacheTable.APP_COLUMN + "=" + folderID + " AND "
					+ AppStubFolderAppCacheTable.APP_STUB_ENABLE + "=?", new String[]{enable ? "1" : "0"},
					AppStubFolderAppCacheTable._ID + " asc");
			if (cursor != null && cursor.getCount() > 0) {
				int index = 1;
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
					App app = am.cursorToApp(cursor);
					if(app != null) {
						NqLog.i("预取成功的文件夹 " + folderID + " 资源有 " + index
								+ " id=" + app.getStrId()
								+ " ,name=" + app.getStrName()
								+ " ,sourceType=" + app.getIntSourceType());
						appList.add(app);
						index++;
					}
				}
				NqLog.i("预取成功的文件夹 " + folderID + " 资源有 " + appList.size() + " 个");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(cursor != null) {
					cursor.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return appList;
	}

	private boolean isAppAlreadyInAppCache(long folderId, App app){
        boolean result = false;
        Cursor c = null;
        try{
            c = mContext.getContentResolver().query(AppStubFolderResTable.TABLE_URI, null,
            		AppStubFolderResTable.APP_SOURCE_TYPE + "=" + AppStubFolderConstants.STORE_MODULE_TYPE_STUB_FOLDER
                            + " AND "  + AppStubFolderResTable.APP_COLUMN + "=? AND " + AppStubFolderResTable.APP_ID + "  = ?",
                    new String[]{String.valueOf(folderId),app.getStrId()}, null);
            if(c != null && c.moveToNext()){
                result = true;
            }
        }finally {
            if(c != null){
                c.close();
            }
        }

        return result;
    }

	private boolean insertAppCache(long folderID, List<App> appList) {
		if (appList == null || appList.isEmpty()) {
			return false;
		}
		try {
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			Builder b = null;
			ContentResolver contentResolver = mContext.getContentResolver();
			
			int index = 0;
			for (App app:appList) {
				if (app != null) {
					if(!isAppAlreadyInAppCache(folderID,app)){
                        NqLog.e("转移到APP_CACHE " + index + ": id= " + app.getStrId() + " ,name= " + app.getStrName());
                        ContentValues values = appToContentValues(folderID, app);
                        b = ContentProviderOperation.newInsert(AppStubFolderResTable.TABLE_URI).withValues(values);
                        ops.add(b.build());
                        index++;
                    }
				}
			}
			contentResolver.applyBatch(AppStubFolderResTable.DATA_AUTHORITY,ops);
			NqLog.e("转移到APP_CACHE " + index +  " 个资源");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/** 预取虚框文件夹App的icon成功 */
	
	protected void setStubFolderAppEnable(long folderID, App app) {
		if (app == null) {
			return;
		}
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		ContentValues values = new ContentValues();
		ContentProviderOperation.Builder b = null;
		values.put(AppStubFolderAppCacheTable.APP_STUB_ENABLE, 1);
		b = ContentProviderOperation.newUpdate(AppStubFolderAppCacheTable.TABLE_URI)
			  .withValues(values)
			  .withSelection(AppStubFolderAppCacheTable.APP_COLUMN + "=" + folderID + " AND "
					  + AppStubFolderAppCacheTable.APP_ID + "=?",new String[]{app.getStrId()});
		ops.add(b.build());
		try {
			mContext.getContentResolver().applyBatch(AppStubFolderAppCacheTable.DATA_AUTHORITY, ops);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** 预取虚框文件夹App的icon失败 */
	
	protected void setStubFolderAppFailed(long folderID, App app) {
		int failedCount = getAppStubPreloadFailedCount(folderID, app);
		// 这是第（failedCount + 1）次失败
		if(failedCount > 0) {
			deleteAppStub(folderID, app, false);
		} else {
			increasePreloadFailCount(folderID, app, failedCount + 1);
		}
	}
	
	
	private int getAppStubPreloadFailedCount(long folderID, App app) {
		if (app == null) {
			return 0;
		}
		int count = 0;
		ContentResolver contentResolver = mContext.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = contentResolver.query(AppStubFolderAppCacheTable.TABLE_URI,
					new String[] { AppStubFolderAppCacheTable.APP_PRELOAD_FAIL },
					AppStubFolderAppCacheTable.APP_COLUMN + "=" + folderID + " AND "
							+ AppStubFolderAppCacheTable.APP_ID + "=?",
					new String[] { app.getStrId() }, AppStubFolderAppCacheTable._ID + " desc");
			if(cursor != null && cursor.moveToNext()) {
				count = cursor.getInt(cursor.getColumnIndex(AppStubFolderAppCacheTable.APP_PRELOAD_FAIL));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (cursor != null) {
					cursor.close();
				}
			} catch (Exception e) { 
				e.printStackTrace();
			}
		}

		return count;
	}

	
	/**
	 * 删除StubAppCache中的缓存
	 * 
	 * @param needAll
	 *			true:根据包名全删除 false：根据resID删除
	 */
	private boolean deleteAppStub(long folderID, App app, boolean needAll) {
		if(app == null) {
			return false;
		}
		NqLog.e("虚框预取失败2次。删除之 folderID= " + folderID + " ,app= " + app.toString());
		try {
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			Builder b = null;
			String selection = AppStubFolderAppCacheTable.APP_COLUMN + "=" + folderID + " AND "
					+ AppStubFolderAppCacheTable.APP_ID + "=?";
			if(needAll) {
				selection = AppStubFolderAppCacheTable.APP_COLUMN + "=" + folderID + " AND "
						+ AppStubFolderAppCacheTable.APP_PACKAGENAME + "=?";
			}
			b = ContentProviderOperation.newDelete(
					AppStubFolderAppCacheTable.TABLE_URI).withSelection(
							selection,
							new String[]{needAll ? app.getStrPackageName() : app.getStrId()});
			ops.add(b.build());

			ContentResolver contentResolver = mContext.getContentResolver();

			contentResolver.applyBatch(AppStubFolderAppCacheTable.DATA_AUTHORITY,ops);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	
	private void increasePreloadFailCount(long folderID, App app, int preloadFailedCount) {
		if(app == null) {
			return;
		}
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		ContentValues values = new ContentValues();
		ContentProviderOperation.Builder b = null;
		values.put(AppStubFolderAppCacheTable.APP_PRELOAD_FAIL, preloadFailedCount);
		b = ContentProviderOperation.newUpdate(AppStubFolderAppCacheTable.TABLE_URI)
			  .withValues(values)
			  .withSelection(AppStubFolderAppCacheTable.APP_COLUMN + "=" + folderID + " AND "
					  + AppStubFolderAppCacheTable.APP_ID + "=?",new String[]{app.getStrId()});
		ops.add(b.build());
		try {
			mContext.getContentResolver().applyBatch(AppStubFolderAppCacheTable.DATA_AUTHORITY, ops);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/** 处理新安装应用的逻辑 */
	public void onNewAppInstalled(String packageName) {
		if(TextUtils.isEmpty(packageName)) {
			return;
		}
		// 场景二：用户下载完成 该文件夹 所有资源，启动更新。
		// 查询含有这个应用的虚框文件夹
		List<String> folderIdList = getFolderIdList(packageName);
		if(folderIdList == null || folderIdList.isEmpty()) {
			return;
		}
		// 看每个文件夹有没有只有这一条资源
		boolean needUpdate = false;
		for(String fid:folderIdList) {
			int count = getAppCount(fid);
			if(count == 1) {
				needUpdate = true;
			} else {
				folderIdList.remove(fid);
			}
		}

		//这里不删除 否则无法弹出运行的通知栏提示
//		deleteInstalledApp(packageName);

		if(needUpdate) {
			getStubFolderFromServer(1, folderIdList, null);
		}
	}

	private void deleteInstalledApp(String packageName) {
		if (TextUtils.isEmpty(packageName)) {
			return;
		}
		try {
			//---------------------
			// delete preloaded app cache
			//---------------------
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			Builder b = null;
			ContentResolver contentResolver = mContext.getContentResolver();
			
			b = ContentProviderOperation
					.newDelete(AppStubFolderResTable.TABLE_URI)
					.withSelection(
							AppStubFolderResTable.APP_SOURCE_TYPE
									+ "="
									+ AppStubFolderConstants.STORE_MODULE_TYPE_STUB_FOLDER
									+ " AND " + AppStubFolderResTable.APP_PACKAGENAME
									+ "=?", new String[] { packageName });
			ops.add(b.build());

			contentResolver.applyBatch(AppStubFolderResTable.DATA_AUTHORITY,ops);
			//---------------------
			// delete app cache
			//---------------------
			ArrayList<ContentProviderOperation> ops1 = new ArrayList<ContentProviderOperation>();
			Builder b1 = null;
			
			b1 = ContentProviderOperation.newDelete(AppStubFolderAppCacheTable.TABLE_URI)
					.withSelection(AppStubFolderAppCacheTable.APP_PACKAGENAME + "=?", new String[]{packageName});
			ops1.add(b1.build());

			contentResolver.applyBatch(AppStubFolderAppCacheTable.DATA_AUTHORITY,ops);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	private int getAppCount(String fid) {
		int count = 0;
		Cursor cursor = null;
		try {
			ContentResolver contentResolver = mContext.getContentResolver();
			cursor = contentResolver.query(AppStubFolderResTable.TABLE_URI, null,
					AppStubFolderResTable.APP_SOURCE_TYPE + "="
							+ AppStubFolderConstants.STORE_MODULE_TYPE_STUB_FOLDER
							+ " AND " + AppStubFolderResTable.APP_COLUMN + "=?",
					new String[] { fid },
					AppStubFolderResTable.APP_COLUMN + " asc");
			if (cursor != null) {
				count = cursor.getCount();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(cursor != null) {
					cursor.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return count;
	}

	
	private List<String> getFolderIdList(String packageName) {
		if (TextUtils.isEmpty(packageName)) {
			return null;
		}
		List<String> folderIdList = null;
		Cursor cursor = null;
		try {
			ContentResolver contentResolver = mContext.getContentResolver();
			cursor = contentResolver.query(AppStubFolderResTable.TABLE_URI, null,
					AppStubFolderResTable.APP_SOURCE_TYPE + "="
							+ AppStubFolderConstants.STORE_MODULE_TYPE_STUB_FOLDER
							+ " AND " + AppStubFolderResTable.APP_PACKAGENAME + "=?",
					new String[] { packageName }, AppStubFolderResTable.APP_COLUMN);
			if (cursor == null || cursor.getCount() == 0) {
				return null;
			}
			
			folderIdList = new ArrayList<String>();
			while (cursor.moveToNext()) {
				String folderID = cursor.getString(cursor.getColumnIndex(AppStubFolderResTable.APP_COLUMN));
				if(!folderIdList.contains(folderID)) {
					folderIdList.add(folderID);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(cursor != null) {
					cursor.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return folderIdList;
	}

	public void onEvent(ConnectivityChangeEvent event) {
		if (NetworkUtils.isWifi(mContext)) {
			onWifiEnabled();
		}
	}

	public void onEvent(PackageAddedEvent event){
		onNewAppInstalled(event.getPackageName());
	}

	/** 获取到新缓存后，保存配置 */
	public void onEvent(AppStubFolderPrefChangeEvent event) {
		mPreference.setIntValue(
				AppStubFolderPreference.KEY_STUB_FOLDER_SHOW_COUNT,
				event.getShowCount());
		mPreference.setBooleanValue(
				AppStubFolderPreference.KEY_STUB_FOLDER_OPRATABLE,
				event.isFolderOpratable());
		mPreference.setBooleanValue(
				AppStubFolderPreference.KEY_STUB_FOLDER_RES_DELETABLE,
				event.isFolderResDeletable());
	}

	public void onEvent(GetAppStubFolderListSuccEvent event) {
		if(event.getAppStubFolderResp() == null) {
			return;
		}
		mPreference.setStubFolderFirstEnabled(true);
		TVirtualFolderAppResp appStubFolderResp = event.getAppStubFolderResp();
		final ArrayList<AppStubFolder> stubFolderList = new ArrayList<AppStubFolder>(
				appStubFolderResp.virtualFolderResources.size());
		int size = 0;
		NqLog.i("onEvent GetAppStubFolderListSuccEvent size = " + appStubFolderResp.virtualFolderResources.size());
		for(TVirtualFolder t:appStubFolderResp.virtualFolderResources){
			AppStubFolder sf = vfResourceToStubFolder(t);
			if(sf != null && CollectionUtils.isNotEmpty(sf.getStubList())) {
				size += sf.getStubList().size();
				stubFolderList.add(sf);
			}
		}
		
		if(stubFolderList.size() > 0){
            if(event.scene == 0){ //新创建
                cacheStubFolder(stubFolderList);
            }else if(event.scene == 1){ //更新资源
                updateFolderInfo(stubFolderList.get(0));
            }
			cacheStubFolderApp(stubFolderList);
		}
		if(NetworkUtils.isWifi(mContext)) {
			final int size2 = size;
			PriorityExecutor.getExecutor().submit(new Runnable() {
				
				@Override
				public void run() {
					prefetchStubFolderAppApk(stubFolderList, size2);
				}
			});
			
			preloadStubFolderIcon(stubFolderList);
			preloadStubFolderAppIcon(stubFolderList);
		}
	}

    private void prefetchStubFolderAppApk(
			ArrayList<AppStubFolder> stubFolderList, int size) {
    	if(stubFolderList == null || stubFolderList.isEmpty()) {
			return;
		}
    	PrefetchEvent prefetchEvent = new PrefetchEvent();
    	ArrayList<PrefetchRequest> requests = new ArrayList<PrefetchRequest>(size);
    	for(AppStubFolder asf:stubFolderList) {
			if(asf.getStubList() == null || asf.getStubList().isEmpty())
				continue;
			for(AppStub appStub:asf.getStubList()) {
				final App app = appStub.getApp();
				NqLog.i("wifi静默下载虚框 start id= " + asf.getFolderId() + " ,name= " + asf.getName()
						+" appid=" + app.getStrId() + " ,appname=" + app.getStrName());
				if(app.isSlientDownload()){
					String resId = app.getStrId();
					int type = PrefetchRequest.TYPE_APK;
					String url = app.getStrAppUrl();
					String path = app.getStrAppPath();
					long mSize = app.getLongSize();
					PrefetchRequest req = new PrefetchRequest(prefetchEvent,resId,
							type, url, path, mSize);
					req.setPackageName(app.getStrPackageName());
					requests.add(req);
				}
			}
    	}
    	
		prefetchEvent.setFeatureId(113);
		prefetchEvent.setSourceType(AppStubFolderConstants.STORE_MODULE_TYPE_STUB_FOLDER);
		prefetchEvent.setRequests(requests);
		
        EventBus.getDefault().post(prefetchEvent);
	}

	private void updateFolderInfo(AppStubFolder folder){
        ContentValues value = new ContentValues();
        value.put(AppStubFolderCacheTable.STUBFOLDER_SHOW_COUNT,0);
		value.put(AppStubFolderCacheTable.STUBFOLDER_DELETABLE,folder.isDeletable());
        mContext.getContentResolver().update(AppStubFolderCacheTable.TABLE_URI,value,
                AppStubFolderCacheTable.STUBFOLDER_ID + " = ?",new String[]{String.valueOf(folder.getFolderId())});
    }
	
	public void onEvent(GetAppStubFolderListFailedEvent event){
		//TODO 需要做什么处理？
	}
}
