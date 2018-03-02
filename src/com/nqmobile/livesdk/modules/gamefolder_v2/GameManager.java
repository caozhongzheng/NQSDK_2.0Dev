package com.nqmobile.livesdk.modules.gamefolder_v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import android.text.format.DateUtils;

import com.nqmobile.livesdk.commons.AppConstants;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.concurrent.PriorityExecutor;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.prefetch.event.PrefetchEvent;
import com.nqmobile.livesdk.commons.prefetch.event.PrefetchRequest;
import com.nqmobile.livesdk.commons.receiver.ConnectivityChangeEvent;
import com.nqmobile.livesdk.commons.receiver.PackageAddedEvent;
import com.nqmobile.livesdk.commons.service.PeriodCheckEvent;
import com.nqmobile.livesdk.commons.system.SystemFacadeFactory;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.apptype.AppTypeManager;
import com.nqmobile.livesdk.modules.apptype.event.AppTypeLibUpgradeEvent;
import com.nqmobile.livesdk.modules.apptype.model.AppTypeInfo;
import com.nqmobile.livesdk.modules.apptype.network.GetAppTypeProtocol.GetAppTypeFailedEvent;
import com.nqmobile.livesdk.modules.apptype.network.GetAppTypeProtocol.GetAppTypeSuccessEvent;
import com.nqmobile.livesdk.modules.gamefolder_v2.model.GameCache;
import com.nqmobile.livesdk.modules.gamefolder_v2.model.GameCacheConverter;
import com.nqmobile.livesdk.modules.gamefolder_v2.network.GameFolderV2ServiceFactory;
import com.nqmobile.livesdk.modules.gamefolder_v2.network.GetGameListProtocol.GetGameListFailedEvent;
import com.nqmobile.livesdk.modules.gamefolder_v2.network.GetGameListProtocol.GetGameListSuccessEvent;
import com.nqmobile.livesdk.modules.gamefolder_v2.table.GameCacheTable;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.utils.CollectionUtils;
import com.nqmobile.livesdk.utils.CursorUtils;
import com.nqmobile.livesdk.utils.FileUtil;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.PackageUtils;
import com.nqmobile.livesdk.utils.StringUtil;

public class GameManager extends AbsManager {
    // ===========================================================
    // Constants
    // ===========================================================
	private static final ILogger NqLog = LoggerFactory.getLogger(GameFolderV2Module.MODULE_NAME);
    private static final String ACTION_CREATE_GAME_FOLDER = "com.lqsoft.launcher.action.ADD_GAME_FOLDER";
    
    //旧版在线文件夹桌面ID： -1000 to -2000 for old version. 新版游戏文件夹桌面ID：-5999
    public static final long GAME_FOLDER_ID = -5999L;
    
    public static final int GAME_FOLDER_STATUS_NONE = 0;
    public static final int GAME_FOLDER_STATUS_CREATED = 1;
    public static final int GAME_FOLDER_STATUS_DELETED = 2;
    
    /** store 模块编号 游戏广告*/
    public static final int STORE_MODULE_TYPE_GAME_AD = 5;
    
    private static final String[] WHITELIST_GAME = new String[]{
		"soco.mortalskies",
		"com.soco.veggies",
		"cn.koogame.Fighter",
		"com.koogame.kootank"
		};
    // ===========================================================
    // Fields
    // ===========================================================
    private Context mContext;
    private static GameManager sInstance = new GameManager();
    private GameFolderV2Preference mPreference;
	private boolean mIsWifiOnFolderOpen;
    // ===========================================================
    // Constructors
    // =========================================================== 
    private GameManager() {
        mContext = ApplicationContext.getContext();
        mPreference = GameFolderV2Preference.getInstance();
    }

    public static GameManager getInstance() {
        return sInstance;
    }
    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================
    @Override
    public void init() {
        EventBus.getDefault().register(this);
        requestCreateGameFolder();
        checkMinimumGameCount();
    }
    
    @Override
    public void onDisable() {
		EventBus.getDefault().unregist(this);
	}
    
    // ===========================================================
    // Methods
    // ===========================================================
    /**
     * 只在本地判断是否是游戏
     * 
     * @param packageName
     *            
     */
    public boolean isGameFromLocal(String packageName) {
        if (TextUtils.isEmpty(packageName)){
            return false;
        }

        int appCode = getAppCodeFromLocalAndWhitelist(packageName);

        return isGame(appCode);
    }
    
    private int getAppCodeFromLocalAndWhitelist(String packageName){
    	if (isWhiteListGame(packageName)){
        	return AppTypeInfo.CODE_GAME;
        }
        
        return AppTypeManager.getInstance().getAppCodeFromLocal(packageName);
    }
    
    private boolean isWhiteListGame(String packageName) {
		boolean isGame = false;
		
		if(WHITELIST_GAME.length == 0 || TextUtils.isEmpty(packageName))
			return isGame;
		
		for(int i=0; i<WHITELIST_GAME.length; i++) {
			if(packageName.startsWith(WHITELIST_GAME[i])) {
				isGame = true;
				break;
			}
		}
		return isGame;
	}
    
    private boolean isGame(int appCode){
    	//应用包名 应用:100~199, 游戏:200~255
    	return (appCode >= 200 && appCode <= 255);
    }
    
    /**
     * 尝试创建游戏文件夹
     */
    public void requestCreateGameFolder() {
        if (!allowCreateGameFolder()) {
            return;
        }
        
        sendBroadcast(null);
    }
    
    private void tryAddIntoGameFolder(String packageName) {
    	if (TextUtils.isEmpty(packageName)){
    		return;
    	}
    	
        if (mPreference.getGameFolderStatus() != GAME_FOLDER_STATUS_CREATED) {
            return;
        }
        
        
        int code = getAppCodeFromLocalAndWhitelist(packageName);
        if (code == AppTypeInfo.CODE_UNKNOWN) {//预制库中没有此包名时联网查询类型
                List<String> list = new ArrayList<String>();
                list.add(packageName);
                GameFolderV2GetAppTypeTag tag = new GameFolderV2GetAppTypeTag();
        		tag.localDetectedGames = 0;
        		tag.onGameFolderCreated = false;
                getAppTypeFromServer(list, tag);
        } else if (isGame(code)) {
            sendBroadcast(packageName);
        }
    }
    
    private void getAppTypeFromServer(List<String> packageList, Object tag) {
        if (CollectionUtils.isEmpty(packageList)){
            return;
        } 

        NqLog.i("getAppTypeFromServer: packageList=" + packageList);
        AppTypeManager.getInstance().getAppTypeFromServer(packageList, tag);
    }
    
    private void updateGameFolder(boolean isJustCreated) {	
        if (!isGameFolderExisted()) {
            return;
        }

        List<String> packageList = PackageUtils.getInstalledPackageNames(mContext);
        List<String> needCheck = new ArrayList<String>();
        int localDetectedGames = 0;
		for (String packageName : packageList) {
			int appCode = getAppCodeFromLocalAndWhitelist(packageName);
			if (appCode == AppTypeInfo.CODE_UNKNOWN) {// 预制库中没有此包名时联网查询类型
				needCheck.add(packageName);
			} else if (isGame(appCode)) {
				sendBroadcast(packageName);
				localDetectedGames++;
			}
		}
		
		if (!needCheck.isEmpty() && NetworkUtils.isConnected(mContext)){
			GameFolderV2GetAppTypeTag tag = new GameFolderV2GetAppTypeTag();
			tag.localDetectedGames = localDetectedGames;
			tag.onGameFolderCreated = isJustCreated;
			getAppTypeFromServer(needCheck, tag);
		} else if (isJustCreated) {// no needCheck, but isJustCreated
			StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    GameFolderV2ActionConstants.ACTION_LOG_1711, null, 0, ""+localDetectedGames);
		}
    }

    private void sendBroadcast(String packageName) {
    	NqLog.i("sendBroadcast: " + packageName);
        Intent intent = new Intent(ACTION_CREATE_GAME_FOLDER);
        intent.putExtra("pkgname", packageName);//packageName=null，桌面会创建一个文件夹
        mContext.sendBroadcast(intent);
    }

    public boolean isGameFolderExisted() {
        int iGameStatus = mPreference.getGameFolderStatus();
        if (iGameStatus == GAME_FOLDER_STATUS_CREATED) {
            return true;
        } else {
            return false;
        }
    }
    
    private boolean allowCreateGameFolder() {
        boolean enabled = mPreference.isGameFolderEnabled();
        int iGameStatus = mPreference.getGameFolderStatus();
        if (enabled && iGameStatus == GAME_FOLDER_STATUS_NONE ) {
        	NqLog.i("allowCreateGameFolder:true, enabled="+enabled+",game_folder_status="+iGameStatus);
            return true;
        } else {
        	NqLog.i("allowCreateGameFolder:false, enabled="+enabled+",game_folder_status="+iGameStatus);
            return false;
        }
    }

	private void getGameListFromServer() {
		GameFolderV2ServiceFactory.getService().getGameList(null);
	}
    
    /**
     * 返回游戏广告给外部
     *
     * @param listener
     */
    public void getGameList(final int size,
            final AdvertisementListListener listener) {
        // 查询是否有可用的缓存数据。
        if (isGameNeedShow(size) && mIsWifiOnFolderOpen) {
            List<App> apps = getGameListFromCache(size, true);
            if (apps != null && apps.size() > 0) {
                listener.getAdvertisementListSucc(apps);
                updateGameShowTimeAndCount(apps);
                onDisplayAction(apps);
                return;
            } else {
                listener.onErr();
            }
        } else {
            listener.onErr();
        }
    }

    /**
     * 返回本地游戏广告给外部
     *
     * @param listener
     */
    public void getAllGameList(final AdvertisementListListener listener) {
        // 查询是否有可用的缓存数据。
        if (mIsWifiOnFolderOpen) {
            List<App> apps = getGameListFromCache(-1, true);
            if (apps != null && apps.size() > 0) {
                listener.getAdvertisementListSucc(apps);
                updateGameShowTimeAndCount(apps);
                onDisplayAction(apps);
                return;
            } else {
                listener.onErr();
            }
        } else {
            listener.onErr();
        }
    }
    
    private void onDisplayAction(List<App> apps) {
        if (apps != null && apps.size() > 0) {
            NqLog.i("updatelog, apps.size = " + apps.size());
            for (App app : apps)
                StatManager.getInstance().onAction(
                        StatManager.TYPE_STORE_ACTION,
                        GameFolderV2ActionConstants.ACTION_LOG_1713,
                        app.getStrId(), 0, "");
        }
    }

    private void updateGameShowTimeAndCount(List<App> apps) {
        if (apps == null || apps.isEmpty())
            return;
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        for (App app : apps) {
            ContentValues values = new ContentValues();
            ContentProviderOperation.Builder b = null;
            values.put(GameCacheTable.APP_SHOW_COUNT, (app.getShowCount() + 1));
            long now = SystemFacadeFactory.getSystem().currentTimeMillis();
            values.put(GameCacheTable.APP_SHOW_TIME, now);
            b = ContentProviderOperation
                    .newUpdate(GameCacheTable.TABLE_URI)
                    .withValues(values)
                    .withSelection(GameCacheTable.APP_ID + " = ?",
                            new String[] { app.getStrId() });
            ops.add(b.build());
        }
        try {
            mContext.getContentResolver().applyBatch(
                    GameCacheTable.DATA_AUTHORITY, ops);
        } catch (Exception e) {
            NqLog.e(e);
        }
    }

    /** 是否需要返回桌面游戏文件夹缓存 */
    private boolean isGameNeedShow(int size) {
        int enabled = getAvailableGameListCache(true);
        if (enabled >= size){
        	return true;
        } else {
        	checkMinimumGameCount();
        	return false;
        }
    }

    /** 预加载成功的个数 */
    public int getAvailableGameListCache(boolean enabled) {
        int count = 0;
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = mContext.getContentResolver();
            cursor = contentResolver.query(GameCacheTable.TABLE_URI, null,
                    GameCacheTable.APP_GAME_ENABLE + "=?",
                    new String[] { "1" }, null);
            if (cursor != null) {
                count = cursor.getCount();
            }
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        NqLog.i("getAvailableGameListCashe count=" + count);
        return count;
    }

    /** 游戏缓存的总个数 */
    public int getAllGameListCache() {
        int count = 0;
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = mContext.getContentResolver();
            cursor = contentResolver.query(GameCacheTable.TABLE_URI, null,
                    null, null, null);
            if (cursor != null) {
                count = cursor.getCount();
            }
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
            CursorUtils.closeCursor(cursor);
        }
        return count;
    }

    /**
     * 返回缓存的游戏文件夹推荐列表。
     *
     * @param enabled
     *            true if get enabled game cache, otherwise false
     * @return
     */
    private List<App> getGameListFromCache(int size, boolean enabled) {
    	if (size <= 0) {
    		size = Integer.MAX_VALUE;
    	}
    	
        List<App> listApps = null;

        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver
                    .query(GameCacheTable.TABLE_URI, null, GameCacheTable.APP_GAME_ENABLE + "=?",
                            new String[] { enabled ? "1" : "0" }, GameCacheTable.APP_SHOW_COUNT
                                    + " asc," + GameCacheTable.APP_SHOW_TIME + " asc");
            if (cursor == null || cursor.getCount() == 0) {
                return null;
            }

            listApps = new ArrayList<App>();

            while (cursor.moveToNext() && listApps.size() < size) {
                App app = GameCacheConverter.cursorToApp(cursor);
                if (app != null) {
                	String pkgName = app.getStrPackageName();
                	if (!PackageUtils.isAppInstalled(mContext, pkgName)){
                		listApps.add(app);
                	}
                }
            }
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
            CursorUtils.closeCursor(cursor);
        }
        NqLog.i("getGameListFromCache enabled data? " + enabled);
        return listApps;
    }
    
	private List<GameCache> getAllGameCache() {
		List<GameCache> listApps = null;

		ContentResolver contentResolver = mContext.getContentResolver();
		Cursor cursor = null;
		try {
			cursor = contentResolver.query(GameCacheTable.TABLE_URI, null,
					null, null, null);
			if (cursor != null) {
				listApps = new ArrayList<GameCache>();
				while (cursor.moveToNext()) {
					GameCache app = GameCacheConverter.cursorToApp(cursor);
					if (app != null) {
						listApps.add(app);
					}
				}
			}
		} catch (Exception e) {
			NqLog.e(e);
		} finally {
			CursorUtils.closeCursor(cursor);
		}

		return listApps;
	}

    


    /**
     * 返回缓存的游戏广告包名
     */
    private Set<String> getCachedGamePkgNames() {
        Set<String> result = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(GameCacheTable.TABLE_URI,
                    new String[] { GameCacheTable.APP_PACKAGENAME }, null, null,
                    GameCacheTable._ID + " desc");
            if (cursor != null && cursor.getCount() > 0) {
                result = new HashSet<String>();
                while(cursor.moveToNext()){
                    String pkgName = StringUtil.nullToEmpty(cursor.getString(cursor
                            .getColumnIndex(GameCacheTable.APP_PACKAGENAME)));
                    result.add(pkgName);
                }
            }
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
            CursorUtils.closeCursor(cursor);
        }
        
        return result;
    }

    /**
     * 应用列表存入游戏文件夹缓存库
     *
     * @param apps
     * @return
     */
    private boolean cacheApps(List<GameCache> apps) {
        if (apps == null || apps.isEmpty()){
            return false;
        }
        
        boolean result = false;
        try {
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            ContentResolver contentResolver = mContext.getContentResolver();
            Builder b = ContentProviderOperation.newDelete(GameCacheTable.TABLE_URI);
            ops.add(b.build());

            long now = SystemFacadeFactory.getSystem().currentTimeMillis();
            for (GameCache app : apps) {
                app.setLongLocalTime(now);
                app.setIntSourceType(STORE_MODULE_TYPE_GAME_AD );
                ContentValues values = GameCacheConverter.appToContentValues(app);
                b = ContentProviderOperation.newInsert(GameCacheTable.TABLE_URI)
                        .withValues(values);
                ops.add(b.build());
            }

            contentResolver.applyBatch(GameCacheTable.DATA_AUTHORITY, ops);
            result = true;
        } catch (Exception e) {
            NqLog.e(e);
        }
        
        return result;
    }

    /**
     * 预取icon，缩略图和音频文件
     * */
    public void preLoad(List<App> apps, boolean bLoadImage, boolean bLoadAudio) {
        for (final App app : apps) {
			PreloadListener listener = new PreloadListener() {

				@Override
				public void onErr() {
					setLastPreloadGameTimeM();
					setGameFailed(app);
				}

				@Override
				public void onNoNetwork() {

				}

				@Override
				public void onPreloadSucc(App app) {
					setLastPreloadGameTimeM();
					setGameEnable(app);
				}
			};
			Runnable runable = new PreloadRunnable(mContext, app, listener);
			PriorityExecutor.getExecutor().submit(runable);
        }
    }

    /** 预取失败 */
    protected void setGameFailed(App a) {
        int failedCount = getAppPreloadFailedCount(a);
        NqLog.i("preLoad failed app " + a.getStrName()
                + ",now failedCount=" + failedCount);
        // 这是第（failedCount + 1）次失败
        if (failedCount > 0) {
            deleteGameFromCache(a.getStrPackageName());
        } else {
            increasePreloadFailCount(a, failedCount + 1);
        }
    }

    private void increasePreloadFailCount(App app, int preloadFailedCount) {
        NqLog.i("increasePreloadFailCount " + app.getStrName()
                + ",new failedCount=" + preloadFailedCount);
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentValues values = new ContentValues();
        ContentProviderOperation.Builder b = null;
        values.put(GameCacheTable.APP_PRELOAD_FAIL, preloadFailedCount);
        b = ContentProviderOperation
                .newUpdate(GameCacheTable.TABLE_URI)
                .withValues(values)
                .withSelection(GameCacheTable.APP_ID + " = ?",
                        new String[] { app.getStrId() });
        ops.add(b.build());
        try {
            mContext.getContentResolver().applyBatch(
                    GameCacheTable.DATA_AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    private int getAppPreloadFailedCount(App a) {
        int result = 0;
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver
                    .query(GameCacheTable.TABLE_URI,
                            new String[] { GameCacheTable.APP_PRELOAD_FAIL },
                            GameCacheTable.APP_ID + "=?",
                            new String[] { a.getStrId() }, GameCacheTable._ID
                                    + " desc");
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                result = cursor.getInt(cursor
                        .getColumnIndex(GameCacheTable.APP_PRELOAD_FAIL));
            }
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
            CursorUtils.closeCursor(cursor);
        }
        
        return result;
    }

    /** 预取成功 */
    protected void setGameEnable(App app) {
        NqLog.i("preload success " + app.getStrName());
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentValues values = new ContentValues();
        ContentProviderOperation.Builder b = null;
        values.put(GameCacheTable.APP_GAME_ENABLE, 1);
        b = ContentProviderOperation
                .newUpdate(GameCacheTable.TABLE_URI)
                .withValues(values)
                .withSelection(GameCacheTable.APP_ID + " = ?",
                        new String[] { app.getStrId() });
        ops.add(b.build());
        try {
            mContext.getContentResolver().applyBatch(
                    GameCacheTable.DATA_AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    private int deleteGameFromCache(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return 0;
        }
        
        int result = 0;
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(GameCacheTable.TABLE_URI, null,
                    GameCacheTable.APP_PACKAGENAME + "=?",
                    new String[] { packageName }, GameCacheTable._ID + " desc");
            if (cursor == null || cursor.getCount() == 0){
                return result;
            }

            while (cursor.moveToNext()) {
                App app = GameCacheConverter.cursorToApp(cursor);
                if (app != null) {
                    FileUtil.delFile(app.getStrIconPath());
                    FileUtil.delFile(app.getArrPreviewPath());
                    FileUtil.delFile(app.getStrAudioPath());
                    FileUtil.delFile(app.getStrVideoPath());
                }
            }
            
			result = contentResolver.delete(GameCacheTable.TABLE_URI,
					GameCacheTable.APP_PACKAGENAME + "=?",
					new String[] { packageName });
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
            CursorUtils.closeCursor(cursor);
        }
        
        return result;
    }

    private long getLastPreloadGameTimeM() {
        return mPreference
                .getLongValue(GameFolderV2Preference.KEY_LAST_PRELOAD_GAME_TIME);
    }

    protected void setLastPreloadGameTimeM() {
    	long now = SystemFacadeFactory.getSystem().currentTimeMillis();
		mPreference.setLongValue(
				GameFolderV2Preference.KEY_LAST_PRELOAD_GAME_TIME, now);
    }

    /** 打开游戏文件夹时检查是否过期了 10天 以及 全部都展示了3次 */
    private void checkCacheOnGameFolderOpen() {
		if (GameFolderV2Policy.isGameCacheExpired()
				|| isExceedGameMaxDisplayTimes()) {
			getGameListFromServer();
		}
    }

    private boolean isExceedGameMaxDisplayTimes() {
        boolean result = false;
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;
        try {
            int maxDisplayTimes = GameFolderV2Policy.getGameMaxDisplayTimes();
            cursor = contentResolver.query(GameCacheTable.TABLE_URI, null,
                    GameCacheTable.APP_SHOW_COUNT + "<" + maxDisplayTimes, null, GameCacheTable._ID
                            + " desc");
            if (cursor == null || cursor.getCount() == 0) {
                result = true;
            }
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
            CursorUtils.closeCursor(cursor);
        }
        
        return result;
    }

        
    
    public void onEvent(ConnectivityChangeEvent event) {
        if (NetworkUtils.isWifi(mContext)) {
            if (!isGameFolderExisted())
                return;
            NqLog.i("onWifiEnabled wifi情况下预取未成功的资源");
            long lastTime = getLastPreloadGameTimeM();
            long now = SystemFacadeFactory.getSystem().currentTimeMillis();
            if ((now - lastTime) > 1000 * 10) {
                List<App> appList = getGameListFromCache(-1, false);
                if (appList != null && !appList.isEmpty()) {
                    preLoad(appList, true, true);
                }
            }
        }
    }

    public void onEvent(PackageAddedEvent event) {
    	int iGameStatus = mPreference.getGameFolderStatus();
        if (iGameStatus != GAME_FOLDER_STATUS_CREATED ){
        	NqLog.i("PackageAddedEvent: isGameStatus="+iGameStatus);
        	return;
        }
    	
        String packageName = event.getPackageName();
        // 先创建，后删除缓存，否则错失判断是游戏
        tryAddIntoGameFolder(packageName);
        int deleted = deleteGameFromCache(packageName);
        
		if (deleted > 0) {
			checkMinimumGameCount();
		}
    }
    
    private void checkMinimumGameCount(){
    	int all = getAllGameListCache();
		if (all < GameFolderV2Policy.getMinimumGameCount()) {
			getGameListFromServer();
		}
    }

    public void onEvent(PeriodCheckEvent event) {
//        if (GameFolderV2Policy.isExceedFrequency()) {
//            getGameListFromServer();
//        }
    }

    public void onEvent(GetGameListSuccessEvent event) {
    	NqLog.i("GetGameListSuccessEvent");
        List<GameCache> apps = event.getApps();
        
        if (apps == null || apps.isEmpty()) {
            return;
        }

        refreshCache(apps);
        postGetAppTypeEvent(apps);
        prefetchGames(apps);
    }

    private void prefetchGames(List<GameCache> apps) {
		PrefetchEvent prefetchEvent = new PrefetchEvent();
		ArrayList<PrefetchRequest> requests = new ArrayList<PrefetchRequest>(apps.size());
		for (App app : apps) {
			if (app == null) {
				continue;
			}
			NqLog.d("get game=" + app.toString());
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
					.setSourceType(AppConstants.STORE_MODULE_TYPE_GAME_AD);
			prefetchEvent.setRequests(requests);

			EventBus.getDefault().post(prefetchEvent);
		}
    }
    
	private void refreshCache(List<GameCache> apps) {
		Set<String> cachedGamePkgNames = getCachedGamePkgNames();
        if (cachedGamePkgNames == null) {
            cachedGamePkgNames = new HashSet<String>();
        }

        Map<String, GameCache> needCacheAppsMap = new HashMap<String, GameCache>(apps.size());
        List<GameCache> needCacheApps = new ArrayList<GameCache>(apps.size());
		// STEP1: 过滤掉已安装的
        for (GameCache app : apps) {
        	String pkgName = app.getStrPackageName();
			if (!PackageUtils.isAppInstalled(mContext, pkgName)) {
				needCacheAppsMap.put(app.getStrId(), app);
				needCacheApps.add(app);
			}
        }
        
        // STEP2: 服务端更新资源的情况
        List<GameCache> oldApps = getAllGameCache();
        for (GameCache oldApp : oldApps) {
        	GameCache newApp = needCacheAppsMap.get(oldApp.getStrId());
        	if (newApp != null){
        		newApp.setGameEnable(oldApp.getGameEnable());
        		newApp.setShowCount(oldApp.getShowCount());
        		newApp.setShowTime(oldApp.getShowTime());
        		newApp.setPreloadFail(oldApp.getPreloadFail());
        	} else {//不需要保留的资源
        		FileUtil.delFile(oldApp.getStrIconPath());
                FileUtil.delFile(oldApp.getArrPreviewPath());
                FileUtil.delFile(oldApp.getStrAudioPath());
                FileUtil.delFile(oldApp.getStrVideoPath());
        	}
        }
        
		if (needCacheApps.size() > 0) {
			boolean cached = cacheApps(needCacheApps);

			// 游戏文件夹广告预取icon
			if (cached && NetworkUtils.isWifi(mContext)) {
				List<App> tempList = new ArrayList<App>(needCacheApps.size());
				for(GameCache app : needCacheApps){
					tempList.add(app);
				}
				preLoad(tempList, true, true);
			}
		}
	}
	
	private void postGetAppTypeEvent(List<GameCache> apps) {
		List<AppTypeInfo> appTypeList = new ArrayList<AppTypeInfo>();
		
        for (GameCache app : apps) {
        	String pkgName = app.getStrPackageName();
        	AppTypeInfo appType = new AppTypeInfo();
        	appType.setPackageName(pkgName);
        	appType.setCode(200);
        	appTypeList.add(appType);
        }
        
		EventBus.getDefault().post(new GetAppTypeSuccessEvent(appTypeList, null));
	}

    public void onEvent(GetGameListFailedEvent event) {
        // TODO 需要做什么处理？
    }
    
    public void onEvent(GetAppTypeSuccessEvent event) {
        List<AppTypeInfo> apps = event.getAppTypeInfos();
        if (apps == null || apps.isEmpty()) {
            return;
        }
        
        Object obj = event.getTag();
        if (obj instanceof GameFolderV2GetAppTypeTag){
        	NqLog.i("GetAppTypeSuccessEvent: GameFolderV2GetAppTypeTag");
        	int serverDetectedGames = 0;
            for (AppTypeInfo info : apps) {
            	String pkgName = info.getPackageName();
                if (isGame(info.getCode()) && PackageUtils.isAppInstalled(mContext, pkgName)) {
                	sendBroadcast(pkgName);
                	serverDetectedGames++;
                }
            }
            
        	GameFolderV2GetAppTypeTag tag = (GameFolderV2GetAppTypeTag)obj;
        	if (tag.onGameFolderCreated){
        		int totalGames = tag.localDetectedGames + serverDetectedGames;
        		StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                        GameFolderV2ActionConstants.ACTION_LOG_1711, null, 0, ""+totalGames);
        	}
        }
    }
    
    public void onEvent(GetAppTypeFailedEvent event) {
    	Object obj = event.getTag();
        if (obj instanceof GameFolderV2GetAppTypeTag){
        	GameFolderV2GetAppTypeTag tag = (GameFolderV2GetAppTypeTag)obj;
        	if (tag.onGameFolderCreated){
        		int totalGames = tag.localDetectedGames;
        		StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                        GameFolderV2ActionConstants.ACTION_LOG_1711, null, 0, ""+totalGames);
        	}
        }
    }
    
    public void onEvent(AppTypeLibUpgradeEvent event){
    	//updateGameFolder();//需求变更：更新预置库后，不更新游戏文件夹
    }
    
	/**
	 * action： 0 创建 1 打开 2 删除 3关闭游戏文件夹中推荐 4 准备打开  5  升级旧版游戏文件夹
	 *        21	删除游戏文件夹的二次确认弹框出现
     * 		  22	删除游戏文件夹二次确认弹框删除按钮点击
     * 		  23	删除游戏文件夹二次确认弹框取消按钮点击
     * 		  24	删除游戏文件夹的二次确认弹框消失
	 */
    public void onFolderAction(int action) {
        if (action == 0) {
        	NqLog.i("GAME_FOLDER_STATUS_CREATED");
            mPreference.setGameFolderStatus(GAME_FOLDER_STATUS_CREATED);
    		updateGameFolder(true);
        } else if (action == 1) {
        	if (mIsWifiOnFolderOpen) {
        		checkCacheOnGameFolderOpen();
        	}
            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    GameFolderV2ActionConstants.ACTION_LOG_1712, null, 1, mIsWifiOnFolderOpen ? "1"  : "0");
        } else if (action == 2) {
            mPreference.setGameFolderStatus(GAME_FOLDER_STATUS_DELETED);
            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    GameFolderV2ActionConstants.ACTION_LOG_1717, null, 0, null);
        } else if (action == 3) {
            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    GameFolderV2ActionConstants.ACTION_LOG_1718, null, 0, null);
        } else if (action == 4) {
        	mIsWifiOnFolderOpen = NetworkUtils.isWifi(mContext);
        } else if (action == 21) {
            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    GameFolderV2ActionConstants.ACTION_LOG_1721, null, 0, null);
        } else if (action == 22) {
            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    GameFolderV2ActionConstants.ACTION_LOG_1722, null, 1, null);
        } else if (action == 23) {
            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    GameFolderV2ActionConstants.ACTION_LOG_1723, null, 1, null);
        } else if (action == 24) {
            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    GameFolderV2ActionConstants.ACTION_LOG_1724, null, 0, null);
        } else if (action == 5) {//5 删除旧版游戏文件夹，升级为新版游戏文件夹
            mPreference.setGameFolderStatus(GAME_FOLDER_STATUS_NONE);
            new Thread(new Runnable(){

				@Override
				public void run() {
					try {
						Thread.sleep(15 * DateUtils.SECOND_IN_MILLIS);
						requestCreateGameFolder();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				}
            	
            }).start();
        }
    }
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
    private class GameFolderV2GetAppTypeTag {
    	boolean onGameFolderCreated;
    	int localDetectedGames;    	
    }
}
