package com.nqmobile.livesdk.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Rainbow on 2014/12/9.
 */
public class DatabaseUtil {

    // ===========================================================
    // Constants
    // ===========================================================

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

    public static boolean checkColumnExists(SQLiteDatabase db, String tableName,String columnName) {
        boolean result = false;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(
                    "select * from sqlite_master where name = ? and sql like ?",
                    new String[] { tableName, "%" + columnName + "%" });
            result = null != cursor && cursor.moveToFirst();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return result;
    }

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
