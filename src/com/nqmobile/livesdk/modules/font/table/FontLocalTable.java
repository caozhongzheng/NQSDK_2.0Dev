package com.nqmobile.livesdk.modules.font.table;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.nqmobile.livesdk.commons.db.AbsDataTable;
import com.nqmobile.livesdk.commons.db.DataProvider;

public class FontLocalTable extends AbsDataTable {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String TABLE_NAME = "font_local";	
	public static final int TABLE_VERSION = 2;
	public static final Uri LOCAL_FONT_URI = Uri.parse("content://" + DataProvider.DATA_AUTHORITY + "/" + TABLE_NAME);
	
	public static final String _ID = "_id";
	/* 本地主题表和主题缓存表的字段 */
	public static final String FONT_ID = "fontId";
	/** 功能模块来源编号：0：store列表；1：banner；2每日推荐 */
	public static final String FONT_SOURCE_TYPE = "sourceType";
	/** 栏目分类：0：新品；1：热门 */
	public static final String FONT_COLUMN = "column";
	public static final String FONT_NAME = "name";
	public static final String FONT_AUTHOR = "author";
	public static final String FONT_VERSION = "version";
	public static final String FONT_SOURCE = "source";
	public static final String FONT_SIZE = "size";
	public static final String FONT_ICON_URL = "iconUrl";
	public static final String FONT_PREVIEW_URL = "previewUrl";
	public static final String FONT_URL = "fontUrl";
	public static final String FONT_ICON_PATH = "iconPath";
	public static final String FONT_PREVIEW_PATH = "previewPath";
	public static final String FONT_PATH = "fontPath";
	public static final String FONT_PACKAGENAME = "packageName";
	public static final String FONT_DOWNLOAD_COUNT = "downloadCount";
	public static final String FONT_UPDATETIME = "updateTime";
	public static final String FONT_LOCALTIME = "localTime";
    public static final String FONT_DAILYICON = "dailyicon";
    public static final String FONT_COSUMEPOINTS = "consumepoints";
    public static final String FONT_POINTSFLAG = "pointsflag";
    public static final String FONT_JSON = "fontjson";
    
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
					FONT_ID +" VARCHAR(20)," +      
					FONT_SOURCE_TYPE + " INTEGER default 0," + 
					FONT_COLUMN + " INTEGER default 0," + 
					FONT_NAME +" VARCHAR(20)," +    
					FONT_AUTHOR +" VARCHAR(20)," +    
					FONT_VERSION +" VARCHAR(20)," +    
					FONT_SOURCE +" VARCHAR(20)," +    
					FONT_SIZE +" VARCHAR(20)," +    
					FONT_ICON_URL +" VARCHAR(20)," +    
					FONT_PREVIEW_URL +" VARCHAR(20)," +    
					FONT_URL +" VARCHAR(20)," +    
					FONT_ICON_PATH +" VARCHAR(20)," +    
					FONT_PREVIEW_PATH +" VARCHAR(20)," +    
					FONT_PATH +" VARCHAR(20)," +    
					FONT_JSON +" VARCHAR(120)," +
					FONT_PACKAGENAME +" VARCHAR(20)," +    
					FONT_DOWNLOAD_COUNT +" VARCHAR(20)," + 
					FONT_UPDATETIME +" INTEGER default 0," +    
					FONT_DAILYICON + " VARCHAR(20)," +
                    FONT_COSUMEPOINTS + " INTEGER default 50," +
                    FONT_POINTSFLAG + " INTEGER default 2," +
					FONT_LOCALTIME +" INTEGER default 0" +  
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
