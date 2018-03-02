package com.nqmobile.livesdk.modules.association.table;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.nqmobile.livesdk.commons.db.AbsDataTable;
import com.nqmobile.livesdk.commons.db.DataProvider;

public class AssociationTable extends AbsDataTable {
    // ===========================================================
    // Constants
    // ===========================================================	
	public static final String TABLE_NAME = "association_cache";
	public static final int TABLE_VERSION = 2;
	public static final String DATA_AUTHORITY = DataProvider.DATA_AUTHORITY;
	public static final Uri TABLE_URI = Uri.parse("content://"+DATA_AUTHORITY+"/" + TABLE_NAME);
	
	public static final String _ID = "_id";	
	
	public static final String ASSOCIATION_PACKAGENAME =  "packageName";
	public static final String ASSOCIATION_APPRESOURCE =  "appResourceId";
	public static final String ASSOCIATION_RECOMMENDED_INTRODUCTION =  "introduction";//被推荐的应用广告语
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
					ASSOCIATION_PACKAGENAME +" VARCHAR(200)," +
					ASSOCIATION_APPRESOURCE + " VARCHAR(200)," +
					ASSOCIATION_RECOMMENDED_INTRODUCTION +" VARCHAR(2000)" +
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
