package com.nqmobile.livesdk.modules.daily.table;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.nqmobile.livesdk.commons.db.AbsDataTable;
import com.nqmobile.livesdk.commons.db.DataProvider;

public class WebCacheTable extends AbsDataTable {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String TABLE_NAME = "web_cache";	
	public static final int TABLE_VERSION = 2;
	public static final Uri WEB_CACHE_URI = Uri.parse("content://" + DataProvider.DATA_AUTHORITY + "/" + TABLE_NAME);
	
	public static final String _ID = "_id";
	public static final String WEB_ID = "webId";
	public static final String WEB_NAME = "name";
	public static final String WEB_JUMP_TYPE = "jumpType";
	public static final String WEB_URL = "url";
	public static final String WEB_DAILYICON = "dailyicon";
	public static final String WEB_PREVIEW_URL = "previewUrl";
	public static final String WEB_ICON_URL = "iconUrl";
	
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
					WEB_ID +" VARCHAR(20)," +
                    WEB_NAME +" VARCHAR(20)," +
                    WEB_JUMP_TYPE + " INTEGER default 0," +
                    WEB_URL + " VARCHAR(20)," +
                    WEB_DAILYICON + " VARCHAR(20)," +
                    WEB_PREVIEW_URL + " VARCHAR(20)," +
                    WEB_ICON_URL + " VARCHAR(20)" +
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
