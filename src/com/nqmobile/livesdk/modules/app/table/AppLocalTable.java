package com.nqmobile.livesdk.modules.app.table;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.nqmobile.livesdk.commons.db.AbsDataTable;
import com.nqmobile.livesdk.commons.db.DataProvider;

public class AppLocalTable extends AbsDataTable {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String TABLE_NAME = "app_local";	
	public static final int TABLE_VERSION = 2;
	public static final Uri LOCAL_APP_URI = Uri.parse("content://" + DataProvider.DATA_AUTHORITY + "/" + TABLE_NAME);
	
	public static final String _ID = "_id";
	/* 本地应用表和应用缓存表的字段 */
	public static final String APP_ID = "appId";
	/** 功能模块来源编号：0：store列表；1：banner；2每日推荐 */
	public static final String APP_SOURCE_TYPE = "sourceType";
	/** 板块编号(仅sourceType=1时才用到，表明banner位于那个板块)： 0：应用、游戏；1：主题；2：壁纸。 */
	public static final String BANNER_PLATE= "bannerPlate";
	/** 栏目编号： 3：游戏；4：应用 */
	public static final String APP_COLUMN = "column";
	/** 资源分类： 0应用推荐 1链接广告 */
	public static final String APP_TYPE = "type";
	public static final String APP_CATEGORY1 = "Category1";
	public static final String APP_CATEGORY2 = "Category2";
	public static final String APP_NAME = "name";
	public static final String APP_DESCRIPTION = "description";
	public static final String APP_DEVELOPERS = "developers";
	public static final String APP_PRICE = "price";
	public static final String APP_POINT = "point";
	public static final String APP_RATE = "rate";
	public static final String APP_SIZE = "size";
	public static final String APP_ICON_URL = "iconUrl";
	public static final String APP_IMAGE_URL = "imageUrl";
	public static final String APP_URL = "appUrl";
	public static final String APP_GP_URL = "appGPUrl";
	public static final String APP_CLICK_ACTION_TYPE = "clickActionType";
	public static final String APP_DOWNLOAD_ACTION_TYPE = "downloadActionType";
	public static final String APP_PREVIEW_URL = "previewUrl";
	public static final String APP_ICON_PATH = "iconPath";
	public static final String APP_IMAGE_PATH = "imagePath";
	public static final String APP_PATH = "appPath";
	public static final String APP_PREVIEW_PATH = "previewPath";
	public static final String APP_PACKAGENAME = "packageName";
	public static final String APP_VERSION = "version";
	public static final String APP_DOWNLOAD_COUNT = "downloadCount";
	public static final String APP_UPDATETIME = "updateTime";
	public static final String APP_LOCALTIME = "localTime";
    public static final String APP_REWARDPOINTS = "rewardpoints";
    public static final String APP_TRACKID = "trackid";
    
	// ===========================================================
	// Fields
	// ===========================================================
	
	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	@Override
	public String getName() {
		return TABLE_NAME;
	}
	
	@Override
	public int getVersion() {
		return TABLE_VERSION;
	}
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void create(SQLiteDatabase db) {
		try{
			db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
					_ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +          
					APP_ID +" VARCHAR(20)," +   
					APP_SOURCE_TYPE + " INTEGER default 0," +   
					BANNER_PLATE + " INTEGER default 0," +        
					APP_COLUMN + " INTEGER default 0," + 
					APP_TYPE +" VARCHAR(20)," +
					APP_CATEGORY1 +" VARCHAR(20)," +
					APP_CATEGORY2 +" VARCHAR(20)," +
					APP_NAME +" VARCHAR(20)," +
					APP_DESCRIPTION +" VARCHAR(50)," +
					APP_DEVELOPERS +" VARCHAR(20)," +
					APP_PRICE +" VARCHAR(20)," +
					APP_POINT +" VARCHAR(20)," +
					APP_RATE +" VARCHAR(20)," +
					APP_DOWNLOAD_COUNT +" VARCHAR(20)," + 
					APP_SIZE +" VARCHAR(20)," + 
					APP_ICON_URL +" VARCHAR(20)," + 
					APP_IMAGE_URL +" VARCHAR(20)," + 
					APP_URL +" VARCHAR(20)," + 
					APP_GP_URL +" VARCHAR(20)," + 
					APP_CLICK_ACTION_TYPE + " INTEGER default 0," + 
					APP_DOWNLOAD_ACTION_TYPE + " INTEGER default 0," + 
					APP_PREVIEW_URL +" VARCHAR(20)," + 
					APP_ICON_PATH +" VARCHAR(20)," + 
					APP_IMAGE_PATH +" VARCHAR(20)," + 
					APP_PATH +" VARCHAR(20)," +  
					APP_PREVIEW_PATH +" VARCHAR(20)," +  
					APP_PACKAGENAME +" VARCHAR(20)," + 
					APP_VERSION +" VARCHAR(20)," + 
					APP_UPDATETIME +" INTEGER default 0," +
                    APP_REWARDPOINTS + " INTEGER default 0," +
                    APP_TRACKID + " VARCHAR(20)," +
					APP_LOCALTIME +" INTEGER default 0" + 
					")"
			);
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		super.upgrade(db, oldVersion, newVersion);
	}
	
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
