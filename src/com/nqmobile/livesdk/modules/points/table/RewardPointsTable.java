package com.nqmobile.livesdk.modules.points.table;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.nqmobile.livesdk.commons.db.AbsDataTable;
import com.nqmobile.livesdk.commons.db.DataProvider;

/**
 * Created by Rainbow on 2014/11/18.
 */
public class RewardPointsTable extends AbsDataTable{

    public static final String TABLE_NAME = "reward_points_history";
    public static final int TABLE_VERSION = 2;
    public static final String DATA_AUTHORITY = DataProvider.DATA_AUTHORITY;
    public static final Uri TABLE_URI = Uri.parse("content://"+DATA_AUTHORITY+"/" + TABLE_NAME);

    public static final String _ID = "_id";
    public static final String REWARD_POINTS_HISTORY_RESID = "resId";
    public static final String REWARD_POINTS_HISTORY_SCENE = "scene";
    public static final String REWARD_POINTS_HISTORY_TIME = "time";
    public static final String REWARD_POINTS_HISTORY_TRACKID = "trackid";
    public static final String REWARD_POINTS_HISTORY_POINTS = "points";

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
                            REWARD_POINTS_HISTORY_RESID +" VARCHAR(20)," +
                            REWARD_POINTS_HISTORY_SCENE + " INTEGER default 0," +
                            REWARD_POINTS_HISTORY_TIME + " INTEGER default 0," +
                            REWARD_POINTS_HISTORY_TRACKID + " VARCHAR(20)," +
                            REWARD_POINTS_HISTORY_POINTS +" INTEGER default 0" +
                            ")"
            );
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
