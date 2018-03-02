/**
 * 
 */
package com.nqmobile.livesdk.modules.locker.tables;

import android.net.Uri;

import com.nqmobile.livesdk.commons.db.DataProvider;

/**
 * @author HouKangxi
 *
 */
public class LockerCacheTable extends AbsLockerTable {
	public static final int LOCKER_CACHE = 19;
	public static final String LOCKER_CACHE_TABLE = "locker_cache";
	public static final Uri LOCKER_CACHE_URI = Uri.parse("content://"
			+ DataProvider.DATA_AUTHORITY + "/" + LOCKER_CACHE_TABLE);

	@Override
	protected String tableName() {
		return LOCKER_CACHE_TABLE;
	}

}
