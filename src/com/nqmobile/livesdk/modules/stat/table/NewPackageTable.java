package com.nqmobile.livesdk.modules.stat.table;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.nqmobile.livesdk.commons.db.AbsDataTable;
import com.nqmobile.livesdk.commons.db.DataProvider;

/**
 * Created by Rainbow on 2014/11/22.
 */
public class NewPackageTable extends AbsDataTable{

    // ===========================================================
    // Constants
    // ===========================================================

    public static final String TABLE_NAME = "new_package";
    public static final int TABLE_VERSION = 2;
    public static final String DATA_AUTHORITY = DataProvider.DATA_AUTHORITY;
    public static final Uri TABLE_URI = Uri.parse("content://"+DATA_AUTHORITY+"/" + TABLE_NAME);

    public static final String _ID = "_id";
    public static final String NEW_PACKAGE_PACKAGENAME = "packageName";
    public static final String NEW_PACKAGE_ISUPLOAD = "isUpload";

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
                            NEW_PACKAGE_PACKAGENAME +" VARCHAR(20)," +
                            NEW_PACKAGE_ISUPLOAD + " INTEGER default 0" +
                            ")"
            );
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
