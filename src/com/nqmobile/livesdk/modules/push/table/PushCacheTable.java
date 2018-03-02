package com.nqmobile.livesdk.modules.push.table;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.nqmobile.livesdk.commons.db.AbsDataTable;
import com.nqmobile.livesdk.commons.db.DataProvider;

/**
 * Push缓存表
 * 
 * @author HouKangxi
 *
 */
public class PushCacheTable extends AbsDataTable {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String TABLE_NAME = "push_cache";
	public static final String DATA_AUTHORITY = DataProvider.DATA_AUTHORITY;
	public static final String _ID = "_id";
	public static final Uri PUSH_CACHE_URI = Uri.parse("content://"
			+ DataProvider.DATA_AUTHORITY + "/" + TABLE_NAME);

	public static final String PUSH_ID = "pushId";
	public static final String COLUMN_APPCODE = "code";
	public static final String COLUMN_PACKAGENAME = "packageName";
	/** 资源类型：0应用 1壁纸 2主题 */
	public static final String PUSH_RESTYPE = "type";
	public static final String PUSH_LINK_RESOURCEID = "resourceId";
	public static final String PUSH_TITLE = "title";
	public static final String PUSH_SUBTITLE = "subtitle";
	public static final String PUSH_ICON_URL = "iconUrl";
	public static final String PUSH_STYLE = "style";
	public static final String PUSH_BIGICON_URL = "bigIconUrl";
	public static final String PUSH_USEBTN = "useBtn";
	public static final String PUSH_UNLIKE_TEXT = "unlikeText";
	public static final String PUSH_DOWN_TEXT = "downText";
	public static final String PUSH_JUMPTYPE = "jumpType";
	public static final String PUSH_DOWNURL = "downUrl";
	public static final String PUSH_PACKAGENAME = "packageName";
	/** 用户场景限制 0---任何情况下都可以展示消息 1--仅回到桌面下展示消息 */
	public static final String PUSH_DESKTOPTYPE = "desktopType";
	/** 联网环境限制 0---只要有网络都可以展示消息 1--仅WIFI下展示消息 */
	public static final String PUSH_NETTYPE = "netType";
	/** 消息生效时间 */
	public static final String PUSH_VALIDTIME = "validTime";
	/** 消息失效时间 */
	public static final String PUSH_EXPIRTIME = "ExpirTime";
	/** 展示时间段开始时间,如08:00 */
	public static final String PUSH_STARTTIME = "startTime";
	/** 展示时间段结束时间,如23:00 */
	public static final String PUSH_ENDTIME = "endTime";

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
	public void create(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" + _ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + PUSH_ID
					+ " VARCHAR(20)," + PUSH_RESTYPE + " INTEGER,"
					+ PUSH_LINK_RESOURCEID + " VARCHAR(20)," + PUSH_TITLE
					+ " VARCHAR(20)," + PUSH_SUBTITLE + " VARCHAR(20),"
					+ PUSH_ICON_URL + " VARCHAR(20)," + PUSH_STYLE
					+ " INTEGER," + PUSH_BIGICON_URL + " VARCHAR(20),"
					+ PUSH_USEBTN + " INTEGER," + PUSH_UNLIKE_TEXT
					+ " VARCHAR(10)," + PUSH_DOWN_TEXT + " VARCHAR(10),"
					+ PUSH_JUMPTYPE + " INTEGER," + PUSH_DOWNURL
					+ " VARCHAR(20)," + PUSH_PACKAGENAME + " VARCHAR(20),"
					+ PUSH_DESKTOPTYPE + " INTEGER," + PUSH_NETTYPE
					+ " INTEGER," + PUSH_VALIDTIME + " INTEGER default 0,"
					+ PUSH_EXPIRTIME + " INTEGER default 0," + PUSH_STARTTIME
					+ " VARCHAR(20)," + PUSH_ENDTIME + " VARCHAR(20)" + ")");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO

	}
}
