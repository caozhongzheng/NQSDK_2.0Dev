package com.nqmobile.livesdk.modules.association;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;

import com.nq.interfaces.launcher.TAppResource;
import com.nq.interfaces.launcher.TAssociateResp;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.net.Listener;
import com.nqmobile.livesdk.commons.prefetch.event.PrefetchEvent;
import com.nqmobile.livesdk.commons.prefetch.event.PrefetchRequest;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.association.features.AssocSwitchFeature;
import com.nqmobile.livesdk.modules.association.network.AssociationRecommendProtocal.GetAssociationFailedEvent;
import com.nqmobile.livesdk.modules.association.network.AssociationRecommendProtocal.GetAssociationSuccessEvent;
import com.nqmobile.livesdk.modules.association.network.AssociationRecommendServiceFactory;
import com.nqmobile.livesdk.modules.association.table.AssociationRecommendTable;
import com.nqmobile.livesdk.modules.association.table.AssociationTable;
import com.nqmobile.livesdk.utils.CommonMethod;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.StringUtil;

/**
 * 关联推荐类
 * @author liujiancheng
 *
 */
public class AssociationManager extends AbsManager {
	private static final ILogger NqLog = LoggerFactory.getLogger(AssociationModule.MODULE_NAME);
	
    private Context mContext;
    private static AssociationManager mInstance;
    public static final long CACHE_ONE_DAY = 24L * 60 * 60 * 1000;//24小时
    public static final long MIN_APP_CLICK_TIMES = 3;//推荐应用的最小被点击次数
	public static final String STORE_LOCAL_PATH_APP = "/LiveStore/app/";
	
    private String mLastVersionTag = "";
    private Map<String, AssociationAppInfo> mAssociationRecommendMap;
    private Map<String, Long> mAppClickTimeMap;
    AssociationPreference mHelper;
    
    public AssociationManager(Context context) {
        super();
        mContext = context;
        mAssociationRecommendMap = new ConcurrentHashMap<String, AssociationAppInfo>();
        mAppClickTimeMap = new ConcurrentHashMap<String, Long>();
        mHelper = AssociationPreference.getInstance();   
        
        //读取db中的关联推荐信息到内存hashmap
        refreshMemoryCache();
    }

    public synchronized static AssociationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AssociationManager(context.getApplicationContext());
        }
        return mInstance;
    }
    
	@Override
	public void init() {
		EventBus.getDefault().register(this);
	}
    
    public static class AssociationAppInfo {
    	public App mAppInfo;//被推荐资源信息
    	public long mShowTimes;//被展示次数
    	public long mLastShowTimes;//上次展示时间
    }
   
    /**
     * 返回指定包名的关联推荐应用接口，桌面调用
     * @param packageName：推荐的包名
     * @return
     */
    public App getAssociationApps(String packageName) {
    	//如果关联推荐开关关闭，返回null
    	if (mHelper.getBooleanValue(AssociationPreference.KEY_ASSOCIATION_ENABLE) == false) {
    		return null;
    	}
    	
    	return getRecommendAppsFromMemoryCache(packageName);
    }
    
    /**
     * 从服务器获取关联推荐应用信息，定期联网调用
     */
    public void getAssciationInfoFromServer() {
    	AssociationRecommendServiceFactory.getService().getAssociationApp(null);
    }
    
    /**
     * 从内存cache中找关联推荐资源，返回指定包名的关联推荐应用
     * @param packageName：推荐的包名
     * @return
     */
    private App getRecommendAppsFromMemoryCache(String packageName) {
    	if (packageName == null) {
    		return null;
    	}
    	
    	// 检查应用的点击次数是否到达3次
    	if (!checkAppClickTimes(packageName)) {
    		return null;
    	}
    	
    	// 非wifi下返回空
    	if (!NetworkUtils.isWifi(mContext)) {
    		return null;
    	}
    	
    	// 间隔天数内无推荐行为
    	if (isIntervalReady() == false) {
    		return null;
    	}
    	
    	App app = null;
    	long lastShowTime;
    	
    	NqLog.i("mAssociationRecommendMap: size =" + mAssociationRecommendMap.size());
    	// print each value for debug
//    	Iterator<Entry<String, AssociationAppInfo>> iter = mAssociationRecommendMap.entrySet().iterator();
//    	while (iter.hasNext()) {
//    		Entry<String, AssociationAppInfo> i = iter.next();
//    		String key = i.getKey();
//    		AssociationAppInfo val = i.getValue();
//    		NqLog.i("mAssociationRecommendMap: key =" + key);
//    		NqLog.i("mAssociationRecommendMap: val =" + val.mAppInfo.getStrId());
//    	}
    	
    	if (mAssociationRecommendMap.containsKey(packageName)) {    		
	    	AssociationAppInfo associationApp = mAssociationRecommendMap.get(packageName);
	
	    	if (associationApp != null) {
	    		if (!isInstalled(associationApp.mAppInfo.getStrPackageName())) {
			        NqLog.i("package:" +packageName  + " recommend -----> package:" + associationApp.mAppInfo.getStrPackageName()
			        			+ "resId:" + associationApp.mAppInfo.getStrId() + "downloadpath:" + associationApp.mAppInfo.getStrAppPath());
		    		app = associationApp.mAppInfo;
		    		app.setIntSourceType(AssociationActionConstants.APP_SOURCE_TYPE);
		        	//成功渠道推荐信息后刷新数据库缓存
		        	lastShowTime = System.currentTimeMillis();
		        	associationApp.mShowTimes++;
		        	associationApp.mLastShowTimes = lastShowTime;
		
		        	mHelper.setLongValue(AssociationPreference.KEY_ASSOCIATION_LAST_SHOWTIME, lastShowTime);
			        updateAssociationCache(associationApp);
	    		}
	        }
    	}
    	return app;
    }
    
    /**
     * 接收关联推荐信息存到db并预取incon
     * @return
     */
    public void receiveAssociationInfo(TAssociateResp resp) {
        try {
        	if (resp != null) {
        		saveAssociationRecommendToCache(resp);
        	}
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
        	getAppRessourceIcon();
        }
    }

    
    /**
     * 刷新内存中的关联推荐信息cache
     * @return
     */
    public void refreshMemoryCache() {
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
		        ContentResolver contentResolver = mContext.getContentResolver();
		        Cursor cursor = null;
		        
		        try {
			        cursor = contentResolver.query(AssociationTable.TABLE_URI, 
			        			new String[]{"DISTINCT " + AssociationTable.ASSOCIATION_PACKAGENAME},
			                	null, null, null);
			        if (cursor == null) {
			        	return;
			        } 
			        
			        mAssociationRecommendMap.clear();
		            while (cursor.moveToNext()) {
		            	String packageName = cursor.getString(cursor.getColumnIndex(AssociationTable.ASSOCIATION_PACKAGENAME));
		            	AssociationAppInfo associationApp = getRecommendFromDbCache(packageName);
		            	if (associationApp != null) {
		            		mAssociationRecommendMap.put(packageName, associationApp);
		            	}
		            }
		        } catch (Exception e) {
		            NqLog.e(e);
		        } finally {
		            if (cursor != null)
		                cursor.close();
		        }
			}
			
		}).start();
    }
    
    /**
     * 从数据库cache中得到指定包名的关联推荐信息。
     * @param packageName：推荐的包名
     * @return
     */
    private AssociationAppInfo getRecommendFromDbCache(String packageName) {
    	AssociationAppInfo recommend = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;
        
//        int showInterval = mHelper.getIntValue(PreferenceDataHelper.KEY_MAX_ASSOCIATION_SHOW_INTERVAL);

        try {
        	NqLog.i("getRecommendFromDbCache:packageName=" + packageName);
            cursor = contentResolver.query(AssociationTable.TABLE_URI, null,
            		AssociationTable.ASSOCIATION_PACKAGENAME + "=?",
                    new String[]{packageName}, null);
            if (cursor == null) {
                return null;
            }            
            recommend = getRecommendFromMultipeApps(cursor);
            
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        
        if (recommend != null) {
	        NqLog.i("recommend:" + recommend.mAppInfo.getStrPackageName() + "resid:" + recommend.mAppInfo.getStrId());
	        NqLog.i("recommend:mLastShowTimes=" + recommend.mLastShowTimes + " recommend.mShowTimes=" + recommend.mShowTimes);
	        NqLog.i("recommend:getStrAppPath=" + recommend.mAppInfo.getStrAppPath());
        }
        return recommend;
    }    
    
    /**
     * 从数据库cache中从多个关联应用中找出满足条件的应用：推荐次数最少和上次推荐时间最早。
     * @param cursor
     * @return
     */    
    private AssociationAppInfo getRecommendFromMultipeApps(Cursor cursor) {
    	AssociationAppInfo recommend = null;
        
        long minShowTimes = mHelper.getIntValue(AssociationPreference.KEY_MAX_ASSOCIATION_SHOWTIMES);;
        long minLastShowTime = System.currentTimeMillis();
        
        // 取符合条件的关联推荐应用
        while (cursor.moveToNext()) {
            String resId = cursor.getString(cursor.getColumnIndex(AssociationTable.ASSOCIATION_APPRESOURCE));
            String adWords = cursor.getString(cursor.getColumnIndex(AssociationTable.ASSOCIATION_RECOMMENDED_INTRODUCTION));
            
        	AssociationAppInfo app = getAppByResourceId(resId);
            if (app != null) {
            	long lastShowTime = app.mLastShowTimes;
            	long showTimes = app.mShowTimes;

            	if (showTimes < minShowTimes) {
            		minShowTimes = showTimes;
            		minLastShowTime = lastShowTime;
            		recommend = app;
            		recommend.mAppInfo.setStrDescription(adWords);
            	} else if (showTimes == minShowTimes) {
            		if (lastShowTime < minLastShowTime) {
            			minLastShowTime = lastShowTime;
            			recommend = app;
            			recommend.mAppInfo.setStrDescription(adWords);
            		}
            	}	                    
            }
        }
        
        return recommend;        
    }
    
    /**
     * 根据资源id等参数得到具体关联推荐资源
     *
     * @param resourceId 资源的id，
     * @param 
     */
    private AssociationAppInfo getAppByResourceId(String resourceId) {
        Cursor cursor = null;
        AssociationAppInfo app = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        try {
        	int maxShowTimes = mHelper.getIntValue(AssociationPreference.KEY_MAX_ASSOCIATION_SHOWTIMES);
            cursor = contentResolver.query(AssociationRecommendTable.TABLE_URI,
                    null, AssociationRecommendTable.ASSOCIATION_RECOMMENDED_RESID + " =?" + " AND " + AssociationRecommendTable.ASSOCIATION_SHOWTIMES + " < " + maxShowTimes, 
                    new String[]{resourceId}, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                String packageName = cursor.getString(cursor.getColumnIndex(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_PACKAGENAME));
                String iconPath = StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_ICON)));
                
                // icon未取到或者被推荐的应用已经安装，则不推荐
                if (iconPath.isEmpty() || isInstalled(packageName)) {
                	app = null;
                } else {
                	app = cursorToApp(cursor);
                }
            } else {
                app = null;
            }
        } catch (Exception e) {
            NqLog.e(e);            
        } finally {
            try {
                if (cursor != null)
                    cursor.close();
            } catch (Exception e) {
                NqLog.e(e);
            }
        }

        return app;
    }    
    
    /**
     * 将数据库cache里面cursor对象转换App
     *
     * @param cursor
     * @return
     */
    private AssociationAppInfo cursorToApp(Cursor cursor) {
    	AssociationAppInfo associationApp = new AssociationAppInfo();
    	App app = new App();
    	
    	String resId = StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_RESID)));    	
        app.setStrId(resId);
        app.setStrAppPath(getAppDownloadPath(resId));
        
        app.setStrName(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_NAME))));
        app.setStrPackageName(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_PACKAGENAME))));
        app.setStrIconUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_ICONURL))));
        app.setStrIconPath(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_ICON)))); 
        app.setStrAppUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_LINKURL))));
        app.setIntDownloadActionType(cursor.getInt(cursor.getColumnIndex(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_DOWNLOADTYPE)));
        app.setIntEffect(cursor.getInt(cursor.getColumnIndex(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_EFFECTID)));
        app.setLongDownloadCount(cursor.getLong(cursor.getColumnIndex(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_DOWNLOADCOUNT)));
        app.setLongSize(cursor.getLong(cursor.getColumnIndex(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_SIZE)));
        app.setFloatRate(cursor.getFloat(cursor.getColumnIndex(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_SCORE)));
        app.setIntClickActionType(cursor.getInt(cursor.getColumnIndex(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_ACTIONCLICKTYPE)));
        
        associationApp.mAppInfo = app;
        associationApp.mShowTimes = cursor.getInt(cursor.getColumnIndex(AssociationRecommendTable.ASSOCIATION_SHOWTIMES));
        String lastTime = cursor.getString(cursor.getColumnIndex(AssociationRecommendTable.ASSOCIATION_LASTSHOWTIME));
	
        long lastShowTime;
        if (lastTime == null || lastTime.isEmpty()) {     
        	lastShowTime = 0;
        } else {
        	lastShowTime= Long.parseLong(lastTime);
        }
        associationApp.mLastShowTimes = lastShowTime;
        return associationApp;
    }
    
    /**
     * 把服务端下发的关联推荐信息缓存进数据库
     */
    public boolean saveAssociationRecommendToCache(TAssociateResp resp) {
    	String versionTag = resp.getVersionTag();

    	if (mLastVersionTag.equalsIgnoreCase(versionTag)) {
    		return true;
    	} else {
    		mLastVersionTag = versionTag;
    	}

    	int interval = resp.getInterval();
    	int maxTimes = resp.getMaxTimes();

    	boolean result = true;
        try {       	            
        	mHelper.setIntValue(AssociationPreference.KEY_MAX_ASSOCIATION_SHOWTIMES, maxTimes);
        	mHelper.setIntValue(AssociationPreference.KEY_MAX_ASSOCIATION_SHOW_INTERVAL, interval);
            
        	Map<String, List<TAppResource>> map = resp.getRelatedApps();

        	NqLog.i("map size =" + map.size());
        	Iterator<Entry<String, List<TAppResource>>> iter = map.entrySet().iterator();
        	ArrayList<TAppResource> tAppList = new ArrayList<TAppResource>();
        	while (iter.hasNext()) {
        		Entry<String, List<TAppResource>> i = iter.next();
        		String key = i.getKey();
        		List<TAppResource> val = i.getValue();
        		saveAppResource(key, val);
        		tAppList.addAll(val);
        	}
        	//发送预取请求
        	sendPrefetchRequest(tAppList);
        } catch (Exception e) {
            NqLog.e(e);
            result = false;
        } finally {
	        // 如果保存成功，写入时间戳
	        if (result == true) {
	        	mHelper.setStringValue(AssociationPreference.KEY_MAX_ASSOCIATION_LAST_VERSIONTAG, versionTag);
	        	//refreshMemoryCache();
	        }	    	
        }
        return result;
    }
        
    private void sendPrefetchRequest(List<TAppResource> appList) {
		PrefetchEvent prefetchEvent = new PrefetchEvent();
		ArrayList<PrefetchRequest> requests = new ArrayList<PrefetchRequest>(appList.size());
		for (TAppResource app : appList) {
			if (app != null && app.isSilentDownload == 1) {
				String resId = app.getResourceId();
				int type = PrefetchRequest.TYPE_APK;
				String url = app.getLinkUrl();
				String path = getAppDownloadPath(resId);
				long size = app.getSize();
				PrefetchRequest req = new PrefetchRequest(prefetchEvent,resId,
						type, url, path, size);
				req.setPackageName(app.getPackageName());
				requests.add(req);
			}
		}
		prefetchEvent.setFeatureId(AssocSwitchFeature.FEATURE);
		prefetchEvent.setSourceType(AssociationActionConstants.APP_SOURCE_TYPE);
		prefetchEvent.setRequests(requests);
        EventBus.getDefault().post(prefetchEvent);
    }
    
    public void getAppRessourceIcon() {
    	new Thread(new IconFetcher(mContext)).start();
    }
    
	private void saveAppResource(String packageNames, List<TAppResource> appList){
		ContentResolver contentResolver = mContext.getContentResolver();
		
		String[] packageList = packageNames.split(",");	

        try {
        	//先保存被推荐的应用信息到数据库
        	saveRecommendAppList(appList);
        	
        	//再保存关联推荐信息到数据库
        	for (String packagename: packageList) {
        		Cursor cursor = null;
                cursor = contentResolver.query(AssociationTable.TABLE_URI,
                        null, AssociationTable.ASSOCIATION_PACKAGENAME + " =? ", new String[]{packagename}, null);
                if (cursor != null && cursor.getCount() > 0) {
                	updateAppAssocation(packagename, cursor, appList);
                } else {
                	saveNewAppAssocation(packagename,appList);
                }
                if (cursor != null)
                    cursor.close();
        	}
		} catch (Exception e) {
			NqLog.e("saveAppResource error " + e.toString());
            e.printStackTrace();
		}
	}
	
	private void saveRecommendAppList(List<TAppResource> appList){
		ContentValues values = null;
		ContentResolver contentResolver = mContext.getContentResolver();
    	ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        try {
        	// 插入AssociationRecommendTable
        	for (TAppResource app: appList) {
        		NqLog.i("app.getResourceId() =" + app.getResourceId());
	            values = new ContentValues();
	            values.put(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_RESID, app.getResourceId());
	    		values.put(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_NAME, app.getName());
	            values.put(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_PACKAGENAME, app.getPackageName());
	    		values.put(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_ICONURL, app.getIcon());
	    		values.put(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_LINKURL, app.getLinkUrl());
	    		values.put(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_DOWNLOADTYPE, app.getDownActionType());
	    		values.put(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_EFFECTID, app.getEffectId());
	    		values.put(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_DOWNLOADCOUNT, app.getDownloadNum());
	    		values.put(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_SIZE, app.getSize());
	    		values.put(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_SCORE, app.getScore());
	    		values.put(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_ACTIONCLICKTYPE, app.getClickActionType());

	    		Cursor cursor = null;
	            cursor = contentResolver.query(AssociationRecommendTable.TABLE_URI,
	                    null, AssociationRecommendTable.ASSOCIATION_RECOMMENDED_RESID + " =? ", new String[]{app.getResourceId()}, null);
	            
	            if (cursor == null || cursor.moveToFirst() == false) {    		    		
		            Builder insert = ContentProviderOperation.newInsert(AssociationRecommendTable.TABLE_URI).withValues(values);
		            ops.add(insert.build());
	            } else {
	                Builder update = ContentProviderOperation.newUpdate(AssociationRecommendTable.TABLE_URI)
	                      .withValues(values)
	                      .withSelection(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_RESID + " =? ", new String[]{app.getResourceId()});
		            ops.add(update.build());	                
	            }
                if (cursor != null)
                    cursor.close();
        	}
        	contentResolver.applyBatch(AssociationRecommendTable.DATA_AUTHORITY,ops);

		} catch (Exception e) {
			NqLog.e("saveRecommendAppList error " + e.toString());
            e.printStackTrace();
		}
	}
	
	private void saveNewAppAssocation(String packageName, List<TAppResource> appList){
		ContentValues values = null;
		ContentResolver contentResolver = mContext.getContentResolver();
    	ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        try {
        	// 插入DataProvider.ASSOCIATION_CACHE_URI
        	for (TAppResource app: appList) {        	
	            values = new ContentValues();
	            values.put(AssociationTable.ASSOCIATION_PACKAGENAME, packageName);
	    		values.put(AssociationTable.ASSOCIATION_APPRESOURCE, app.getResourceId());
	    		values.put(AssociationTable.ASSOCIATION_RECOMMENDED_INTRODUCTION, app.getIntroduction());
	    		NqLog.i("saveNewAppAssocation: packageName = " + packageName +" appResourceId =" + app.getResourceId());
	            Builder insert = ContentProviderOperation.newInsert(AssociationTable.TABLE_URI).withValues(values);
	            ops.add(insert.build());
        	}
            contentResolver.applyBatch(AssociationTable.DATA_AUTHORITY,ops);
		} catch (Exception e) {
			NqLog.e("saveNewAppAssocation error " + e.toString());
            e.printStackTrace();
		}
	}
	
	private void updateAppAssocation(String packageName, Cursor cursor, List<TAppResource> appList){
		ContentValues values = null;
		ContentResolver contentResolver = mContext.getContentResolver();
    	ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
    	
        try {
        	// 对于一个包，删除已有关联推荐中数据库中老的推荐资源
            while (cursor.moveToNext()) {
            	String resId = cursor.getString(cursor.getColumnIndex(AssociationTable.ASSOCIATION_APPRESOURCE));
            	if (!isInAppList(resId, appList)) {
                    Builder delete = ContentProviderOperation.newDelete(AssociationTable.TABLE_URI)
                    		.withSelection(AssociationTable.ASSOCIATION_PACKAGENAME + "=?" + " AND " +
                    				AssociationTable.ASSOCIATION_APPRESOURCE + "=?", new String[]{packageName,resId});
                    ops.add(delete.build());
            	} 
            }
            
            // 添加新的推荐资源
    		for (TAppResource app: appList) {
    			if (!isInAssociationTable(app.getResourceId(), cursor)) {
    	            values = new ContentValues();   
    	    		values.put(AssociationTable.ASSOCIATION_PACKAGENAME, packageName);
    	    		values.put(AssociationTable.ASSOCIATION_APPRESOURCE, app.getResourceId());
    	    		values.put(AssociationTable.ASSOCIATION_RECOMMENDED_INTRODUCTION, app.getIntroduction());
    	            Builder insert = ContentProviderOperation.newInsert(AssociationTable.TABLE_URI).withValues(values);
    	            ops.add(insert.build());
    			} else {
    	            values = new ContentValues();   
    	    		values.put(AssociationTable.ASSOCIATION_RECOMMENDED_INTRODUCTION, app.getIntroduction());
	                Builder update = ContentProviderOperation.newUpdate(AssociationTable.TABLE_URI)
		                      .withValues(values)
		                      .withSelection(AssociationTable.ASSOCIATION_PACKAGENAME + " =?"  + " AND " + AssociationTable.ASSOCIATION_APPRESOURCE + " =?", 
		                    		  new String[]{packageName,app.getResourceId()});
			        ops.add(update.build());
            	}
    		}

            contentResolver.applyBatch(AssociationTable.DATA_AUTHORITY,ops);                	
		} catch (Exception e) {
			NqLog.e("updateAppAssocation error " + e.toString());
            e.printStackTrace();
		}
	}
	
	private boolean isInAppList(String resId,List<TAppResource> appList) {
		for (TAppResource app: appList) {
			if (resId.equalsIgnoreCase(app.getResourceId())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isInAssociationTable(String resId, Cursor cursor) {
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        	String res = cursor.getString(cursor.getColumnIndex(AssociationTable.ASSOCIATION_APPRESOURCE));
        	if (resId.equalsIgnoreCase(res)) {
        		return true;
        	}
        }
		return false;
	}	
	
    /**
     * 检查推荐应用点击次数是否到达3次
     * @param associationApp：推荐的关联应用
     * @return
     */    
    private boolean checkAppClickTimes(String clickAppPackageName) {   	
    	//推荐应用点击次数如果未达3次则不推荐
    	Long appClickTime = mAppClickTimeMap.get(clickAppPackageName);
    	if (appClickTime == null) {
    		appClickTime = 0L;
    	}
		appClickTime++;
		mAppClickTimeMap.put(clickAppPackageName, appClickTime);
   		NqLog.i("package:" + clickAppPackageName + " click times:" + appClickTime);
    	if (appClickTime < MIN_APP_CLICK_TIMES) {  		
    		return false;
    	}    	   	

    	return true;
    }
    
    private boolean isIntervalReady() {
        long lastShowTime = mHelper.getLongValue(AssociationPreference.KEY_ASSOCIATION_LAST_SHOWTIME);
        int showInterval = mHelper.getIntValue(AssociationPreference.KEY_MAX_ASSOCIATION_SHOW_INTERVAL);
        
    	if (System.currentTimeMillis() - lastShowTime < CACHE_ONE_DAY * showInterval) {
    		return false;
    	} else {
    		return true;
    	}
    }
    
    /**
     * 判断被推荐的应用是否已经被安装
     *
     * @param cursor
     * @return
     */
    private boolean isInstalled(String packageName) {
    	PackageInfo packageInfo = null;   
    	boolean ret = false;
    	try {
	         packageInfo = mContext.getPackageManager().getPackageInfo(packageName, 0);
	    } catch (NameNotFoundException e) {
            packageInfo = null;
	    } finally {
	    	if(packageInfo == null){
	    		ret = false;
	    	}else{
	    		ret = true;
	    	}
	    }
    	return ret;
    }
    
    private String getAppDownloadPath(String strId) {    	
        String SDCardPath = CommonMethod.getSDcardPath(mContext);
        if(SDCardPath == null)
        	SDCardPath = CommonMethod.getSDcardPathFromPref(mContext);
        String strAppPath = new StringBuilder().append(SDCardPath).append(STORE_LOCAL_PATH_APP).toString();
        return new StringBuilder().append(strAppPath).append(strId).append(".apk").toString();
    }
    
    /**
     * 推荐成功后更新关联推荐应用缓存
     * @param associationApp：被推荐的应用信息
     * @return
     */
    private void updateAssociationCache(AssociationAppInfo associationApp) {
    	new Thread(new UpdateCacheThread(mContext,associationApp)).start();
    }
    
	class UpdateCacheThread implements Runnable {	
		private AssociationAppInfo mAssociationApp;
		private Context mContext;
		
		public UpdateCacheThread(Context context, AssociationAppInfo associationApp) {
			this.mContext = context;
			this.mAssociationApp = associationApp;
		}
		
		@Override
		public void run() {		            
	        try {		
            	ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                ContentValues values = new ContentValues();
                ContentProviderOperation.Builder b = null;
                
                values.put(AssociationRecommendTable.ASSOCIATION_LASTSHOWTIME, mAssociationApp.mLastShowTimes);
                values.put(AssociationRecommendTable.ASSOCIATION_SHOWTIMES, mAssociationApp.mShowTimes); 
                
                b = ContentProviderOperation.newUpdate(AssociationRecommendTable.TABLE_URI)
                      .withValues(values)
                      .withSelection(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_RESID + " =? ",
                      new String[]{mAssociationApp.mAppInfo.getStrId()});
                ops.add(b.build());
                mContext.getContentResolver().applyBatch(AssociationRecommendTable.DATA_AUTHORITY,ops);

	        } catch (Exception e) {
	            NqLog.e(e);
	        } finally {
	        	refreshMemoryCache();
	        }            
		}

	}
	
    /**
     * 对于关联推荐资源表中icon没有预取到的记录，下载icon到本地
     *
     * @param cursor
     * @return
     */    
	private class IconFetcher implements Runnable {
		private static final int CONNECT_TIMEOUT = 10*1000;
	    private static final int READ_TIMEOUT = 10*1000;

	    private Context mContext;
	    ContentResolver mContentResolver;
	    
		public IconFetcher(Context mContext ){
			this.mContext = mContext.getApplicationContext();
			mContentResolver = mContext.getContentResolver();
		}

		@Override
		public void run() {
	        Cursor cursor = null;
	        try {
	            cursor = mContentResolver.query(AssociationRecommendTable.TABLE_URI, null,
	            		AssociationRecommendTable.ASSOCIATION_RECOMMENDED_ICON + " IS NULL", null, null);
	            if (cursor == null)
	                return;

               	String SDCardPath = CommonMethod.getSDcardPath(this.mContext);
                
	            while (cursor.moveToNext()) {
	            	String resId = cursor.getString(cursor.getColumnIndex(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_RESID));
	            	String iconUrl = cursor.getString(cursor.getColumnIndex(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_ICONURL));
	            	
	            	String strImgPath = new StringBuilder().append(SDCardPath).append(STORE_LOCAL_PATH_APP).toString();
	            	String iconPath = new StringBuilder().append(strImgPath).append(resId).append("_icon").append(StringUtil.getExt(iconUrl)).toString();
		            boolean succ = download(iconUrl, iconPath);
					if (succ) {
						// 更新关联推荐app信息表
						NqLog.i("download icon for resource:" + resId + "succeed!");
						
		            	ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		                ContentValues values = new ContentValues();
		                ContentProviderOperation.Builder b = null;
		                
		                values.put(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_ICON,iconPath);
		                b = ContentProviderOperation.newUpdate(AssociationRecommendTable.TABLE_URI)
		                      .withValues(values)
		                      .withSelection(AssociationRecommendTable.ASSOCIATION_RECOMMENDED_RESID + " =? ", new String[]{resId});
		                ops.add(b.build());
		                mContentResolver.applyBatch(AssociationRecommendTable.DATA_AUTHORITY,ops);						
					} else {
						NqLog.i("download icon for resource:" + resId + "failed!");
					}
	            }
	        } catch (Exception e) {
                NqLog.e(e);
            } finally {
                if (cursor != null)
                    cursor.close();
                
                //刷新缓存
				refreshMemoryCache();
            }		        
		}

		private boolean download(String url, String path) {
			boolean result = false;
			InputStream is = null;

			File file = new File(path);
			if (file.exists()) {
				return true;
			}
			
			HttpURLConnection conn = null;
			try {
				URL iconUrl = new URL(url);
				conn = (HttpURLConnection) iconUrl.openConnection();
				conn.setConnectTimeout(CONNECT_TIMEOUT);
				conn.setReadTimeout(READ_TIMEOUT);
				conn.setInstanceFollowRedirects(true);
				if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					is = conn.getInputStream();
				}
				if (is == null) {
					throw new RuntimeException("stream is null");
				} else {
	                try {
	                    OutputStream os = new FileOutputStream(path);
	                    byte[] buffer = new byte[1024];
	                    int len = 0;
	                    while( (len=is.read(buffer)) != -1){
	                    	os.write(buffer, 0, len);
	                    }
	                    os.close();
	                    is.close();
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	                is.close();
	                result = true;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
			}

			return result;
		}

	}
	
	/**
	 * 关联推荐应用信息获取成功后的事件处理
	 * @param event
	 */
    public void onEvent(GetAssociationSuccessEvent event){
    	TAssociateResp resp = event.getAssociationResp();
        try {
        	if (resp != null) {
        		saveAssociationRecommendToCache(resp);
        	}
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
        	getAppRessourceIcon();
        }
    }
    
    public void onEvent(GetAssociationFailedEvent event){
    	//即使获取关联推荐失败或无新信息，也要尝试取icon，刷新内存hanshmap
    	getAppRessourceIcon();
    }
    
    /**
     * 关联推荐获取监听
     * @author liujiancheng
     * @time 2014-9-16
     */
    public interface AssociationListener extends Listener{
        public void onGetAssociationSucc(TAssociateResp resp);
    }
}
