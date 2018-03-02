package com.nqmobile.livesdk.modules.incrementupdate.table;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.nqmobile.livesdk.commons.db.AbsDataTable;
import com.nqmobile.livesdk.commons.db.DataProvider;

public class NewVersionTable extends AbsDataTable {
    // ===========================================================
    // Constants
    // ===========================================================	
	public static final String TABLE_NAME = "newversion_cache";
	public static final int TABLE_VERSION = 2;
	public static final String DATA_AUTHORITY = DataProvider.DATA_AUTHORITY;
	public static final Uri TABLE_URI = Uri.parse("content://"+DATA_AUTHORITY+"/" + TABLE_NAME);
	
	public static final String _ID = "_id";	
	
	public static final String NEWVERSION_NEW_VERSION_NAME =  "newVersionName";
	public static final String NEWVERSION_UPGRADE_TYPE =  "upgradeType";
	public static final String NEWVERSION_AUTO_DOWNLOAD =  "autoDownload";
	public static final String NEWVERSION_PROMPT_TYPE =  "promptType";
	public static final String NEWVERSION_PROMPT_TITLE =  "promptTitle";
	public static final String NEWVERSION_PROMPT_CONTENT =  "promptContent";
	public static final String NEWVERSION_PROMPT_START_TIME =  "promptStartTime";
	public static final String NEWVERSION_PROMPT_END_TIME =  "promptEndTime";
	public static final String NEWVERSION_PROMPT_INTERVAL =  "promptInterval";
	public static final String NEWVERSION_PROMPT_NETWORK =  "promptNetwork";
	public static final String NEWVERSION_OLD_FILE_SIZE =  "oldFileSize";
	public static final String NEWVERSION_OLD_FILE_MD5 =  "oldFileMd5";
	public static final String NEWVERSION_PATCH_ALGORITHM =  "patchAlgorithm";
	public static final String NEWVERSION_PATCH_SIZE =  "patchSize";
	public static final String NEWVERSION_PATCH_MD5 =  "patchMd5";
	public static final String NEWVERSION_PATCH_URL =  "patchUrl";
	public static final String NEWVERSION_NEW_FILE_SIZE =  "newFileSize";
	public static final String NEWVERSION_NEW_FILE_MD5 =  "newFileMd5";
	public static final String NEWVERSION_NEW_FILE_URL =  "newFileUrl";
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
					NEWVERSION_NEW_VERSION_NAME +" VARCHAR(200)," +
					NEWVERSION_UPGRADE_TYPE +" INTEGER default -1," +
					NEWVERSION_AUTO_DOWNLOAD +" INTEGER default -1," +
					NEWVERSION_PROMPT_TYPE +" INTEGER default -1," +
					NEWVERSION_PROMPT_TITLE +" VARCHAR(200)," +
					NEWVERSION_PROMPT_CONTENT +" VARCHAR(200)," +
					NEWVERSION_PROMPT_START_TIME +" VARCHAR(200)," +
					NEWVERSION_PROMPT_END_TIME +" VARCHAR(200)," +
					NEWVERSION_PROMPT_INTERVAL +" INTEGER default -1," +
					NEWVERSION_PROMPT_NETWORK +" INTEGER default -1," +
					NEWVERSION_OLD_FILE_SIZE +" INTEGER default -1," +
					NEWVERSION_OLD_FILE_MD5 +" VARCHAR(200)," +
					NEWVERSION_PATCH_ALGORITHM +" VARCHAR(200)," +
					NEWVERSION_PATCH_SIZE +" INTEGER default -1," +
					NEWVERSION_PATCH_MD5 +" VARCHAR(200)," +
					NEWVERSION_PATCH_URL +" VARCHAR(200)," +
					NEWVERSION_NEW_FILE_SIZE +" INTEGER default -1," +
					NEWVERSION_NEW_FILE_MD5 +" VARCHAR(200)," +
					NEWVERSION_NEW_FILE_URL +" VARCHAR(200)" +
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
