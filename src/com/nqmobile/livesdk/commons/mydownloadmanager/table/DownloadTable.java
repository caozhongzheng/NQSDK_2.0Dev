package com.nqmobile.livesdk.commons.mydownloadmanager.table;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.nqmobile.livesdk.commons.db.AbsDataTable;
import com.nqmobile.livesdk.commons.db.DataProvider;
import com.nqmobile.livesdk.utils.DatabaseUtil;

/**
 * Created by Rainbow on 2014/11/22.
 */
public class DownloadTable extends AbsDataTable{

    // ===========================================================
    // Constants
    // ===========================================================

    private static final String TABLE_NAME = "download";
    public static final int TABLE_VERSION = 2;
    public static final String DATA_AUTHORITY = DataProvider.DATA_AUTHORITY;
    public static final Uri TABLE_URI = Uri.parse("content://"+DATA_AUTHORITY+"/" + TABLE_NAME);

    public static final String _ID = "_id";
    public static final String DOWNLOAD_DOWNLOAD_ID = "downloadId";
    public static final String DOWNLOAD_TYPE = "type";
    public static final String DOWNLOAD_URL = "url";
    public static final String DOWNLOAD_RES_ID = "resId";
    public static final String DOWNLOAD_DEST_PATH = "destPath";
    public static final String DOWNLOAD_TOTAL_SIZE = "totalSize";
    public static final String DOWNLOAD_IS_FINISH = "is_finish";
    public static final String DOwNLOAD_ICON_URL = "icon_url";
    public static final String DOWNLOAD_NAME = "name";
    public static final String DOWNLOAD_SHOWFLAG = "showflag";
    public static final String DOWNLOAD_PACKAGENAME = "packagename";
    public static final String DOWNLOAD_URL_INDEX = "url_index";
    public static final int DOWNLOAD_DOWNLOAD_ID_INDEX = 1;

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
                            DOWNLOAD_DOWNLOAD_ID +" INTEGER(20) default -1," +
                            DOWNLOAD_TYPE +" INTEGER(20) default -1," +
                            DOWNLOAD_RES_ID +" VARCHAR(50)," +
                            DOWNLOAD_URL + " VARCHAR(500)," +
                            DOWNLOAD_TOTAL_SIZE + " INTEGER(20) default -1," +
                            DOWNLOAD_IS_FINISH + " INTEGER default 0," +
                            DOwNLOAD_ICON_URL + " VARCHAR(20)," +
                            DOWNLOAD_NAME + " VARCHAR(20)," +
                            DOWNLOAD_SHOWFLAG + " INTEGER default 0," +
                            DOWNLOAD_DEST_PATH + " VARCHAR(500)," +
                            DOWNLOAD_PACKAGENAME + " VARCHAR(20)" +
                            ")"
            );
            db.execSQL("CREATE INDEX IF NOT EXISTS " + DOWNLOAD_URL_INDEX +
                            " ON " + TABLE_NAME + "(" +
                            DOWNLOAD_URL + ")"
            );

        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        boolean result = DatabaseUtil.checkColumnExists(db,TABLE_NAME,DOWNLOAD_IS_FINISH);
        if(!result){        	
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + DOWNLOAD_IS_FINISH + " INTEGER default 0");
        }
        result = DatabaseUtil.checkColumnExists(db,TABLE_NAME,DOwNLOAD_ICON_URL);
        if(!result){        	
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + DOwNLOAD_ICON_URL + " VARCHAR(20)");
        }
        result = DatabaseUtil.checkColumnExists(db,TABLE_NAME,DOWNLOAD_NAME);
        if(!result){        	
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + DOWNLOAD_NAME + " VARCHAR(20)");
        }
        result = DatabaseUtil.checkColumnExists(db,TABLE_NAME,DOWNLOAD_SHOWFLAG);
        if(!result){        	
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + DOWNLOAD_SHOWFLAG + " INTEGER default 0");
        }
        result = DatabaseUtil.checkColumnExists(db,TABLE_NAME,DOWNLOAD_PACKAGENAME);
        if(!result){        	
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + DOWNLOAD_PACKAGENAME + " VARCHAR(20)");
        }        
    }

    // ===========================================================
    // Methods
    // ===========================================================

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
