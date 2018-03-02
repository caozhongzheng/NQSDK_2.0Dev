package com.nqmobile.livesdk.modules.categoryfolder;

import android.content.*;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.text.format.DateUtils;
import com.nqmobile.livesdk.commons.ApplicationContext;
import com.nqmobile.livesdk.commons.db.DataProvider;
import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.image.ImageListener;
import com.nqmobile.livesdk.commons.image.ImageManager;
import com.nqmobile.livesdk.commons.log.ILogger;
import com.nqmobile.livesdk.commons.log.LoggerFactory;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.commons.prefetch.event.PrefetchEvent;
import com.nqmobile.livesdk.commons.prefetch.event.PrefetchRequest;
import com.nqmobile.livesdk.commons.receiver.ConnectivityChangeEvent;
import com.nqmobile.livesdk.commons.service.PeriodCheckEvent;
import com.nqmobile.livesdk.commons.ui.StoreMainActivity;
import com.nqmobile.livesdk.modules.app.App;
import com.nqmobile.livesdk.modules.app.AppManager;
import com.nqmobile.livesdk.modules.appstub.AppStubManager;
import com.nqmobile.livesdk.modules.appstub.model.AppStub;
import com.nqmobile.livesdk.modules.apptype.model.AppTypeInfo;
import com.nqmobile.livesdk.modules.apptype.network.GetAppTypeProtocol;
import com.nqmobile.livesdk.modules.categoryfolder.model.RecommendApp;
import com.nqmobile.livesdk.modules.categoryfolder.network.CategoryFolderFactory;
import com.nqmobile.livesdk.modules.categoryfolder.network.GetRecommendAppsProtocol;
import com.nqmobile.livesdk.modules.categoryfolder.table.RecommendAppTable;
import com.nqmobile.livesdk.modules.stat.StatManager;
import com.nqmobile.livesdk.modules.storeentry.StoreEntry;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.PackageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Rainbow on 2014/12/11.
 */
public class CategoryFolderManager extends AbsManager{

    // ===========================================================
    // Constants
    // ===========================================================

    private static final ILogger NqLog = LoggerFactory.getLogger(CategoryFolderModule.MODULE_NAME);

    public static final int[] ALL_CATEGORY = {
            CategoryFolderConstants.CATEGORY_103,
            CategoryFolderConstants.CATEGORY_106,
            CategoryFolderConstants.CATEGORY_114,
            CategoryFolderConstants.CATEGORY_117,
            CategoryFolderConstants.CATEGORY_119,
            CategoryFolderConstants.CATEGORY_120,
            CategoryFolderConstants.CATEGORY_122,
            CategoryFolderConstants.CATEGORY_124,
            CategoryFolderConstants.CATEGORY_200
    };

    private static final int PRE_LOAD_ICON_SIZE = 4;
    private static final long MIN_CACHE_TIME = DateUtils.DAY_IN_MILLIS;
    private static final String KEY_FROM_CATEGORY_FODLER = "from_category_folder";
    private static final String VALUE_CATEGORY_FODLER = "category_folder";
    // ===========================================================
    // Fields
    // ===========================================================

    private static CategoryFolderManager mInstance;
    private Context mContext;
    private AppManager mManager;

    private boolean mLoading;

    // ===========================================================
    // Constructors
    // ===========================================================

    private CategoryFolderManager() {
        mContext = ApplicationContext.getContext();
        mManager = AppManager.getInstance(mContext);
        EventBus.getDefault().register(this);
    }

    public synchronized static CategoryFolderManager getInstance() {
        if (mInstance == null) {
            mInstance = new CategoryFolderManager();
        }
        return mInstance;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public void init() {
        NqLog.i("CategoryFolderManager init");
        EventBus.getDefault().register(this);
    }

    // ===========================================================
    // Methods
    // ===========================================================

    public void getRecommendAppsByCategory(int category, int size,boolean byUser,RecommendAppsListener listener){
        NqLog.i("getRecommendAppsByCategory category="+category+" size="+size+" listener="+listener.getClass());
        long lasttime = CategoryFolderPreference.getInstance().getLongValue(CategoryFolderPreference.KEY_LAST_GET_RECOMMEND_APP_TIME);
        if(System.currentTimeMillis() - lasttime > MIN_CACHE_TIME && byUser){
            NqLog.i("byUser > 24 hour,get from server!");
            getRecommendAppsSingle(category, size, listener);
            return;
        }

        if (getCategoryCacheSize(category) > 0) {
            List<RecommendApp> list = getRecommentAppFromLocal(category, size);
            if (list.size() > 0) {
                NqLog.i("local have apps size=" + list.size());
                listener.onSuccess(list);
                for (RecommendApp app : list) {
                    StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                            CategoryFolderConstants.ACTION_LOG_2903, app.mApp.getStrId(), 0, "");
                }
            } else {
                NqLog.i("local app all installed!");
                listener.onErr();
            }
        } else { //缓存中没有资源
            if(getCategorySize(category) > 0){
                listener.onErr(); //缓存中有资源 但是可能全是虚框了
            }else{
                if(byUser){
                    getRecommendAppsSingle(category, size, listener); //缓存中没有资源 用户主动触发刷新 联网获取
                }else{
                    listener.onErr();
                }
            }
        }
    }

    private void getRecommendAppsSingle(int category,int size,RecommendAppsListener listener){
        NqLog.i("getRecommendAppsSingle category="+category+" size="+size);
        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(category);
        GetRecommendAppTag tag = new GetRecommendAppTag();
        tag.category = list;
        tag.listener = listener;
        tag.size = size;
        tag.single = true;
        CategoryFolderFactory.getService().getRecommendAppsByCategory(tag);
    }

    private void getAppCategoryRecommendAps(){
        NqLog.i("getAppCategoryRecommendAps");
        if(mLoading){
            NqLog.i("alread loading,return!");
        }

        mLoading = true;
        List<Integer> list = new ArrayList<Integer>();
        for(int i : ALL_CATEGORY){
            list.add(i);
        }
        GetRecommendAppTag tag = new GetRecommendAppTag();
        tag.category = list;
        tag.listener = null;
        tag.size = 0;
        tag.single = false;
        CategoryFolderFactory.getService().getRecommendAppsByCategory(tag);
    }

    private int getCategorySeq(int category){
        Cursor c = null;
        int result = -1;
        try{
            c = mContext.getContentResolver().query(RecommendAppTable.TABLE_URI,null,
                    RecommendAppTable.APP_CATEGORY + " = ? AND " + RecommendAppTable.APP_IS_STUB + " = 0",
                    new String[]{String.valueOf(category)},null);
            if(c != null && c.moveToNext()){
                result = c.getInt(c.getColumnIndex(RecommendAppTable.APP_SEQ));
            }
        }finally {
            if(c != null){
                c.close();
            }
        }

        NqLog.i("getCategorySeq category="+category+" result="+result);
        return result;
    }

    private void updateCategorySeq(int category,long seq){
        NqLog.i("updateCategorySeq category="+category+" seq="+seq);
        ContentValues value = new ContentValues();
        value.put(RecommendAppTable.APP_SEQ,seq);
        mContext.getContentResolver().update(RecommendAppTable.TABLE_URI,value,
                RecommendAppTable.APP_CATEGORY + " = ? AND " + RecommendAppTable.APP_IS_STUB + " = 0",
                new String[]{String.valueOf(category)});
    }

    private int getCategoryCacheSize(int category){
        Cursor c = null;
        int result = -1;
        try{
            c = mContext.getContentResolver().query(RecommendAppTable.TABLE_URI,null,
                    RecommendAppTable.APP_CATEGORY + " = ? AND " + RecommendAppTable.APP_IS_STUB + " IN (0,2)",
                    new String[]{String.valueOf(category)},null);
            if(c != null){
                result = c.getCount();
            }
        }finally {
            if(c != null){
                c.close();
            }
        }

        NqLog.i("getCategoryCacheSize category="+category+" size="+result);
        return result;
    }

    private int getCategorySize(int category){
        Cursor c = null;
        int result = -1;
        try{
            c = mContext.getContentResolver().query(RecommendAppTable.TABLE_URI,null,
                    RecommendAppTable.APP_CATEGORY + " = ?",
                    new String[]{String.valueOf(category)},null);
            if(c != null){
                result = c.getCount();
            }
        }finally {
            if(c != null){
                c.close();
            }
        }

        NqLog.i("getCategorySize category="+category+" size="+result);
        return result;
    }

    private long getMinId(int category){
        long result = -1;
        Cursor c = null;
        try{
            c = mContext.getContentResolver().query(RecommendAppTable.TABLE_URI,null,RecommendAppTable.APP_CATEGORY + " = ?",
                    new String[]{String.valueOf(category)},"_id ASC");
            if(c != null && c.moveToNext()){
                result = c.getLong(c.getColumnIndex(RecommendAppTable._ID));
            }
        }finally {
            if(c != null){
                c.close();
            }
        }
        return result;
    }

    private void flagRecommendApp(List<Long> list){
        for(Long id : list){
            ContentValues value = new ContentValues();
            value.put(RecommendAppTable.APP_IS_STUB,2);
            mContext.getContentResolver().update(RecommendAppTable.TABLE_URI,value,
                    RecommendAppTable._ID + " = ?",new String[]{String.valueOf(id)});
        }
    }
    public List<RecommendApp> getRecommentAppFromLocal(int category,int size) {
        NqLog.i("getRecommentAppFromLocal category="+category+" size="+size);
        List<RecommendApp> list = new ArrayList<RecommendApp>();

        int seq = getCategorySeq(category);
        NqLog.i("getRecommentAppFromLocal category="+category+" seq="+seq);
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;
        try {
            List<Long> idList = getNotInstallIdList(category,seq,size);
            NqLog.i("getRecommentAppFromLocal category="+category+" idList="+getSeqString(idList));
            if(idList.size() > 0){
                cursor = contentResolver.query(RecommendAppTable.TABLE_URI, null,
                        RecommendAppTable._ID + " IN (" + getSeqString(idList) + ") AND "
                                + RecommendAppTable.APP_CATEGORY + " = ?",
                        new String[]{String.valueOf(category)}, RecommendAppTable._ID + " asc");
                while (cursor != null && cursor.moveToNext()) {
                    App app = mManager.cursorToApp(cursor);
                    list.add(new RecommendApp(app));
                }

                updateCategorySeq(category,idList.get(idList.size() - 1) + 1);
                flagRecommendApp(idList);
            }
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
            if (cursor != null){
                cursor.close();
            }
        }

        for(RecommendApp app : list){
            NqLog.i("getRecommentAppFromLocal app.name="+app.mApp.getStrName()+" pack="+app.mApp.getStrPackageName());
        }

        return list;
    }

    private boolean isAppExists(String resId){
    	boolean result = false;
        Cursor c = null;
        try{
            c = mContext.getContentResolver().query(RecommendAppTable.TABLE_URI,null,
                    RecommendAppTable.APP_ID + " = ?",new String[]{resId},null);
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
    
    private String getPackageNameById(long id){
        String result = "";
        Cursor c = null;
        try{
            c = mContext.getContentResolver().query(RecommendAppTable.TABLE_URI,null,
                    RecommendAppTable._ID + " = ?",new String[]{String.valueOf(id)},null);
            if(c != null && c.moveToNext()){
                result = c.getString(c.getColumnIndex(RecommendAppTable.APP_PACKAGENAME));
            }
        }finally {
            if(c != null){
                c.close();
            }
        }

        return result;
    }

    private List<Long> getCategoryIds(int category){
        Cursor c = null;
        List<Long> list = new ArrayList<Long>();
        try{
            c = mContext.getContentResolver().query(RecommendAppTable.TABLE_URI,null,
                    RecommendAppTable.APP_CATEGORY + " = ? AND " + RecommendAppTable.APP_IS_STUB + " IN (0,2)",
                    new String[]{String.valueOf(category)},null);
            while(c != null && c.moveToNext()){
                list.add(c.getLong(c.getColumnIndex(RecommendAppTable._ID)));
            }
        }finally {
            if(c != null){
                c.close();
            }
        }
        return list;
    }

    private List<Long> getNotInstallIdList(int category,int seq,int size){
        int count = 0;
        List<Long> idList = getCategoryIds(category);

        NqLog.i("getNotInstallIdList category="+category+" seq="+seq+" idList.size="+idList.size());

        int startIndex = 0;
        for(int i = 0;i < idList.size();i++){
            if(idList.get(i) >= seq){
                startIndex = i;
                break;
            }
        }

        NqLog.i("startIndex="+startIndex);

        List<Long> list = new ArrayList<Long>();
        for (int i = 0; i < idList.size(); i++) {
            if (startIndex >= idList.size()) {
                startIndex = 0;
            }

            if(!PackageUtils.isAppInstalled(mContext,getPackageNameById(idList.get(startIndex)))){
                list.add(idList.get(startIndex));
                count++;
            }

            startIndex++;
            if(count == size){
                break;
            }
        }

        for(Long l: list){
            NqLog.i("list l="+l);
        }
        return list;
    }

    private String getSeqString(List<Long> list){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<list.size();i++){
            sb.append(list.get(i));
            if(i != list.size() - 1){
                sb.append(",");
            }
        }

        NqLog.i("getSeqString str="+sb.toString());

        return sb.toString();
    }

    private void cacheRecommendApps(Map<Integer,List<RecommendApp>> map){
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation.Builder b;
        for (Integer key : map.keySet()) {
            List<RecommendApp> value = map.get(key);
            NqLog.i("cacheRecommendApps key="+key+" size="+value.size());
            b = ContentProviderOperation.newDelete(RecommendAppTable.TABLE_URI)
                    .withSelection(RecommendAppTable.APP_CATEGORY + " = ?"
                    + " AND " + RecommendAppTable.APP_IS_STUB + " IN (0,2) ",
                    new String[]{String.valueOf(key)});
            ops.add(b.build());
            for(int i = 0;i < value.size();i++){
                RecommendApp recommend = value.get(i);
                if (!isAppExists(recommend.mApp.getStrId()))  {
	                ContentValues contentValue = mManager.appToContentValues(0,recommend.mApp);
	                contentValue.put(RecommendAppTable.APP_CATEGORY,key);
	                contentValue.put(RecommendAppTable.APP_SILIENT_DOWNLOAD,recommend.mApp.isSlientDownload());
	                b = ContentProviderOperation.newInsert(RecommendAppTable.TABLE_URI).withValues(contentValue);
	                ops.add(b.build());
                }
            }

            try {
                mContext.getContentResolver().applyBatch(DataProvider.DATA_AUTHORITY, ops);
                ops.clear();
            } catch (Exception e) {
                NqLog.i("cacheRecommendApps err!");
                e.printStackTrace();
            }
        }
    }

    private List<String> getPreLoadUrl(int category){
        List<String> list = new ArrayList<String>();
        Cursor c = null;
        String selection = "limit " + 0 + "," + PRE_LOAD_ICON_SIZE;
        try{
            c = mContext.getContentResolver().query(RecommendAppTable.TABLE_URI,null,
                    RecommendAppTable.APP_CATEGORY + " = ? AND " + RecommendAppTable.APP_ICON_CACHE + " = 0",
                    new String[]{String.valueOf(category)},"_id ASC " + selection);
            while(c != null && c.moveToNext()){
                list.add(c.getString(c.getColumnIndex(RecommendAppTable.APP_ICON_URL)));
            }
        }finally {
            if(c != null){
                c.close();
            }
        }
        return list;
    }

    private void flagIconCache(String url){
        ContentValues value = new ContentValues();
        value.put(RecommendAppTable.APP_ICON_CACHE,1);
        mContext.getContentResolver().update(RecommendAppTable.TABLE_URI,value,RecommendAppTable.APP_ICON_URL + " = ?",new String[]{url});
    }

    private void preLoadIcon(){
        NqLog.i("preLoadIcon");
        for(int i :ALL_CATEGORY){
            List<String> list = getPreLoadUrl(i);
            for(String s:list){
                ImageManager.getInstance(mContext).loadImage(s,new ImageListener() {
                    @Override
                    public void getImageSucc(String url, BitmapDrawable drawable) {
                        flagIconCache(url);
                    }

                    @Override
                    public void onErr() {

                    }
                });
            }
        }
    }

    /**
     * 分类文件夹事件统计
     * @param category
     * @param action 0 创建 1 从桌面打开 2 从tab页切换到
     */
    public void onCategoryFolderAction(int category, int action){
        if(action == 1 || action == 2){
            int iswifi = NetworkUtils.isWifi(mContext) ? 1:0;
            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    CategoryFolderConstants.ACTION_LOG_2901,"",0,category + "_" + iswifi);
        }
    }

    public void onEvent(GetRecommendAppsProtocol.GetRecommendAppsSuccEvent event){
        NqLog.i("onEvent GetRecommendAppsSuccEvent");
        Map<Integer,List<RecommendApp>> map = event.mList;
        GetRecommendAppTag tag = (GetRecommendAppTag) event.getTag();
        List<Integer> category = tag.category;
    	int size = CategoryFolderPreference.getInstance().getIntValue(CategoryFolderPreference.KEY_EMPTY_FOLDER_APPSTUB_SIZE);
    	
        cacheRecommendApps(map);

        for(Integer key:category){
            updateCategorySeq(key,getMinId(key));
        }

        if(tag.single){
            RecommendAppsListener listener = tag.listener;
            List<RecommendApp> list = getRecommentAppFromLocal(category.get(0),tag.size);
            if(listener != null){
                if(list.size() > 0){
                    listener.onSuccess(list);

                    for(RecommendApp app : list){
                        StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                                CategoryFolderConstants.ACTION_LOG_2903,app.mApp.getStrId(),0,"");
                    }
                }else{
                    NqLog.i("get recommend apps succ,but all intall,return err!");
                    listener.onErr();
                }
            }
        }else{
            mLoading = false;
            preLoadIcon();
        }

        postGetAppTypeEvent(map);
    }

    private void postGetAppTypeEvent( Map<Integer,List<RecommendApp>> map){
        NqLog.i("postGetAppTypeEvent");
        for(Integer i : map.keySet()){
            List<AppTypeInfo> appTypeList = new ArrayList<AppTypeInfo>();
            for(RecommendApp app : map.get(i)){
                AppTypeInfo appType = new AppTypeInfo();
                appType.setPackageName(app.mApp.getStrPackageName());
                appType.setCode(i);
                appTypeList.add(appType);
            }

            EventBus.getDefault().post(new GetAppTypeProtocol.GetAppTypeSuccessEvent(appTypeList, null));
        }
    }

    public void onEvent(GetRecommendAppsProtocol.GetRecommendAppsFailEvent event){
        NqLog.i("onEvent GetRecommendAppsFailEvent");
        GetRecommendAppTag tag = (GetRecommendAppTag) event.getTag();
        if(tag.single){
            List<RecommendApp> list = getRecommentAppFromLocal(tag.category.get(0),tag.size);
            if(list.size() > 0){
                NqLog.i("get apps fail,return local app size="+list.size());
                tag.listener.onSuccess(list);

                for(RecommendApp app : list){
                    StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                            CategoryFolderConstants.ACTION_LOG_2903,app.mApp.getStrId(),0,"");
                }
            }else{
                NqLog.i("get apps fail,local app size is 0");
                tag.listener.onErr();
            }
        }else{
            mLoading = false;
            if(tag.listener != null){
                tag.listener.onErr();
            }
        }
    }

    private void checkRefreshRecommendApp(){
        NqLog.i("checkRefreshRecommendApp");
        long lasttime = CategoryFolderPreference.getInstance().getLongValue(CategoryFolderPreference.KEY_LAST_GET_RECOMMEND_APP_TIME);
        CategoryFolderPreference pre = CategoryFolderPreference.getInstance();
        if(System.currentTimeMillis() - lasttime > MIN_CACHE_TIME){
            if(NetworkUtils.isWifi(mContext) &&
                    (pre.getBooleanValue(CategoryFolderPreference.KEY_BOTTOM_RECOMMEND_SWITCH) ||
                     pre.getBooleanValue(CategoryFolderPreference.KEY_APP_STUB_SWITCH))) {
                getAppCategoryRecommendAps();
            }
        }
    }

    public void onEvent(PeriodCheckEvent event){
        NqLog.i("onEvent PeriodCheckEvent");
        checkRefreshRecommendApp();
    }

    public void onEvent(ConnectivityChangeEvent event){
        if(NetworkUtils.isWifi(mContext)){
            long lasttime = CategoryFolderPreference.getInstance().getLongValue(CategoryFolderPreference.KEY_LAST_GET_RECOMMEND_APP_TIME);
            CategoryFolderPreference pre = CategoryFolderPreference.getInstance();
            if(System.currentTimeMillis() - lasttime > MIN_CACHE_TIME &&
                    (pre.getBooleanValue(CategoryFolderPreference.KEY_BOTTOM_RECOMMEND_SWITCH) ||
                            pre.getBooleanValue(CategoryFolderPreference.KEY_APP_STUB_SWITCH))) {
                getAppCategoryRecommendAps();
            }else{
                preLoadIcon();
            }
        }
    }

    public void gotoStoreByCategory(int category) {
    	NqLog.i("ljc1234:gotoStoreByCategory--> category =" + category);
		Intent intent = new Intent(mContext, StoreMainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(StoreMainActivity.KEY_FROM, StoreEntry.FROM_CATEGORY_FOLDER);
        intent.putExtra(StoreMainActivity.KEY_COLUMN, category == CategoryFolderConstants.CATEGORY_200 ? 0 : 1);
		mContext.startActivity(intent);
    }
    
    /**
     * 
     * @param category
     * @param resID
     * @param packageName
     * @param action  0:分类文件夹里创建虚框应用     
     * 				  1:分类文件夹里虚框应用的展示       
     * 				  2:分类文件夹里虚框应用的点击
     * @param params
     */
    public void onAppStubAction(int category, String resID, String packageName, int action, String params) {
    	switch (action) {
    	case 0:
            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    CategoryFolderConstants.ACTION_LOG_2905, resID, 0, String.valueOf(category));    		
    		break;
    	case 1:
            StatManager.getInstance().onAction(StatManager.TYPE_STORE_ACTION,
                    CategoryFolderConstants.ACTION_LOG_2906, resID, 0, String.valueOf(category));     		
    		break;    		
    	case 2:
            StatManager.getInstance().onAction(StatManager.TYPE_AD_STAT,
                    CategoryFolderConstants.ACTION_LOG_2907, resID, 0, String.valueOf(category));     		
    		break;    
    	default:
    		break;
    	}
    }
       
    public void getAppStubByCategory(int category, AppStubListener listener){
        NqLog.i("getAppStubByCategory category="+category+" listener="+listener.getClass());
        int size = CategoryFolderPreference.getInstance().getIntValue(CategoryFolderPreference.KEY_EMPTY_FOLDER_APPSTUB_SIZE);
        if (size <= 0) {
        	listener.onErr(); //服务器下发的虚框个数不正确，返回错误
        }
               
        if(getAppStubCacheSize(category) > 0){
            List<AppStub> list = getAppStubFromLocal(category, size);
            if(list.size() > 0){
            	prefetchAppStubList(list);//静默下载取到的虚框
                listener.onSuccess(list);
                NqLog.i("getAppStubByCategory category="+category+" succeed!");
            }else{
                listener.onErr(); //过滤后广告数量为0 先返回错误
                NqLog.i("getAppStubByCategory category="+category+" failed: no resource after filter");	                
            }
        }else{
            if(getCategorySize(category) > 0) {
                listener.onErr(); //缓存中有资源 但是可能全是底部推荐了
            } else {
	            listener.onErr();
            }
        }
    }
    
    private int getAppStubCacheSize(int category){
        Cursor c = null;
        int result = -1;
        try{
            c = mContext.getContentResolver().query(RecommendAppTable.TABLE_URI,null,
                    RecommendAppTable.APP_CATEGORY + " = ? AND " + RecommendAppTable.APP_IS_STUB + " IN (0,1)",
                    new String[]{String.valueOf(category)},null);
            if(c != null){
                result = c.getCount();
            }
        }finally {
            if(c != null){
                c.close();
            }
        }

        NqLog.i("getAppStubCacheSize category="+category+" size="+result);
        return result;
    }
    
    private List<AppStub> getAppStubFromLocal(int category, int size) {
        NqLog.i("ljc1234:getAppStubFromLocal category="+category+" size="+size);
        List<AppStub> list = new ArrayList<AppStub>();
                  
        List<Long> resultList = new ArrayList<Long>();
        
        boolean notReturnBefore = false;
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;
        try {
            //先找标记为虚框的资源，如果数据库中没有已经存在的虚框资源，则重新找标记为0的资源，找到后更新虚框标记位
            cursor = contentResolver.query(RecommendAppTable.TABLE_URI, null,
                    RecommendAppTable.APP_IS_STUB + " = 1 AND " + RecommendAppTable.APP_CATEGORY + " = ?",
                    new String[]{String.valueOf(category)}, RecommendAppTable._ID + " asc");
            if (cursor == null || !cursor.moveToNext() ) {
            	notReturnBefore = true;
            	if (cursor != null) {
            		cursor.close();
            	}            	
	            cursor = contentResolver.query(RecommendAppTable.TABLE_URI, null,
	                    RecommendAppTable.APP_IS_STUB + " = 0 AND " + RecommendAppTable.APP_CATEGORY + " = ?",
	                    new String[]{String.valueOf(category)}, RecommendAppTable._ID + " asc");
            }
            while (cursor != null && cursor.moveToNext()) {
            	if (PackageUtils.isAppInstalled(mContext, cursor.getString(cursor.getColumnIndex(RecommendAppTable.APP_PACKAGENAME)))) {
            		continue;
            	}
                App app = mManager.cursorToApp(cursor);
                app.setIntSourceType(CategoryFolderConstants.DONWLOAD_SOURCE_TYPE_APPSTUB);
                app.setSlientDownload(1 == cursor.getInt(cursor.getColumnIndex(RecommendAppTable.APP_SILIENT_DOWNLOAD)));
                
                AppStub appStub = new AppStub();
                appStub.setApp(app);
                Intent intent = AppStubManager.getInstance(mContext).getAppStubIntent(app);
                intent.putExtra(KEY_FROM_CATEGORY_FODLER, VALUE_CATEGORY_FODLER);
                appStub.setOpen(intent);
                list.add(appStub);
                resultList.add(cursor.getLong(cursor.getColumnIndex(RecommendAppTable._ID)));
                if (list.size() >= size) {
                	break;
                }
            }
            NqLog.i("ljc1234:getAppStubFromLocal list.size()="+list.size());
            if (notReturnBefore && list.size() > 0) {
	            flagAppStubItems(category,resultList);
            }
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
            if (cursor != null){
                cursor.close();
            }
        }

        for(AppStub app : list){
            NqLog.i("getAppStubFromLocal app.name="+app.getApp().getStrName()+" pack="+app.getApp().getStrPackageName());
        }

        return list;    
    }
    
    private void prefetchAppStubList(List<AppStub> apps) {
		PrefetchEvent prefetchEvent = new PrefetchEvent();
		ArrayList<PrefetchRequest> requests = new ArrayList<PrefetchRequest>(apps.size());
		for (AppStub appStub : apps) {
			if (appStub == null) {
				continue;
			}
			App app = appStub.getApp();
			if (app.isSlientDownload()) {
				NqLog.i("ljc1234: prefetch add resource:" + app.getStrName());
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
			prefetchEvent.setSourceType(CategoryFolderConstants.DONWLOAD_SOURCE_TYPE_APPSTUB);
			prefetchEvent.setRequests(requests);

			EventBus.getDefault().post(prefetchEvent);
		}	
    }
    
    private void flagAppStubItems(int category, List<Long> ids){
        NqLog.i("flagAppStubItems category="+category);
        ContentValues value = new ContentValues();
        value.put(RecommendAppTable.APP_IS_STUB, 1);
        mContext.getContentResolver().update(RecommendAppTable.TABLE_URI,value,
        		RecommendAppTable._ID + " IN (" + getSeqString(ids) + ") AND " +
                RecommendAppTable.APP_CATEGORY + " = ? AND " + RecommendAppTable.APP_IS_STUB + " = 0",
                new String[]{String.valueOf(category)});
    }
    
    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
