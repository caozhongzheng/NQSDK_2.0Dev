package com.nqmobile.livesdk.modules.locker;

import android.content.ContentValues;

import com.nqmobile.livesdk.modules.locker.tables.AbsLockerTable;

public class LockerConverter {
	/**
	 * 将locker对象转换层数据库操作的ContentValues对象
	 *
	 * @param column
	 *            0：新品；1：热门；-1：未知（下载锁屏，保存数据到Local表时，栏目字段为-1）
	 * @param locker
	 * @return
	 */
	public static ContentValues toContentValues(int column, Locker locker) {
		ContentValues values = null;
		if (locker != null) {
			values = new ContentValues();
			values.put(AbsLockerTable.LOCKER_ID, locker.getStrId());
			values.put(AbsLockerTable.LOCKER_SOURCE_TYPE,
					locker.getIntSourceType());
			values.put(AbsLockerTable.LOCKER_COLUMN, column);
			values.put(AbsLockerTable.LOCKER_NAME, locker.getStrName());
			values.put(AbsLockerTable.LOCKER_TYPE, locker.getIntEngineType());
			values.put(AbsLockerTable.LOCKER_AUTHOR, locker.getStrAuthor());
			values.put(AbsLockerTable.LOCKER_VERSION, locker.getIntMinVersion());
			values.put(AbsLockerTable.LOCKER_URL, locker.getStrLockerUrl());
			values.put(AbsLockerTable.LOCKER_PATH, locker.getStrLockerPath());
			values.put(AbsLockerTable.LOCKER_PREVIEW_URL,
					locker.getStrPreviewUrl());
			values.put(AbsLockerTable.LOCKER_PREVIEW_PATH,
					locker.getStrPreviewPath());
			values.put(AbsLockerTable.LOCKER_ICON_URL, locker.getStrIconUrl());
			values.put(AbsLockerTable.LOCKER_ICON_PATH, locker.getStrIconPath());
			values.put(AbsLockerTable.LOCKER_DOWNLOAD_COUNT,
					locker.getLongDownloadCount());
			values.put(AbsLockerTable.LOCKER_SIZE, locker.getLongSize());
			values.put(AbsLockerTable.LOCKER_DAILYICON,
					locker.getStrDailyIconUrl());
			values.put(AbsLockerTable.LOCKER_LOCALTIME, locker.getLongLocalTime());
			values.put(AbsLockerTable.LOCKER_UPDATETIME,
					locker.getLongUpdateTime());
		}
		return values;
	}
}
