package com.nqmobile.livesdk.modules.appstubfolder.table;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.nqmobile.livesdk.commons.db.AbsDataTable;
import com.nqmobile.livesdk.commons.db.DataProvider;
import com.nqmobile.livesdk.utils.DatabaseUtil;


/**
 * 虚框文件夹缓存表
 * @author caozhongzheng
 * @date 2014-11-20
 */
public class AppStubFolderCacheTable extends AbsDataTable {
    // ===========================================================
    // Constants
    // ===========================================================	
	public static final String TABLE_NAME = "stubfolder_cache";	
	public static final int TABLE_VERSION = 2;
	public static final String DATA_AUTHORITY = DataProvider.DATA_AUTHORITY;
	public static final Uri TABLE_URI = Uri.parse("content://"+DATA_AUTHORITY+"/" + TABLE_NAME);
	
	public static final String _ID =  "_id";
	public static final String STUBFOLDER_ID =  "folderId";
	public static final String STUBFOLDER_NAME =  "name";
	public static final String STUBFOLDER_TYPE =  "type";//在线 1， 预置0.
	public static final String STUBFOLDER_ENABLE =  "enable";//有效 1， 默认无效0. icon预取成功后会有效
	public static final String STUBFOLDER_SCR =  "screen";
	public static final String STUBFOLDER_X =  "x";
	public static final String STUBFOLDER_Y =  "y";
	public static final String STUBFOLDER_ICON_ID =  "iconResID";
	public static final String STUBFOLDER_ICON_URL =  "iconUrl";
	public static final String STUBFOLDER_ICON_PATH =  "iconPath";
	public static final String STUBFOLDER_SHOW_COUNT =  "showCount";
	public static final String STUBFOLDER_SHOW_TIME =  "showTime";
	public static final String STUBFOLDER_EDITABLE =  "editable";
	public static final String STUBFOLDER_DELETABLE =  "deletable";
	public static final String STUBFOLDER_SHOW_RED_POINT = "showRedPoint";
    public static final String STUBFOLDER_CREATED = "created";
    public static final String STUBFOLDER_DELETED = "deleted";
	// 游戏全部下载完毕后要更新。这个需要判断是不是sf的数据
	
    // ===========================================================
    // Fields
    // ===========================================================
	
    // ===========================================================
    // Constructors
    // ===========================================================
	
    // ===========================================================
    // Getter & Setter
    // ===========================================================
    
    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================
	@Override
	public String getName() {
		return TABLE_NAME;
	}
	
	@Override
	public int getVersion() {
		return TABLE_VERSION;
	}

	@Override
	public void create(SQLiteDatabase db) {
		try{
			db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
					_ID +" INTEGER PRIMARY KEY AUTOINCREMENT," +          
					STUBFOLDER_ID +" VARCHAR(20)," +          
					STUBFOLDER_NAME +" VARCHAR(20)," +
					STUBFOLDER_TYPE + " INTEGER default 0," +
					STUBFOLDER_ENABLE + " INTEGER default 0," +
					STUBFOLDER_SCR +" INTEGER default 0," + 
					STUBFOLDER_X +" INTEGER default 0," + 
					STUBFOLDER_Y +" INTEGER default 0," + 
					STUBFOLDER_ICON_ID +" INTEGER default 0," + 
					STUBFOLDER_ICON_URL +" VARCHAR(20)," + 
					STUBFOLDER_ICON_PATH +" VARCHAR(20)," + 
					STUBFOLDER_SHOW_COUNT + " INTEGER default 0," +
					STUBFOLDER_SHOW_TIME + " VARCHAR(20)," +
					STUBFOLDER_EDITABLE + " INTEGER default 0," +
					STUBFOLDER_DELETABLE +" INTEGER default 0," + 
					STUBFOLDER_SHOW_RED_POINT + " INTEGER default 0," +
                    STUBFOLDER_CREATED + " INTEGER default 0," +
                    STUBFOLDER_DELETED + " INTEGER default 0" +
					")"
					);
		}catch (SQLException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        boolean result = DatabaseUtil.checkColumnExists(db,TABLE_NAME,STUBFOLDER_CREATED);
        if(!result){
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + STUBFOLDER_CREATED + " INTEGER default 0");
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + STUBFOLDER_DELETED + " INTEGER default 0");
        }
        boolean result2 = DatabaseUtil.checkColumnExists(db,TABLE_NAME,STUBFOLDER_SHOW_RED_POINT);
        if(!result2){
        	db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + STUBFOLDER_SHOW_RED_POINT + " INTEGER default 0");
        }
	}
}
