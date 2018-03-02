package com.nqmobile.livesdk.modules.wallpaper.table;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.nqmobile.livesdk.commons.db.AbsDataTable;
import com.nqmobile.livesdk.commons.db.DataProvider;

public class WallpaperCacheTable extends AbsDataTable {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String TABLE_NAME = "wallpaper_cache";	
	public static final int TABLE_VERSION = 2;
	public static final Uri WALLPAPER_CACHE_URI = Uri.parse("content://" + DataProvider.DATA_AUTHORITY + "/" + TABLE_NAME);

	public static final String _ID = "_id";
	/* 本地壁纸表和壁纸缓存表的字段 */
	public static final String WALLPAPER_ID = "wallpaperId";
	/** 功能模块来源编号：0：store列表；1：banner；2每日推荐 */
	public static final String WALLPAPER_SOURCE_TYPE = "sourceType";
	/** 栏目分类：0：新品；1：热门 */
	public static final String WALLPAPER_COLUMN = "column";
	public static final String WALLPAPER_NAME = "name";
	/** 壁纸分类：0静态壁纸; 1动态壁纸 */
	public static final String WALLPAPER_TYPE = "type";
	public static final String WALLPAPER_AUTHOR = "author";
	public static final String WALLPAPER_VERSION = "version";
	public static final String WALLPAPER_SOURCE = "source";
	public static final String WALLPAPER_SIZE = "size";
	public static final String WALLPAPER_ICON_URL = "iconUrl";
	public static final String WALLPAPER_URL = "url";
	public static final String WALLPAPER_ICON_PATH = "iconPath";
    public static final String WALLPAPER_PREVIEW_URL = "previewUrl";
	public static final String WALLPAPER_PREVIEW_PATH = "previewPath";
	public static final String WALLPAPER_PATH = "path";
	public static final String WALLPAPER_PACKAGENAME = "packageName";
	public static final String WALLPAPER_DOWNLOAD_COUNT = "downloadCount";
	public static final String WALLPAPER_UPDATETIME = "updateTime";
	public static final String WALLPAPER_LOCALTIME = "localTime";
	public static final String WALLPAPER_DOWNLOAD_ID = "downloadId";
    public static final String WALLPAPER_DAILYICON = "dailyicon";
    
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
					WALLPAPER_ID +" VARCHAR(20)," +
					WALLPAPER_NAME +" VARCHAR(20)," +
					WALLPAPER_SOURCE_TYPE + " INTEGER default 0," + 
					WALLPAPER_COLUMN + " INTEGER default 0," + 
					WALLPAPER_TYPE +" VARCHAR(20)," + 
					WALLPAPER_AUTHOR +" VARCHAR(20)," + 
					WALLPAPER_VERSION +" VARCHAR(20)," + 
					WALLPAPER_SOURCE +" VARCHAR(20)," + 
					WALLPAPER_SIZE +" VARCHAR(20)," + 
					WALLPAPER_ICON_URL +" VARCHAR(20)," + 
					WALLPAPER_URL +" VARCHAR(20)," + 
					WALLPAPER_ICON_PATH +" VARCHAR(20)," +
                    WALLPAPER_PREVIEW_URL + " VARCHAR(20)," +
					WALLPAPER_PREVIEW_PATH +" VARCHAR(20)," + 
					WALLPAPER_PATH +" VARCHAR(20)," + 
					WALLPAPER_PACKAGENAME +" VARCHAR(20)," + 
					WALLPAPER_DOWNLOAD_COUNT +" VARCHAR(20)," + 
					WALLPAPER_UPDATETIME +" INTEGER default 0," +
                    WALLPAPER_DAILYICON + " VARCHAR(20)," +
					WALLPAPER_LOCALTIME +" INTEGER default 0" + 
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
