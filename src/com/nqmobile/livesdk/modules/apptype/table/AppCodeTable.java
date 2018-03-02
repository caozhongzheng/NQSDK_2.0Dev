package com.nqmobile.livesdk.modules.apptype.table;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.nqmobile.livesdk.commons.db.AbsDataTable;
import com.nqmobile.livesdk.commons.db.DataProvider;


/**
 * 应用分类表 
 * @author chenyanmin
 * @date 2014-10-26
 */
public class AppCodeTable extends AbsDataTable {
    // ===========================================================
    // Constants
    // ===========================================================	
	public static final String TABLE_NAME = "app_code";
	public static final int TABLE_VERSION = 2;
	public static final String DATA_AUTHORITY = DataProvider.DATA_AUTHORITY;
	public static final Uri TABLE_URI = Uri.parse("content://"+DATA_AUTHORITY+"/" + TABLE_NAME);
	
	public static final String _ID = "_id";	
	
	public static final String COLUMN_APPCODE = "code";
	public static final String COLUMN_PACKAGENAME = "packageName";
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
					COLUMN_APPCODE +" VARCHAR(20)," +
					COLUMN_PACKAGENAME + " INTEGER default 0" +
					")"
					);
		}catch (SQLException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
}
