package com.nqmobile.livesdk.utils;

import android.database.Cursor;

import com.nqmobile.livesdk.commons.log.NqLog;

public class CursorUtils {
	public static void closeCursor(Cursor cursor) {
		try {
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			NqLog.e(e);
		}
	}
}
