package com.nqmobile.livesdk.modules.banner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.nqmobile.livesdk.commons.eventbus.EventBus;
import com.nqmobile.livesdk.commons.moduleframework.AbsManager;
import com.nqmobile.livesdk.modules.banner.network.BannerListProtocol.GetBannerListSuccessEvent;
import com.nqmobile.livesdk.modules.banner.network.BannerServiceFactory;
import com.nqmobile.livesdk.modules.banner.table.BannerCacheTable;
import com.nqmobile.livesdk.utils.NetworkUtils;
import com.nqmobile.livesdk.utils.StringUtil;

/**
 * banner业务类
 *
 * @author changxiaofei
 * @time 2013-12-5 下午6:43:37
 */
public class BannerManager extends AbsManager{
	private static final String TAG = "BannerManager";
    private Context mContext;
    private static BannerManager mInstance;

    private Object mGetListLock;
    private boolean mIsGettingList;
    private ArrayList<BannerListRequest> mBannerListRequests;
    
    private class BannerListRequest{
    	BannerListListener mListener;
    	public BannerListRequest(BannerListListener listener){
    		mListener = listener;
    	}
    }

    public BannerManager(Context context) {
        super();
        this.mContext = context.getApplicationContext();
        mGetListLock = new Object();
        mIsGettingList = false;
        mBannerListRequests = new ArrayList<BannerListRequest>();
        
        init();
    }

    public synchronized static BannerManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BannerManager(context);
        }
        return mInstance;
    }


    /**
     * 将cursor对象转换Banner
     *
     * @param cursor
     * @return
     */
    public Banner cursorToBanner(Cursor cursor) {
        Banner app = new Banner();
        app.setStrId(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(BannerCacheTable.APP_ID))));
        app.setIntSourceType(cursor.getInt(cursor.getColumnIndex(BannerCacheTable.APP_SOURCE_TYPE)));
        app.setStrName(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(BannerCacheTable.APP_NAME))));
        app.setStrPackageName(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(BannerCacheTable.APP_PACKAGENAME))));
        app.setStrDescription(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(BannerCacheTable.APP_DESCRIPTION))));
        app.setStrDevelopers(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(BannerCacheTable.APP_DEVELOPERS))));
        app.setStrCategory1(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(BannerCacheTable.APP_CATEGORY1))));
        app.setStrCategory2(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(BannerCacheTable.APP_CATEGORY2))));
        app.setStrVersion(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(BannerCacheTable.APP_VERSION))));
        app.setFloatRate(cursor.getLong(cursor.getColumnIndex(BannerCacheTable.APP_RATE)));
        app.setLongDownloadCount(cursor.getLong(cursor.getColumnIndex(BannerCacheTable.APP_DOWNLOAD_COUNT)));
        app.setLongSize(cursor.getLong(cursor.getColumnIndex(BannerCacheTable.APP_SIZE)));
        app.setStrIconUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(BannerCacheTable.APP_ICON_URL))));
        app.setStrIconPath(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(BannerCacheTable.APP_ICON_PATH))));
        app.setStrImageUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(BannerCacheTable.APP_IMAGE_URL))));
        app.setStrImagePath(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(BannerCacheTable.APP_IMAGE_PATH))));
        String previewUrl = cursor.getString(cursor.getColumnIndex(BannerCacheTable.APP_PREVIEW_URL));
        if (!TextUtils.isEmpty(previewUrl)) {
            String[] s = previewUrl.split(";");
            List<String> list = new ArrayList<String>();
            for (int i = 0; i < s.length; i++) {
                list.add(s[i]);
            }
            app.setArrPreviewUrl(list);
        }
        String previewPath = cursor.getString(cursor.getColumnIndex(BannerCacheTable.APP_PREVIEW_PATH));
        if (!TextUtils.isEmpty(previewPath)) {
            String[] s = previewPath.split(";");
            List<String> listPath = new ArrayList<String>();
            for (int i = 0; i < s.length; i++) {
                listPath.add(s[i]);
            }
            app.setArrPreviewPath(listPath);
        }
        app.setStrAppUrl(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(BannerCacheTable.APP_URL))));
        app.setStrAppPath(StringUtil.nullToEmpty(cursor.getString(cursor.getColumnIndex(BannerCacheTable.APP_PATH))));
        app.setIntClickActionType(cursor.getInt(cursor.getColumnIndex(BannerCacheTable.APP_CLICK_ACTION_TYPE)));
        app.setIntDownloadActionType(cursor.getInt(cursor.getColumnIndex(BannerCacheTable.APP_DOWNLOAD_ACTION_TYPE)));
        app.setLongUpdateTime(cursor.getLong(cursor.getColumnIndex(BannerCacheTable.APP_UPDATETIME)));
        app.setLongLocalTime(cursor.getLong(cursor.getColumnIndex(BannerCacheTable.APP_LOCALTIME)));
        //banner新增
        List<Integer> plate = new ArrayList<Integer>();
        plate.add(cursor.getInt(cursor.getColumnIndex(BannerCacheTable.BANNER_PLATE)));
        app.setIntPlate(plate);//板块编号
        return app;
    }

    /**
     * 将app对象转换层数据库操作的ContentValues对象
     *
     * @param Banner
     * @return
     */
    public ContentValues bannerToContentValues(Banner app, int plate) {
        ContentValues values = null;
        if (app != null) {
            values = new ContentValues();
            values.put(BannerCacheTable.APP_ID, app.getStrId());
            values.put(BannerCacheTable.APP_SOURCE_TYPE, app.getIntSourceType());
            values.put(BannerCacheTable.APP_COLUMN, 0);//banner不区分栏目
            values.put(BannerCacheTable.APP_TYPE, "0");//???广告平台整合到服务器后会下发此字段。
            values.put(BannerCacheTable.APP_CATEGORY1, app.getStrCategory1());
            values.put(BannerCacheTable.APP_CATEGORY2, app.getStrCategory2());
            values.put(BannerCacheTable.APP_NAME, app.getStrName());
            values.put(BannerCacheTable.APP_DESCRIPTION, app.getStrDescription());
            values.put(BannerCacheTable.APP_DEVELOPERS, app.getStrDevelopers());
            values.put(BannerCacheTable.APP_RATE, app.getFloatRate());
            values.put(BannerCacheTable.APP_VERSION, app.getStrVersion());
            values.put(BannerCacheTable.APP_SIZE, app.getLongSize());
            values.put(BannerCacheTable.APP_DOWNLOAD_COUNT, app.getLongDownloadCount());
            values.put(BannerCacheTable.APP_PACKAGENAME, app.getStrPackageName());
            values.put(BannerCacheTable.APP_ICON_URL, app.getStrIconUrl());
            values.put(BannerCacheTable.APP_IMAGE_URL, app.getStrImageUrl());
            //预览图网址
            StringBuilder previewUrl = new StringBuilder();
            List<String> previewUrls = app.getArrPreviewUrl();
            if (previewUrls != null && previewUrls.size() > 0) {
                for (int j = 0; j < previewUrls.size(); j++) {
                    previewUrl.append(previewUrls.get(j)).append(";");
                }
            }
            if (previewUrl.length() > 1) {
                values.put(BannerCacheTable.APP_PREVIEW_URL, previewUrl.substring(0, previewUrl.length() - 1));
            } else {
                values.put(BannerCacheTable.APP_PREVIEW_URL, "");
            }
            values.put(BannerCacheTable.APP_URL, app.getStrAppUrl());
            values.put(BannerCacheTable.APP_CLICK_ACTION_TYPE, app.getIntClickActionType());
            values.put(BannerCacheTable.APP_DOWNLOAD_ACTION_TYPE, app.getIntDownloadActionType());
            values.put(BannerCacheTable.APP_ICON_PATH, app.getStrIconPath());
            values.put(BannerCacheTable.APP_IMAGE_PATH, app.getStrImagePath());
            //预览图本地路径
            StringBuilder previewPath = new StringBuilder();
            List<String> previewPaths = app.getArrPreviewPath();
            if (previewPaths != null && previewPaths.size() > 0) {
                for (int j = 0; j < previewPaths.size(); j++) {
                    previewPath.append(previewPaths.get(j)).append(";");
                }
            }
            if (previewPath.length() > 1) {
                values.put(BannerCacheTable.APP_PREVIEW_PATH, previewPath.substring(0, previewPath.length() - 1));
            } else {
                values.put(BannerCacheTable.APP_PREVIEW_PATH, "");
            }
            values.put(BannerCacheTable.APP_PATH, app.getStrAppPath());
            values.put(BannerCacheTable.APP_UPDATETIME, app.getLongUpdateTime());
            values.put(BannerCacheTable.APP_LOCALTIME, app.getLongLocalTime());

            //banner新增
            values.put(BannerCacheTable.BANNER_PLATE, plate);//板块编号
        }
        return values;
    }

    /**
     * 获取Banner列表
     *
     * @param plate    板块编号
     * @param listener
     */
    public void getBannerList(final int plate, final BannerListListener listener) {
    	//检查参数
    	if (listener == null){
    		return;
    	}
    	
        // 没有缓存，或缓存过期，联网查询数据。
        if (!NetworkUtils.isConnected(mContext)){
        	listener.onNoNetwork();	
        	return;
        }
		synchronized (mGetListLock) {
			mBannerListRequests.add(new BannerListRequest(listener));
			if (!mIsGettingList) {
				BannerServiceFactory.getService().getBannerList(mContext, plate, new MyBannerListListener());
				mIsGettingList = true;
			}
		}
    }
    
    class MyBannerListListener implements BannerListListener {
		@Override
		public void onErr() {
			List<BannerListRequest> requests = new ArrayList<BannerListRequest>();
			synchronized (mGetListLock) {
				mIsGettingList = false;
				requests.addAll(mBannerListRequests);
				mBannerListRequests.clear();
			}
			for (BannerListRequest listener : requests) {
				listener.mListener.onErr();
			}
		}

		@Override
		public void onGetBannerListSucc(List<Banner> banners) {
			List<BannerListRequest> requests = new ArrayList<BannerListRequest>();
			synchronized (mGetListLock) {
				mIsGettingList = false;
				requests.addAll(mBannerListRequests);
				mBannerListRequests.clear();
			}

			for (BannerListRequest listener : requests) {
				/*List<Banner> list = null;
				if (banners != null && banners.size() > 0) {
					list = filterByPlate(banners, listener.mPlate);
				}*/
				listener.mListener.onGetBannerListSucc(banners/*list*/);
			}
		}

		@Override
		public void onNoNetwork() {
			List<BannerListRequest> listeners = new ArrayList<BannerListRequest>();
			synchronized (mGetListLock) {
				mIsGettingList = false;
				listeners.addAll(mBannerListRequests);
				mBannerListRequests.clear();
			}
			for (BannerListRequest listener : listeners) {
				listener.mListener.onNoNetwork();
			}
		}
	}
    
    /**
     * Banner列表存入缓存库
     *
     * @param banners
     * @return
     */
    public boolean saveBannerCache2(ArrayList<Banner> banners, int plat) {
         CacheHelper.getInstance(mContext).saveBannerListDataObjCache(banners, plat);
    	// 更新缓存时间。
         BannerPreference.getInstance().setLongValue(BannerPreference.KEY_BANNER_LIST_CACHE_TIME + plat, new Date().getTime());
        return true;
    }


    /**
     * 返回缓存的Banner列表。
     *
     * @param plate 板块编号
     * @return
     */
    public List<Banner> getBannerListFromCache(int plate) {
        List<Banner> listBanners = null;
        /*ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(BannerCacheTable.BANNER_CACHE_URI, null, BannerCacheTable.BANNER_PLATE + "="
                    + plate, null, BannerCacheTable._ID + " asc");
            if (cursor != null && cursor.getCount() > 0) {
                listBanners = new ArrayList<Banner>();
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    Banner banner = BannerManager.getInstance(mContext).cursorToBanner(cursor);
                    listBanners.add(banner);
                }
            }
        } catch (Exception e) {
            NqLog.e(e);
        } finally {
			CursorUtils.closeCursor(cursor);
        }*/
        
        listBanners = CacheHelper.getInstance(mContext).getBannerListFromCache(plate);
        return listBanners;
    }
    
    public boolean isCacheExpired(int plat){
        Long cacheTime = 0l;
        cacheTime = BannerPreference.getInstance().getLongValue(BannerPreference.KEY_BANNER_LIST_CACHE_TIME + plat);
        Long currentTime = new Date().getTime();
    	return currentTime - cacheTime > BannerConstants.CACHE_MAX_TIME;
    }

	@Override
	public void init() {
		EventBus.getDefault().register(this);
	}
	
	public void onEvent(GetBannerListSuccessEvent event){
		MyBannerListListener listener = (MyBannerListListener) event.getTag();
		if(event.isSuccess()){
			listener.onGetBannerListSucc(event.getBanners());
		}else {
			listener.onErr();
		}
	}
}
