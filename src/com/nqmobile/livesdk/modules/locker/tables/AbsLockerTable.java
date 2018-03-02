/**
 * 
 */
package com.nqmobile.livesdk.modules.locker.tables;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.nqmobile.livesdk.commons.db.AbsDataTable;
import com.nqmobile.livesdk.commons.db.DataProvider;

/**
 * @author HouKangxi
 *
 */
public abstract class AbsLockerTable extends AbsDataTable {
	// ===========================================================
	// Constants
	// ===========================================================
    public static final String DATA_AUTHORITY = DataProvider.DATA_AUTHORITY;
	public static final String LOCKER_ID = "lockerId";
	public static final String LOCKER_NAME = "name";
	/** 功能模块来源编号：0：store列表；1：banner；2每日推荐 */
	public static final String LOCKER_SOURCE_TYPE = "sourceType";
	/** 栏目分类：0：新品；1：热门 */
	public static final String LOCKER_COLUMN = "column";
	/** 引擎类型：0：自有锁屏 ；1：拉风锁屏 */
	public static final String LOCKER_TYPE = "type";
	public static final String LOCKER_AUTHOR = "author";
	public static final String LOCKER_VERSION = "version";
	public static final String LOCKER_URL = "url";
	public static final String LOCKER_PATH = "path";
	public static final String LOCKER_PREVIEW_URL = "previewUrl";
	public static final String LOCKER_PREVIEW_PATH = "previewPath";
	public static final String LOCKER_ICON_URL = "iconUrl";
	public static final String LOCKER_ICON_PATH = "iconPath";
	public static final String LOCKER_DOWNLOAD_COUNT = "downloadCount";
	public static final String LOCKER_SIZE = "size";
	public static final String LOCKER_DAILYICON = "dailyicon";
	public static final String LOCKER_UPDATETIME = "updateTime";
	public static final String LOCKER_LOCALTIME = "localTime";

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
	public void create(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName() + "("
					+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + LOCKER_ID
					+ " VARCHAR(20)," + LOCKER_NAME + " VARCHAR(20),"
					+ LOCKER_SOURCE_TYPE + " INTEGER default 0,"
					+ LOCKER_COLUMN + " INTEGER default 0," + LOCKER_TYPE
					+ " INTEGER default 0," + LOCKER_AUTHOR + " VARCHAR(20),"
					+ LOCKER_VERSION + " VARCHAR(20)," + LOCKER_URL
					+ " VARCHAR(20)," + LOCKER_PATH + " VARCHAR(20),"
					+ LOCKER_PREVIEW_URL + " VARCHAR(20),"
					+ LOCKER_PREVIEW_PATH + " VARCHAR(20)," + LOCKER_ICON_URL
					+ " VARCHAR(20)," + LOCKER_ICON_PATH + " VARCHAR(20),"
					+ LOCKER_DOWNLOAD_COUNT + " VARCHAR(20)," + LOCKER_SIZE
					+ " VARCHAR(20)," + LOCKER_DAILYICON + " VARCHAR(20),"
					+ LOCKER_UPDATETIME + " INTEGER default 0,"
					+ LOCKER_LOCALTIME + " INTEGER default 0" + ")");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected abstract String tableName();
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
