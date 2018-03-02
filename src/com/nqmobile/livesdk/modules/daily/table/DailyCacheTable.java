package com.nqmobile.livesdk.modules.daily.table;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.nqmobile.livesdk.commons.db.AbsDataTable;
import com.nqmobile.livesdk.commons.db.DataProvider;

public class DailyCacheTable extends AbsDataTable {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String TABLE_NAME = "daily_cache";	
	public static final int TABLE_VERSION = 2;
	public static final Uri DAILY_CACHE_URI = Uri.parse("content://" + DataProvider.DATA_AUTHORITY + "/" + TABLE_NAME);

	public static final String _ID = "_id";
	public static final String DAILY_CACHE_ID = "dailyId";
	public static final String DAILY_CACHE_RESOURCEID = "resourceId";
	public static final String DAILY_CACHE_TYPE = "type";
	public static final String DAILY_CACHE_ICON_URL = "iconUrl";
	public static final String DAILY_CACHE_SEQ = "seq";
	
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
					DAILY_CACHE_ID +" VARCHAR(20)," +
					DAILY_CACHE_RESOURCEID +" VARCHAR(20)," +
					DAILY_CACHE_TYPE +" INTEGER," +
					DAILY_CACHE_ICON_URL + " VARCHAR(50)," +
					DAILY_CACHE_SEQ + " INTEGER default 0" +
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
