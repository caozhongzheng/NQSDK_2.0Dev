/**
 * 
 */
package com.nqmobile.livesdk.modules.locker.tables;

import com.nqmobile.livesdk.commons.db.DataProvider;

import android.net.Uri;

/**
 * @author HouKangxi
 *
 */
public class LockerLocalTable extends AbsLockerTable {
	/* 本地锁屏表 ，字段同锁屏缓存表 */
	public static final int LOCAL_LOCKER = 18;
	public static final String LOCAL_LOCKER_TABLE = "locker_local";
	public static final Uri LOCAL_LOCKER_URI = Uri.parse("content://"
			+ DataProvider.DATA_AUTHORITY + "/" + LOCAL_LOCKER_TABLE);

	@Override
	protected String tableName() {
		return LOCAL_LOCKER_TABLE;
	}
}
