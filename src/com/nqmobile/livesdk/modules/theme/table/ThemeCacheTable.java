package com.nqmobile.livesdk.modules.theme.table;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.nqmobile.livesdk.commons.db.AbsDataTable;
import com.nqmobile.livesdk.commons.db.DataProvider;

public class ThemeCacheTable extends AbsDataTable {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String TABLE_NAME = "theme_cache";	
	public static final int TABLE_VERSION = 2;
	public static final Uri THEME_CACHE_URI = Uri.parse("content://" + DataProvider.DATA_AUTHORITY + "/" + TABLE_NAME);

	public static final String _ID = "_id";
	/* 本地主题表和主题缓存表的字段 */
	public static final String THEME_ID = "themeId";
	/** 功能模块来源编号：0：store列表；1：banner；2每日推荐 */
	public static final String THEME_SOURCE_TYPE = "sourceType";
	/** 栏目分类：0：新品；1：热门 */
	public static final String THEME_COLUMN = "column";
	public static final String THEME_NAME = "name";
	public static final String THEME_AUTHOR = "author";
	public static final String THEME_VERSION = "version";
	public static final String THEME_SOURCE = "source";
	public static final String THEME_SIZE = "size";
	public static final String THEME_ICON_URL = "iconUrl";
	public static final String THEME_PREVIEW_URL = "previewUrl";
	public static final String THEME_URL = "themeUrl";
	public static final String THEME_ICON_PATH = "iconPath";
	public static final String THEME_PREVIEW_PATH = "previewPath";
	public static final String THEME_PATH = "themePath";
	public static final String THEME_PACKAGENAME = "packageName";
	public static final String THEME_DOWNLOAD_COUNT = "downloadCount";
	public static final String THEME_UPDATETIME = "updateTime";
	public static final String THEME_LOCALTIME = "localTime";
    public static final String THEME_DAILYICON = "dailyicon";
    public static final String THEME_COSUMEPOINTS = "consumepoints";
    public static final String THEME_POINTSFLAG = "pointsflag";
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
					THEME_ID +" VARCHAR(20)," +      
					THEME_SOURCE_TYPE + " INTEGER default 0," + 
					THEME_COLUMN + " INTEGER default 0," + 
					THEME_NAME +" VARCHAR(20)," +    
					THEME_AUTHOR +" VARCHAR(20)," +    
					THEME_VERSION +" VARCHAR(20)," +    
					THEME_SOURCE +" VARCHAR(20)," +    
					THEME_SIZE +" INTEGER(30)," +    
					THEME_ICON_URL +" VARCHAR(20)," +    
					THEME_PREVIEW_URL +" VARCHAR(20)," +    
					THEME_URL +" VARCHAR(20)," +    
					THEME_ICON_PATH +" VARCHAR(20)," +    
					THEME_PREVIEW_PATH +" VARCHAR(20)," +    
					THEME_PATH +" VARCHAR(20)," +    
					THEME_PACKAGENAME +" VARCHAR(20)," +  
					THEME_DOWNLOAD_COUNT +" VARCHAR(20)," +   
					THEME_UPDATETIME +" INTEGER default 0," +
                    THEME_DAILYICON + " VARCHAR(20)," +
                    THEME_COSUMEPOINTS + " INTEGER default 50," +
                    THEME_POINTSFLAG + " INTEGER default 2," +
					THEME_LOCALTIME +" INTEGER default 0" +  
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
