/**
 * 
 */
package com.nqmobile.livesdk.commons.prefetch.table;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.nqmobile.livesdk.commons.db.AbsDataTable;
import com.nqmobile.livesdk.commons.db.DataProvider;
import com.nqmobile.livesdk.utils.DatabaseUtil;

/**
 * @author HouKangxi
 *
 */
public class PrefetchTable extends AbsDataTable {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String TABLE_NAME = "prefetch";
	
	public static final Uri TABLE_URI = Uri.parse("content://" + DataProvider.DATA_AUTHORITY + "/" + TABLE_NAME);
	// field columns
	public static final String DownloadId = "downloadId";//long--download表里的id
	public static final String ResId = "resId";// string
	public static final String ResTYPE = "resType";// int
	public static final String FEATURE = "feature";//int
	public static final String SOURCETYPE = "source";//int 同AppCache中的sourceType
	public static final String URL = "url"; // string
	public static final String PATH = "path";// string
	public static final String SIZE = "size";// long
	public static final String STATUS = "status";//0--待下载 1--正在下载 2--已下载完成 -1--下载失败 -2 已删除
	public static final String CREATE_TIME = "createTime";// long
	public static final String PACKAGE = "pkg";// string 包名--apk类型需要

	public static enum Status{
		 waitDownLoad(0)
		,Downloading(1)
		,DownloadSuccess(2)
		,DownloadFailure(-1)
		,DownloadPaused(4)
		,Removed(-2)
		;
		
		private int mCode;
		private Status(int code){
			mCode = code;
		}
		public static Status valueOf(int code){
			for (Status st : values()) {
				if (code == st.code()) {
					return st;
				}
			}
			return null;
		}
		public int code(){
			return mCode;
		}
	}
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
		return 1;
	}

	@Override
	public void create(SQLiteDatabase db) {
		try {
			StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
			sql.append(TABLE_NAME);
			sql.append('(');
			sql.append(_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT");
			sql.append(',');
			sql.append(DownloadId).append(" INTEGER");
			sql.append(',');
			sql.append(ResId).append(" VARCHAR(10)");
			sql.append(',');
			sql.append(ResTYPE).append(" INTEGER");
			sql.append(',');
			sql.append(PACKAGE).append(" VARCHAR(50)");
			sql.append(',');
			sql.append(FEATURE).append(" INTEGER");
			sql.append(',');
			sql.append(SOURCETYPE).append(" INTEGER");
			sql.append(',');
			sql.append(URL).append(" VARCHAR(255)");
			sql.append(',');
			sql.append(PATH).append(" VARCHAR(255)");
			sql.append(',');
			sql.append(SIZE).append(" INTEGER default 0");
			sql.append(',');
			sql.append(STATUS).append(" INTEGER default 0");
			sql.append(',');
			sql.append(CREATE_TIME).append(" INTEGER default 0");
			sql.append(')');

			db.execSQL(sql.toString());
			
			db.execSQL(String.format("CREATE INDEX IF NOT EXISTS _download_index ON %s(%s)",TABLE_NAME,DownloadId));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		boolean result = DatabaseUtil.checkColumnExists(db,TABLE_NAME,SOURCETYPE);
        if(!result){        	
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + SOURCETYPE + " INTEGER");
        }
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
